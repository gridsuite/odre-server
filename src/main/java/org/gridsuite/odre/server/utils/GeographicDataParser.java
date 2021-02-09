/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.time.StopWatch;
import org.gridsuite.odre.server.dto.Coordinate;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.jgrapht.Graphs;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.min;
import static java.util.Collections.reverse;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public final class GeographicDataParser {

    private GeographicDataParser() {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GeographicDataParser.class);

    private static final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder('"', ';', System.lineSeparator()).build();

    private static final int THRESHOLD = 5;

    public static Map<String, SubstationGeoData> parseSubstations(BufferedReader bufferedReader) {
        Map<String, SubstationGeoData> substations = new HashMap<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int substationCount = 0;

        try (CsvListReader csvReader = new CsvListReader(bufferedReader, CSV_PREFERENCE)) {
            // skip header
            csvReader.read();

            List<String> tokens;
            while ((tokens = csvReader.read()) != null) {
                String id = tokens.get(0);

                double lon = Double.parseDouble(tokens.get(5));
                double lat = Double.parseDouble(tokens.get(6));

                SubstationGeoData substation = substations.get(id);
                if (substation == null) {
                    SubstationGeoData substationGeoData = new SubstationGeoData(id, "FR", new Coordinate(lat, lon));
                    substations.put(id, substationGeoData);
                }

                substationCount++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("{} substations read  in {} ms", substationCount, stopWatch.getTime());

        return substations;
    }

    private static double distanceCoordinate(Coordinate coord1, Coordinate coord2) {
        return DistanceCalculator.distance(coord1.getLat(), coord1.getLon(), coord2.getLat(), coord2.getLon(), "M");
    }

    public static String findSubstationStart(Map<String, SubstationGeoData> substationGeoData, String lineId, List<Coordinate> coordinates) {
        String substation1 = lineId.substring(0, 5).trim();
        String substation2 = lineId.substring(8).trim();
        SubstationGeoData geo1 = substationGeoData.get(substation1);
        SubstationGeoData geo2 = substationGeoData.get(substation2);

        if (geo1 == null && geo2 == null) {
            return "";
        } else if (geo1 != null && geo2 != null) {
            return distanceCoordinate(geo1.getCoordinate(), coordinates.get(0))
                    < distanceCoordinate(geo2.getCoordinate(), coordinates.get(0))
                    ? substation1 : substation2;
        } else {
            double d1 = distanceCoordinate((geo1 != null ? geo1 : geo2).getCoordinate(), coordinates.get(0));
            double d2 = distanceCoordinate((geo1 != null ? geo1 : geo2).getCoordinate(), coordinates.get(coordinates.size() - 1));
            if (d1 > d2) {
                Collections.reverse(coordinates);
            }
            return geo1 != null ? substation1 : substation2;
        }
    }

    public static Map<String, LineGeoData> parseLines(BufferedReader aerialLinesBr, BufferedReader undergroundLinesBr,
                                                      Map<String, SubstationGeoData> stringSubstationGeoDataMap) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Map<String, Graph<Coordinate, Object>> graphByLine = new HashMap<>();

        parseLine(graphByLine, aerialLinesBr, 8, 9, 10, 11);
        parseLine(graphByLine, undergroundLinesBr, 9, 10, 11, 12);

        Map<String, LineGeoData> lines = new HashMap<>();

        int linesWithOneConnectedSet = 0;
        int linesWithTwoOrMoreConnectedSets = 0;

        int oneConnectedSetDiscarded = 0;
        int twoOrMoreConnectedSetsDiscarded = 0;

        for (Map.Entry<String, Graph<Coordinate, Object>> e : graphByLine.entrySet()) {
            String lineId = e.getKey();
            Graph<Coordinate, Object> graph = e.getValue();
            List<Set<Coordinate>> connectedSets = new ConnectivityInspector<>(graph).connectedSets();
            if (connectedSets.size() == 1) {
                linesWithOneConnectedSet++;
                List<Coordinate> ends = getEnds(connectedSets.get(0), graph);
                if (ends.size() == 2) {
                    List<Coordinate> coordinates = Lists.newArrayList(new BreadthFirstIterator<>(graph, ends.get(0)));
                    String substationStart = findSubstationStart(stringSubstationGeoDataMap, lineId, coordinates);
                    LineGeoData line = new LineGeoData(lineId, "FR", "FR", substationStart, coordinates);
                    lines.put(lineId, line);
                } else {
                    oneConnectedSetDiscarded++;
                }
            } else {
                List<List<Coordinate>> coordinatesComponents = new ArrayList<>();
                linesWithTwoOrMoreConnectedSets++;
                for (Set<Coordinate> connectedSet : connectedSets) {
                    List<Coordinate> endsComponent = getEnds(connectedSet, graph);
                    if (endsComponent.size() == 2) {
                        List<Coordinate> coordinatesComponent = Lists.newArrayList(new BreadthFirstIterator<>(graph, endsComponent.get(0)));
                        coordinatesComponents.add(coordinatesComponent);
                    } else {
                        break;
                    }
                }

                if (coordinatesComponents.size() != connectedSets.size()) {
                    twoOrMoreConnectedSetsDiscarded++;
                    continue;
                }

                List<Coordinate> aggregatedCoordinates =  aggregateCoordinates(coordinatesComponents);
                String substationStart = findSubstationStart(stringSubstationGeoDataMap, lineId, aggregatedCoordinates);
                LineGeoData line = new LineGeoData(lineId, "FR", "FR", substationStart, aggregatedCoordinates);
                lines.put(lineId, line);
            }
        }

        LOGGER.info("{} lines read in {} ms", lines.size(), stopWatch.getTime());
        LOGGER.info("{} lines have one Connected set, {} of them were discarded", linesWithOneConnectedSet, oneConnectedSetDiscarded);
        LOGGER.info("{} lines have two or more Connected sets, {} of them were discarded", linesWithTwoOrMoreConnectedSets, twoOrMoreConnectedSetsDiscarded);

        if (graphByLine.size() != lines.size()) {
            LOGGER.warn("Total discarded lines : {}/{} ",
                    graphByLine.size() - lines.size(), graphByLine.size());
        }

        return lines;
    }

    private static void parseLine(Map<String, Graph<Coordinate, Object>> graphByLine,
                                  BufferedReader br, int lon1Index, int lat1Index, int lon2Index, int lat2Index) {
        try (CsvListReader csvReader = new CsvListReader(br, CSV_PREFERENCE)) {
            // skip header
            csvReader.read();

            List<String> tokens;
            while ((tokens = csvReader.read()) != null) {
                List<String> tokensCopy = tokens;
                List<String> ids = IntStream.of(1, 16, 19, 22, 25)
                        .mapToObj(tokensCopy::get)
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.toList());

                if (ids.isEmpty()) {
                    continue;
                }

                double lon1 = Double.parseDouble(tokens.get(lon1Index));
                double lat1 = Double.parseDouble(tokens.get(lat1Index));
                double lon2 = Double.parseDouble(tokens.get(lon2Index));
                double lat2 = Double.parseDouble(tokens.get(lat2Index));
                Coordinate coordinate1 = new Coordinate(lat1, lon1);
                Coordinate coordinate2 = new Coordinate(lat2, lon2);
                for (String lineId : ids) {
                    Graph<Coordinate, Object> graph = graphByLine.get(lineId);
                    if (graph == null) {
                        graph = new Pseudograph<>(Object.class);
                        graphByLine.put(lineId, graph);
                    }
                    if (!graph.containsVertex(coordinate1)) {
                        graph.addVertex(coordinate1);
                    }
                    if (!graph.containsVertex(coordinate2)) {
                        graph.addVertex(coordinate2);
                    }
                    graph.addEdge(coordinate1, coordinate2);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Coordinate> getEnds(Set<Coordinate> connectedSet, Graph<Coordinate, Object> graph) {
        List<Coordinate> ends = new ArrayList<>();
        for (Coordinate coordinate : connectedSet) {
            if (Graphs.neighborListOf(graph, coordinate).size() == 1) {
                ends.add(coordinate);
            }
        }
        return ends;
    }

    private static double getBranchLength(List<Coordinate> coordinatesComponent) {
        return DistanceCalculator.distance(coordinatesComponent.get(0).getLat(), coordinatesComponent.get(0).getLon(),
                coordinatesComponent.get(coordinatesComponent.size() - 1).getLat(), coordinatesComponent.get(coordinatesComponent.size() - 1).getLon());
    }

    private static List<Coordinate> aggregateCoordinates(List<List<Coordinate>> coordinatesComponents) {
        coordinatesComponents.sort((comp1, comp2) -> (int) (getBranchLength(comp2) - getBranchLength(comp1)));
        return aggregateCoordinates(coordinatesComponents.get(0), coordinatesComponents.get(1));
    }

    private static List<Coordinate> aggregateCoordinates(List<Coordinate> coordinatesComponent1, List<Coordinate> coordinatesComponent2) {
        List<Coordinate> aggregatedCoordinates;

        double l1 = getBranchLength(coordinatesComponent1);
        double l2 = getBranchLength(coordinatesComponent2);

        if (100 * l2 / l1 < THRESHOLD) {
            return coordinatesComponent1;
        }

        double d1 = DistanceCalculator.distance(coordinatesComponent1.get(0).getLat(), coordinatesComponent1.get(0).getLon(),
                coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLat(), coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLon());

        double d2 = DistanceCalculator.distance(coordinatesComponent1.get(0).getLat(), coordinatesComponent1.get(0).getLon(),
                coordinatesComponent2.get(0).getLat(), coordinatesComponent2.get(0).getLon());

        double d3 = DistanceCalculator.distance(coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLat(), coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLon(),
                coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLat(), coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLon());

        double d4 = DistanceCalculator.distance(coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLat(), coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLon(),
                coordinatesComponent2.get(0).getLat(), coordinatesComponent2.get(0).getLon());

        List<Double> distances = Arrays.asList(d1, d2, d3, d4);
        double min = min(distances);

        if (d1 == min) {
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent2);
            aggregatedCoordinates.addAll(coordinatesComponent1);

        } else if (d2 == min) {
            reverse(coordinatesComponent1);
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent1);
            aggregatedCoordinates.addAll(coordinatesComponent2);

        } else if (d3 == min) {
            reverse(coordinatesComponent2);
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent1);
            aggregatedCoordinates.addAll(coordinatesComponent2);
        } else {
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent1);
            aggregatedCoordinates.addAll(coordinatesComponent2);
        }
        return aggregatedCoordinates;
    }

}
