/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
public final class FileValidator {

    private static FileValidator instance;

    private FileValidator() {

    }

    public static FileValidator getInstance() {
        if (instance == null) {
            instance = new FileValidator();
        }
        return instance;
    }

    public static final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder('"', ';', System.lineSeparator()).build();
    public static final String TYPE = "text/csv";
    protected static final Map<String, String> IDS_COLUMNS_NAME = new HashMap<>(
            Map.of("id1", "Code ligne 1", "id2", "Code ligne 2", "id3", "Code ligne 3", "id4", "Code ligne 4", "id5", "Code ligne 5"));
    public static final String LONGITUDE_DEBUT_SEGMENT_DD = "Longitude début segment (DD)";
    public static final String LATITUDE_DEBUT_SEGMENT_DD = "Latitude début segment (DD)";
    public static final String LONGITUDE_ARRIVEE_SEGMENT_DD = "Longitude arrivée segment (DD)";
    public static final String LATITUDE_ARRIVEE_SEGMENT_DD = "Latitude arrivée segment (DD)";
    protected static final Map<String, String> LONG_LAT_COLUMNS_NAME = new HashMap<>(
            Map.of("long1", LONGITUDE_DEBUT_SEGMENT_DD, "lat1", LATITUDE_DEBUT_SEGMENT_DD, "long2", LONGITUDE_ARRIVEE_SEGMENT_DD, "lat2", LATITUDE_ARRIVEE_SEGMENT_DD));
    protected static final List<String> SUBSTATIONS_EXPECTED_HEADERS = Arrays.asList("Code poste", "Longitude poste (DD)", "Latitude poste (DD)");
    protected static final List<String> ARIAL_LINES_EXPECTED_HEADERS = Arrays.asList(LONGITUDE_DEBUT_SEGMENT_DD, LATITUDE_DEBUT_SEGMENT_DD, LONGITUDE_ARRIVEE_SEGMENT_DD, LATITUDE_ARRIVEE_SEGMENT_DD);
    protected static final List<String> UNDERGROUND_LINES_EXPECTED_HEADERS = Arrays.asList(LONGITUDE_DEBUT_SEGMENT_DD, LATITUDE_DEBUT_SEGMENT_DD, LONGITUDE_ARRIVEE_SEGMENT_DD, LATITUDE_ARRIVEE_SEGMENT_DD);

    public static boolean validateSubstations(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CsvMapReader mapReader = new CsvMapReader(fileReader, CSV_PREFERENCE);) {
            final String[] headers = mapReader.getHeader(true);
            Map<String, String> row = mapReader.read(headers);
            String typeOuvrage = row.get("Type ouvrage");
            if (typeOuvrage == null && new HashSet<>(Arrays.asList(headers)).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static Map<String, BufferedReader> validateLines(List<MultipartFile> files) {
        Map<String, BufferedReader> mapResult = new HashMap<>();
        files.stream().forEach(file -> {
            try (CsvMapReader mapReader = new CsvMapReader(new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)), CSV_PREFERENCE);) {
                final String[] headers = mapReader.getHeader(true);
                Map<String, String> row = mapReader.read(headers);
                String typeOuvrage = row.get("Type ouvrage");
                if (typeOuvrage == null && new HashSet<>(Arrays.asList(headers)).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
                    mapResult.putIfAbsent(FileNameEnum.SUBSTATIONS.getValue(), new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)));
                } else if (StringUtils.equals(typeOuvrage, "AERIEN") && new HashSet<>(Arrays.asList(headers)).containsAll(ARIAL_LINES_EXPECTED_HEADERS)) {
                    mapResult.putIfAbsent(FileNameEnum.AERIAL_LINES.getValue(), new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)));
                } else if (StringUtils.equals(typeOuvrage, "SOUTERRAIN") && new HashSet<>(Arrays.asList(headers)).containsAll(UNDERGROUND_LINES_EXPECTED_HEADERS)) {
                    mapResult.putIfAbsent(FileNameEnum.UNDERGROUND_LINES.getValue(), new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return mapResult;
    }

    public static boolean hasCSVFormat(MultipartFile file) {
        return FileValidator.TYPE.equals(file.getContentType());
    }
}
