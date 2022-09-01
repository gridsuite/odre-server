/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
public final class FileValidator {

    private FileValidator() {

    }

    private static final String HEADERS_OF_FILE_HAS_CHANGED = "Invalid file, Headers of file {} has changed, header(s) not found: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidator.class);
    static final String COUNTRY_FR = "FR";
    static FileValidator instance;
    static final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder('"', ';', System.lineSeparator()).build();
    static final String TYPE = "text/csv";
    public static final String CODE_LIGNE_1 = "Code ligne 1";
    public static final String CODE_LIGNE_2 = "Code ligne 2";
    public static final String CODE_LIGNE_3 = "Code ligne 3";
    public static final String CODE_LIGNE_4 = "Code ligne 4";
    public static final String CODE_LIGNE_5 = "Code ligne 5";
    static final Map<String, String> IDS_COLUMNS_NAME = new HashMap<>(
            Map.of("id1", CODE_LIGNE_1, "id2", CODE_LIGNE_2, "id3", CODE_LIGNE_3, "id4", CODE_LIGNE_4, "id5", CODE_LIGNE_5));
    private static final String LONGITUDE_DEBUT_SEGMENT_DD = "Longitude début segment (DD)";
    private static final String LATITUDE_DEBUT_SEGMENT_DD = "Latitude début segment (DD)";
    private static final String LONGITUDE_ARRIVEE_SEGMENT_DD = "Longitude arrivée segment (DD)";
    private static final String LATITUDE_ARRIVEE_SEGMENT_DD = "Latitude arrivée segment (DD)";
    static final Map<String, String> LONG_LAT_COLUMNS_NAME = new HashMap<>(
            Map.of("long1", LONGITUDE_DEBUT_SEGMENT_DD, "lat1", LATITUDE_DEBUT_SEGMENT_DD, "long2", LONGITUDE_ARRIVEE_SEGMENT_DD, "lat2", LATITUDE_ARRIVEE_SEGMENT_DD));
    static final String CODE_POSTE = "Code poste";
    static final String LONGITUDE_POSTE_DD = "Longitude poste (DD)";
    static final String LATITUDE_POSTE_DD = "Latitude poste (DD)";
    private static final List<String> SUBSTATIONS_EXPECTED_HEADERS = Arrays.asList(CODE_POSTE, LONGITUDE_POSTE_DD, LATITUDE_POSTE_DD);
    private static final List<String> AERIAL_LINES_EXPECTED_HEADERS = Arrays.asList(CODE_LIGNE_1, CODE_LIGNE_2, CODE_LIGNE_3, CODE_LIGNE_4, CODE_LIGNE_5, LONGITUDE_DEBUT_SEGMENT_DD, LATITUDE_DEBUT_SEGMENT_DD, LONGITUDE_ARRIVEE_SEGMENT_DD, LATITUDE_ARRIVEE_SEGMENT_DD);
    private static final List<String> UNDERGROUND_LINES_EXPECTED_HEADERS = Arrays.asList(CODE_LIGNE_1, CODE_LIGNE_2, CODE_LIGNE_3, CODE_LIGNE_4, CODE_LIGNE_5, LONGITUDE_DEBUT_SEGMENT_DD, LATITUDE_DEBUT_SEGMENT_DD, LONGITUDE_ARRIVEE_SEGMENT_DD, LATITUDE_ARRIVEE_SEGMENT_DD);

    public static FileValidator getInstance() {
        if (instance == null) {
            instance = new FileValidator();
        }
        return instance;
    }

    public static boolean validateSubstations(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CsvMapReader mapReader = new CsvMapReader(fileReader, CSV_PREFERENCE);) {
            final String[] headers = mapReader.getHeader(true);
            if (new HashSet<>(Arrays.asList(headers)).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
                return true;
            } else {
                List<String> notFoundHeaders = SUBSTATIONS_EXPECTED_HEADERS.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
                String fileName = file.getOriginalFilename();
                LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, fileName, notFoundHeaders);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return false;
    }

    public static Map<String, BufferedReader> validateLines(List<MultipartFile> files) {
        Map<String, BufferedReader> mapResult = new HashMap<>();
        files.forEach(file -> {
            try (CsvMapReader mapReader = new CsvMapReader(new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)), CSV_PREFERENCE);) {
                final String[] headers = mapReader.getHeader(true);
                Map<String, String> row = mapReader.read(headers);
                String typeOuvrage = row.get("Type ouvrage");
                switch ((typeOuvrage != null) ? typeOuvrage : "NULL") {
                    case "NULL":
                        getIfSubstationsOrLogError(mapResult, file, headers, typeOuvrage);
                        break;
                    case "AERIEN":
                        getResultOrLogError(headers, AERIAL_LINES_EXPECTED_HEADERS, mapResult, FileTypeEnum.AERIAL_LINES, file);
                        break;
                    case "SOUTERRAIN":
                        getResultOrLogError(headers, UNDERGROUND_LINES_EXPECTED_HEADERS, mapResult, FileTypeEnum.UNDERGROUND_LINES, file);
                        break;
                    default:
                        LOGGER.error("The file {} has no known equipment type : {}", file.getOriginalFilename(), typeOuvrage);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return mapResult;
    }

    private static void getIfSubstationsOrLogError(Map<String, BufferedReader> mapResult, MultipartFile file, String[] headers, String typeOuvrage) throws IOException {
        if (new HashSet<>(Arrays.asList(headers)).containsAll(SUBSTATIONS_EXPECTED_HEADERS)) {
            mapResult.putIfAbsent(FileTypeEnum.SUBSTATIONS.getValue(), new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)));
        } else if (isAerealOrUnderground(headers)) {
            LOGGER.error("The file {} has no equipment type : {}", file.getOriginalFilename(), typeOuvrage);
        } else {
            List<String> notFoundHeaders = SUBSTATIONS_EXPECTED_HEADERS.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
            String fileName = file.getOriginalFilename();
            LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, fileName, notFoundHeaders);
        }
    }

    private static Predicate<String> isChangedHeaders(String[] headers) {
        return h -> !Arrays.asList(headers).contains(h);
    }

    private static boolean isAerealOrUnderground(String[] headers) {
        return new HashSet<>(Arrays.asList(headers)).containsAll(AERIAL_LINES_EXPECTED_HEADERS) ||
                new HashSet<>(Arrays.asList(headers)).containsAll(UNDERGROUND_LINES_EXPECTED_HEADERS);
    }

    private static void getResultOrLogError(String[] headers, List<String> expectedHeaders, Map<String, BufferedReader> mapResult, FileTypeEnum fileType, MultipartFile file) throws IOException {
        if (new HashSet<>(Arrays.asList(headers)).containsAll(expectedHeaders)) {
            mapResult.putIfAbsent(fileType.getValue(), new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)));
        } else {
            List<String> notFoundHeaders = expectedHeaders.stream().filter(isChangedHeaders(headers)).collect(Collectors.toList());
            String fileName = file.getOriginalFilename();
            LOGGER.error(HEADERS_OF_FILE_HAS_CHANGED, fileName, notFoundHeaders);
        }
    }

    public static boolean hasCSVFormat(MultipartFile file) {
        if (FileValidator.TYPE.equals(file.getContentType())) {
            return true;
        }
        LOGGER.error("The file {} is not in format {}", file.getOriginalFilename(), TYPE);
        return false;
    }
}

