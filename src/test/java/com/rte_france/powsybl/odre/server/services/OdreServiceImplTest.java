/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server.services;

import com.powsybl.iidm.network.Country;
import com.rte_france.powsybl.odre.server.client.OdreClient;
import com.rte_france.powsybl.odre.server.dto.Coordinate;
import com.rte_france.powsybl.odre.server.dto.LineGeoData;
import com.rte_france.powsybl.odre.server.dto.SubstationGeoData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class OdreServiceImplTest {

    @Mock
    private OdreClient client;

    @Mock
    private RestTemplate geoDataServerRest;

    @InjectMocks
    private OdreService odreService =  new OdreServiceImpl("https://localhost:8080");

    @Before
    public void setUp() {
        List<SubstationGeoData> substationGeoData = new ArrayList<>();
        substationGeoData.add(new SubstationGeoData("substation1", Country.FR, new Coordinate(1, 2)));
        substationGeoData.add(new SubstationGeoData("substation2", Country.FR, new Coordinate(3, 4)));
        substationGeoData.add(new SubstationGeoData("substation3", Country.FR, new Coordinate(5, 6)));

        List<LineGeoData> lineGeoData = new ArrayList<>();
        lineGeoData.add(new LineGeoData("lines1", Country.FR, Country.FR,
                Arrays.asList(new Coordinate(2, 3), new Coordinate(3, 4))));

        lineGeoData.add(new LineGeoData("lines2", Country.FR, Country.BE,
                Arrays.asList(new Coordinate(1, 3), new Coordinate(5, 3))));

        lineGeoData.add(new LineGeoData("lines3", Country.FR, Country.GE,
                Arrays.asList(new Coordinate(4, 3), new Coordinate(2, 3), new Coordinate(7, 4))));

        Mockito.when(client.getSubstations())
                .thenReturn(substationGeoData);

        Mockito.when(client.getLines())
                .thenReturn(lineGeoData);
    }

    @Test
    public void test() {
        assertEquals(3, client.getSubstations().size());
        assertEquals(3, client.getLines().size());

        odreService.pushLines();
        odreService.pushSubstations();
    }
}
