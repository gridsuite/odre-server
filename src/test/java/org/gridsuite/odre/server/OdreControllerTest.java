/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server;

import org.apache.commons.io.IOUtils;
import org.gridsuite.odre.server.services.OdreService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(OdreController.class)
@ContextConfiguration(classes = {OdreApplication.class, OdreSwaggerConfig.class})
public class OdreControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OdreService odreService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() throws Exception {
        byte[] aerialLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-aeriennes-rte.csv")));
        byte[] undergroundLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-souterraines-rte.csv")));
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte.csv")));

        MockMultipartFile file = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile substationsFile = new MockMultipartFile("files", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile aerialLinesFile = new MockMultipartFile("files", "lignes-aeriennes-rte.csv", "text/csv", aerialLinesBytes);
        MockMultipartFile undergroundLinesFile = new MockMultipartFile("files", "lignes-souterraines-rte.csv", "text/csv", undergroundLinesBytes);
        MockMultipartFile fileWithInvalidName = new MockMultipartFile("file", "postes-electriques.csv", "text/csv", substationsBytes);
        MockMultipartFile substationsFileWithInvalidName = new MockMultipartFile("files", "postes-electriques.csv", "text/csv", substationsBytes);
        MockMultipartFile aerialLinesFileWithInvalidName = new MockMultipartFile("files", "lignes-aeriennes.csv", "text/csv", aerialLinesBytes);
        MockMultipartFile undergroundLinesFileWithInvalidName = new MockMultipartFile("files", "lignes-souterraines.csv", "text/csv", undergroundLinesBytes);

        mvc.perform(post("/" + OdreController.API_VERSION + "/substations")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(post("/" + OdreController.API_VERSION + "/lines")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(multipart("/" + OdreController.API_VERSION + "/substations/upload").file(file))
                .andExpect(status().isOk());

        mvc.perform(multipart("/" + OdreController.API_VERSION + "/lines/upload").file(substationsFile).file(aerialLinesFile).file(undergroundLinesFile))
                .andExpect(status().isOk());

        // test with invalid file name
        mvc.perform(multipart("/" + OdreController.API_VERSION + "/substations/upload").file(fileWithInvalidName))
                .andExpect(status().isBadRequest());
        // test with invalid file name
        mvc.perform(multipart("/" + OdreController.API_VERSION + "/lines/upload").file(substationsFileWithInvalidName).file(aerialLinesFileWithInvalidName).file(undergroundLinesFileWithInvalidName))
                .andExpect(status().isBadRequest());
    }
}
