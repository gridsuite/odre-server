/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import com.google.common.collect.Lists;
import com.powsybl.iidm.network.Country;
import org.gridsuite.odre.server.dto.Coordinate;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.gridsuite.odre.server.utils.DistanceCalculator;
import org.apache.commons.lang3.time.StopWatch;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Collections.min;
import static java.util.Collections.reverse;

/**
 * Parse RTE substation and line segment coordinates.
 * <ul>
 *     <li>
 *         <a href="https://opendata.reseaux-energies.fr/explore/dataset/postes-electriques-rte/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true">postes-electriques-rte.csv</a>
 *     </li>
 *     <li>
 *         <a href="https://opendata.reseaux-energies.fr/explore/dataset/lignes-aeriennes-rte/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true">lignes-aeriennes-rte.csv</a>
 *     </li>
 *     <li>
 *         <a href="https://opendata.reseaux-energies.fr/explore/dataset/lignes-souterraines-rte/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true">lignes-souterraines-rte.csv</a>
 *     </li>
 * </ul>
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@Component
public class OdreCsvClientImpl implements OdreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdreCsvClientImpl.class);

    private static final int THRESHOLD = 5;

    private static final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder('"', ';', System.lineSeparator())
            .build();

    static Map<String, SubstationGeoData> parseSubstations(Path path) {
        Map<String, SubstationGeoData> substations = new HashMap<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int substationCount = 0;

        try (CsvListReader csvReader = new CsvListReader(Files.newBufferedReader(path, StandardCharsets.UTF_8), CSV_PREFERENCE)) {
            // skip header
            csvReader.read();

            List<String> tokens;
            while ((tokens = csvReader.read()) != null) {
                String id = tokens.get(0);

                double lon = Double.parseDouble(tokens.get(5));
                double lat = Double.parseDouble(tokens.get(6));

                SubstationGeoData substation = substations.get(id);
                if (substation == null) {
                    SubstationGeoData substationGeoData = new SubstationGeoData(id, Country.FR, new Coordinate(lat, lon));
                    substations.put(id, substationGeoData);
                }

                substationCount++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("{} substations read from CSV file in {} ms", substationCount, stopWatch.getTime());

        return substations;
    }

    private static void parseLine(Map<String, UndirectedGraph<Coordinate, Object>> graphByLine,
                                  Path path, int lon1Index, int lat1Index, int lon2Index, int lat2Index) {
        try (CsvListReader csvReader = new CsvListReader(Files.newBufferedReader(path, StandardCharsets.UTF_8), CSV_PREFERENCE)) {
            // skip header
            csvReader.read();

            List<String> tokens;
            while ((tokens = csvReader.read()) != null) {
                String lineId = tokens.get(1);
                if (lineId.isEmpty()) {
                    continue;
                }
                double lon1 = Double.parseDouble(tokens.get(lon1Index));
                double lat1 = Double.parseDouble(tokens.get(lat1Index));
                double lon2 = Double.parseDouble(tokens.get(lon2Index));
                double lat2 = Double.parseDouble(tokens.get(lat2Index));
                Coordinate coordinate1 = new Coordinate(lat1, lon1);
                Coordinate coordinate2 = new Coordinate(lat2, lon2);
                UndirectedGraph<Coordinate, Object> graph = graphByLine.get(lineId);
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, LineGeoData> parseLines(Path aerialLinesFilePath, Path undergroundLinesFilePath) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Map<String, UndirectedGraph<Coordinate, Object>> graphByLine = new HashMap<>();

        parseLine(graphByLine, aerialLinesFilePath, 8, 9, 10, 11);
        parseLine(graphByLine, undergroundLinesFilePath, 9, 10, 11, 12);

        Map<String, LineGeoData> lines = new HashMap<>();

        int linesWithOneConnectedSet = 0;
        int linesWithTwoOrMoreConnectedSets = 0;

        int oneConnectedSetDiscarded = 0;
        int twoOrMoreConnectedSetsDiscarded = 0;

        for (Map.Entry<String, UndirectedGraph<Coordinate, Object>> e : graphByLine.entrySet()) {
            String lineId = e.getKey();
            UndirectedGraph<Coordinate, Object> graph = e.getValue();
            List<Set<Coordinate>> connectedSets = new ConnectivityInspector<>(graph).connectedSets();
            if (connectedSets.size() == 1) {
                linesWithOneConnectedSet++;
                List<Coordinate> ends = getEnds(connectedSets.get(0), graph);

                if (ends.size() == 2) {
                    List<Coordinate> coordinates = Lists.newArrayList(new BreadthFirstIterator<>(graph, ends.get(0)));
                    LineGeoData line = new LineGeoData(lineId, Country.FR, Country.FR, coordinates);
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
                LineGeoData line = new LineGeoData(lineId, Country.FR, Country.FR, aggregatedCoordinates);
                lines.put(lineId, line);
            }
        }

        LOGGER.info("{} lines read from CSV file in {} ms", lines.size(), stopWatch.getTime());
        LOGGER.info("{} lines have one Connected set, {} of them were discarded", linesWithOneConnectedSet, oneConnectedSetDiscarded);
        LOGGER.info("{} lines have two or more Connected sets, {} of them were discarded", linesWithTwoOrMoreConnectedSets, twoOrMoreConnectedSetsDiscarded);

        if (graphByLine.size() != lines.size()) {
            LOGGER.warn("Total discarded lines : {}/{} ",
                    graphByLine.size() - lines.size(), graphByLine.size());
        }

        return lines;
    }

    private static List<Coordinate> getEnds(Set<Coordinate> connectedSet, UndirectedGraph<Coordinate, Object> graph) {
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
                coordinatesComponent.get(coordinatesComponent.size() - 1).getLat(), coordinatesComponent.get(coordinatesComponent.size() - 1).getLon(), "M");
    }

    private static List<Coordinate> aggregateCoordinates(List<List<Coordinate>> coordinatesComponents) {
        coordinatesComponents.sort((comp1, comp2) -> (int) (getBranchLength(comp2) - getBranchLength(comp1)));
        return aggregateCoordinates(coordinatesComponents.get(0), coordinatesComponents.get(1));
    }

    private static List<Coordinate> aggregateCoordinates(List<Coordinate> coordinatesComponent1, List<Coordinate> coordinatesComponent2) {
        List<Coordinate> aggregatedCoordinates;

        double l1 = getBranchLength(coordinatesComponent1);
        double l2 = getBranchLength(coordinatesComponent2);

        if (100 * l1 / l2 < THRESHOLD) {
            return coordinatesComponent2;
        } else if (100 * l2 / l1 < THRESHOLD) {
            return coordinatesComponent1;
        }

        double d1 = DistanceCalculator.distance(coordinatesComponent1.get(0).getLat(), coordinatesComponent1.get(0).getLon(),
                coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLat(), coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLon(),
                "M");

        double d2 = DistanceCalculator.distance(coordinatesComponent1.get(0).getLat(), coordinatesComponent1.get(0).getLon(),
                coordinatesComponent2.get(0).getLat(), coordinatesComponent2.get(0).getLon(),
                "M");

        double d3 = DistanceCalculator.distance(coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLat(), coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLon(),
                coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLat(), coordinatesComponent2.get(coordinatesComponent2.size() - 1).getLon(),
                "M");

        double d4 = DistanceCalculator.distance(coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLat(), coordinatesComponent1.get(coordinatesComponent1.size() - 1).getLon(),
                coordinatesComponent2.get(0).getLat(), coordinatesComponent2.get(0).getLon(),
                "M");

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

    @Override
    public List<SubstationGeoData> getSubstations() {
        return getSubstations(Paths.get(System.getenv("HOME") + "/GeoData/postes-electriques-rte.csv"));
    }

    @Override
    public List<SubstationGeoData> getSubstations(Path path) {
        return new ArrayList<>(parseSubstations(path).values());
    }

    @Override
    public List<LineGeoData> getLines() {
        return getLines(Paths.get(System.getenv("HOME") + "/GeoData/lignes-aeriennes-rte.csv"),
                Paths.get(System.getenv("HOME") + "/GeoData/lignes-souterraines-rte.csv"));
    }

    @Override
    public List<LineGeoData> getLines(Path aerialLinesFilePath, Path undergroundLinesFilePath) {
        return new ArrayList<>(parseLines(aerialLinesFilePath, undergroundLinesFilePath).values());
    }
}
