/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.gridsuite.odre.server.utils.FileNameEnum;
import org.gridsuite.odre.server.utils.GeographicDataParser;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class OdreCsvClientImpl implements OdreClient, OdreCsvClient {
    @Override
    public List<SubstationGeoData> getSubstations() {
        return getSubstations(Paths.get(System.getenv("HOME") + "/GeoData/postes-electriques-rte.csv"));
    }

    @Override
    public List<LineGeoData> getLines() {
        return getLines(Paths.get(System.getenv("HOME") + "/GeoData/lignes-aeriennes-rte.csv"),
                Paths.get(System.getenv("HOME") + "/GeoData/lignes-souterraines-rte.csv"),
                Paths.get(System.getenv("HOME") + "/GeoData/postes-electriques-rte.csv")
            );
    }

    @Override
    public List<SubstationGeoData> getSubstationsFromCsv(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return new ArrayList<>(GeographicDataParser.parseSubstations(fileReader).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<LineGeoData> getLinesFromCsv(Map<String, MultipartFile> files) {
        try (BufferedReader aerialBufferedReader = new BufferedReader(new InputStreamReader(files.get(FileNameEnum.AERIAL_LINES.getValue()).getInputStream(), StandardCharsets.UTF_8));
             BufferedReader undergroundBufferedReader = new BufferedReader(new InputStreamReader(files.get(FileNameEnum.UNDERGROUND_LINES.getValue()).getInputStream(), StandardCharsets.UTF_8));
             BufferedReader substationBufferedReader = new BufferedReader(new InputStreamReader(files.get(FileNameEnum.SUBSTATIONS.getValue()).getInputStream(), StandardCharsets.UTF_8));
        ) {
            return new ArrayList<>(GeographicDataParser.parseLines(aerialBufferedReader, undergroundBufferedReader,
                    GeographicDataParser.parseSubstations(substationBufferedReader)).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<SubstationGeoData> getSubstations(Path path) {
        try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new ArrayList<>(GeographicDataParser.parseSubstations(bufferedReader).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<LineGeoData> getLines(Path aerialLinesFilePath, Path undergroundLinesFilePath, Path substationPath) {
        try (BufferedReader aerialBufferedReader = Files.newBufferedReader(aerialLinesFilePath, StandardCharsets.UTF_8);
            BufferedReader undergroundBufferedReader = Files.newBufferedReader(undergroundLinesFilePath, StandardCharsets.UTF_8);
            BufferedReader substationBufferedReader = Files.newBufferedReader(substationPath, StandardCharsets.UTF_8);
            ) {
            return new ArrayList<>(GeographicDataParser.parseLines(aerialBufferedReader, undergroundBufferedReader,
                GeographicDataParser.parseSubstations(substationBufferedReader)).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
