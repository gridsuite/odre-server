/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.services;

import org.gridsuite.odre.server.client.OdreClient;
import org.gridsuite.odre.server.dto.Coordinate;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
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
        substationGeoData.add(new SubstationGeoData("substation1", "FR", new Coordinate(1, 2)));
        substationGeoData.add(new SubstationGeoData("substation2", "FR", new Coordinate(3, 4)));
        substationGeoData.add(new SubstationGeoData("substation3", "FR", new Coordinate(5, 6)));

        List<LineGeoData> lineGeoData = new ArrayList<>();
        lineGeoData.add(new LineGeoData("lines1", "FR", "FR",
                Arrays.asList(new Coordinate(2, 3), new Coordinate(3, 4))));

        lineGeoData.add(new LineGeoData("lines2", "FR", "BE",
                Arrays.asList(new Coordinate(1, 3), new Coordinate(5, 3))));

        lineGeoData.add(new LineGeoData("lines3", "FR", "GE",
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
