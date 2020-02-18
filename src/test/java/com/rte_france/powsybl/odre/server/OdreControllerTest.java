/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server;

import com.rte_france.powsybl.odre.server.services.OdreService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
        mvc.perform(post("/" + OdreController.API_VERSION + "/substations")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(post("/" + OdreController.API_VERSION + "/lines")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
