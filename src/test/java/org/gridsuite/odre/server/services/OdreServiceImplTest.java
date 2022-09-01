/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.services;

import org.apache.commons.io.IOUtils;
import org.gridsuite.odre.server.client.OdreClient;
import org.gridsuite.odre.server.client.OdreCsvClient;
import org.gridsuite.odre.server.client.OdreCsvClientImpl;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
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
    private OdreCsvClient csvClient;

    @Mock
    private RestTemplate geoDataServerRest;

    @InjectMocks
    private OdreService odreService =  new OdreServiceImpl("https://localhost:8080");

    @InjectMocks
    private OdreCsvClientImpl odreCsvClientImpl =  new OdreCsvClientImpl();

    @Before
    public void setUp() {
        List<SubstationGeoData> substationGeoData = new ArrayList<>();
        substationGeoData.add(new SubstationGeoData("substation1", "FR", new Coordinate(1, 2)));
        substationGeoData.add(new SubstationGeoData("substation2", "FR", new Coordinate(3, 4)));
        substationGeoData.add(new SubstationGeoData("substation3", "FR", new Coordinate(5, 6)));

        List<LineGeoData> lineGeoData = new ArrayList<>();
        lineGeoData.add(new LineGeoData("lines1", "FR", "FR", "substation1", "substation2",
                Arrays.asList(new Coordinate(2, 3), new Coordinate(3, 4))));

        lineGeoData.add(new LineGeoData("lines2", "FR", "BE", "substation1", "substation2",
                Arrays.asList(new Coordinate(1, 3), new Coordinate(5, 3))));

        lineGeoData.add(new LineGeoData("lines3", "FR", "GE", "substation2", "substation2",
                Arrays.asList(new Coordinate(4, 3), new Coordinate(2, 3), new Coordinate(7, 4))));

        Mockito.when(client.getSubstations())
                .thenReturn(substationGeoData);

        Mockito.when(client.getLines())
                .thenReturn(lineGeoData);
    }

    @Test
    public void test() throws Exception {
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte.csv")));
        byte[] aerialLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-aeriennes-rte.csv")));
        byte[] undergroundLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-souterraines-rte.csv")));
        byte[] invalideSubstationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte-invalide.csv")));
        MockMultipartFile invalidFile = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", invalideSubstationsBytes);

        MockMultipartFile substationsFile = new MockMultipartFile("files", "postes-electriques-rte.csv", "text/csv", substationsBytes);
        MockMultipartFile aerialLinesFile = new MockMultipartFile("files", "lignes-aeriennes-rte.csv", "text/csv", aerialLinesBytes);
        MockMultipartFile undergroundLinesFile = new MockMultipartFile("files", "lignes-souterraines-rte.csv", "text/csv", undergroundLinesBytes);
        MockMultipartFile file = new MockMultipartFile("file", "postes-electriques-rte.csv", "text/csv", substationsBytes);

        assertEquals(3, client.getSubstations().size());
        assertEquals(3, client.getLines().size());

        assertEquals(10, odreCsvClientImpl.getSubstationsFromCsv(file).size());
        assertEquals(6, odreCsvClientImpl.getLinesFromCsv(List.of(substationsFile, aerialLinesFile, undergroundLinesFile)).size());

        // test with invalid file
        assertEquals(0, odreCsvClientImpl.getLinesFromCsv(List.of(invalidFile, aerialLinesFile, undergroundLinesFile)).size());
        assertEquals(0, odreCsvClientImpl.getSubstationsFromCsv(invalidFile).size());

        odreService.pushLines();
        odreService.pushSubstations();
        odreService.pushSubstationsFromCsv(file);
        odreService.pushLinesFromCsv(List.of(substationsFile, aerialLinesFile, undergroundLinesFile));
    }
}
