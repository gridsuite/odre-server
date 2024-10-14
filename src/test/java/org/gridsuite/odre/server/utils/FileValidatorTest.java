/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.utils;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class FileValidatorTest {

    @Test
    void whenCallingValidate() throws IOException {
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte.csv")));
        byte[] aerialLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-aeriennes-rte.csv")));
        byte[] undergroundLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-souterraines-rte.csv")));
        byte[] invalideSubstationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte-invalide.csv")));

        MockMultipartFile file = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile aerialLinesFile = new MockMultipartFile("files", "lignes-aeriennes-rte.csv", "text/csv", aerialLinesBytes);
        MockMultipartFile undergroundLinesFile = new MockMultipartFile("files", "lignes-souterraines-rte.csv", "text/csv", undergroundLinesBytes);
        MockMultipartFile substationsFile = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile invalidFile = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", invalideSubstationsBytes);

        // test substations file validator with valid file
        Assertions.assertThat(FileValidator.validateSubstations(file)).isTrue();
        // test substations file validator with invalid file
        Assertions.assertThat(FileValidator.validateSubstations(invalidFile)).isFalse();
        // test lines file validator with valid files
        Assertions.assertThat(FileValidator.validateLines(List.of(substationsFile, aerialLinesFile, undergroundLinesFile))).hasSize(3);
        // test lines file validator with 1 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(substationsFile, invalidFile, undergroundLinesFile))).hasSize(2);
        // test lines file validator with 2 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(invalidFile, invalidFile, undergroundLinesFile))).hasSize(1);
        // test lines file validator with 3 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(invalidFile, invalidFile, invalidFile))).isEmpty();
        // test lines file validator with 4 invalid file
        Assertions.assertThat(FileValidator.validateLines(List.of(invalidFile, invalidFile, invalidFile, invalidFile))).isEmpty();
    }

    @Test
    void hasCSVFormatTest() throws IOException {
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte.csv")));

        MockMultipartFile file = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile invalidTypeFile = new MockMultipartFile("file", "postes-electriques-rte.pdf", "text/pdf", substationsBytes);

        assertTrue(FileValidator.hasCSVFormat(file));
        assertFalse(FileValidator.hasCSVFormat(invalidTypeFile));
    }
}
