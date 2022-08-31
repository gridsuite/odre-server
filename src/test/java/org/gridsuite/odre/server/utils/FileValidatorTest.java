/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.utils;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class FileValidatorTest {

    @Test
    public void whenValideSubstationFile() throws IOException {
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte.csv")));
        byte[] aerialLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-aeriennes-rte.csv")));
        byte[] undergroundLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-souterraines-rte.csv")));
        byte[] invalideSubstationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte-invalide.csv")));

        MockMultipartFile file = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile aerialLinesFile = new MockMultipartFile("files", "lignes-aeriennes-rte.csv", "text/csv", aerialLinesBytes);
        MockMultipartFile undergroundLinesFile = new MockMultipartFile("files", "lignes-souterraines-rte.csv", "text/csv", undergroundLinesBytes);
        MockMultipartFile substationsFile = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile invalidFile = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", invalideSubstationsBytes);

        Map<String, BufferedReader> result = new HashMap<>();
        result.putIfAbsent(FileNameEnum.SUBSTATIONS.getValue(), new BufferedReader(new InputStreamReader(substationsFile.getInputStream(), StandardCharsets.UTF_8)));
        result.putIfAbsent(FileNameEnum.AERIAL_LINES.getValue(), new BufferedReader(new InputStreamReader(aerialLinesFile.getInputStream(), StandardCharsets.UTF_8)));
        result.putIfAbsent(FileNameEnum.UNDERGROUND_LINES.getValue(), new BufferedReader(new InputStreamReader(undergroundLinesFile.getInputStream(), StandardCharsets.UTF_8)));

        // test substations file validator with valid file
        Assertions.assertThat(FileValidator.getInstance().validateSubstations(file)).isEqualTo(true);
        // test substations file validator with invalid file
        Assertions.assertThat(FileValidator.getInstance().validateSubstations(invalidFile)).isEqualTo(false);
        // test lines file validator with valid files
        Assertions.assertThat(FileValidator.getInstance().validateLines(List.of(substationsFile, aerialLinesFile, undergroundLinesFile)).size()).isEqualTo(3);
        // test lines file validator with 1 invalid file
        Assertions.assertThat(FileValidator.getInstance().validateLines(List.of(substationsFile, invalidFile, undergroundLinesFile)).size()).isEqualTo(2);
        // test lines file validator with 2 invalid file
        Assertions.assertThat(FileValidator.getInstance().validateLines(List.of(invalidFile, invalidFile, undergroundLinesFile)).size()).isEqualTo(1);
        // test lines file validator with 3 invalid file
        Assertions.assertThat(FileValidator.getInstance().validateLines(List.of(invalidFile, invalidFile, invalidFile)).size()).isEqualTo(0);
        // test lines file validator with 4 invalid file
        Assertions.assertThat(FileValidator.getInstance().validateLines(List.of(invalidFile, invalidFile, invalidFile, invalidFile)).size()).isEqualTo(0);
    }

    @Test
    public void hasCSVFormatTest() throws IOException {
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte.csv")));

        MockMultipartFile file = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile invalidTypeFile = new MockMultipartFile("file", "postes-electriques-rte.pdf", "text/pdf", substationsBytes);

        assertTrue(FileValidator.hasCSVFormat(file));
        assertFalse(FileValidator.hasCSVFormat(invalidTypeFile));
    }
}
