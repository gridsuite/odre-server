/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.apache.commons.io.IOUtils;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@RunWith(SpringRunner.class)
public class OdreClientImplTest {

    @Mock
    private RestTemplate openDataRest;

    @Before
    public void setUp() throws IOException {

        byte[] aerialLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-aeriennes-rte-light.csv")));
        byte[] undergroundLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-souterraines-rte-light.csv")));
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte-light.csv")));

        given(openDataRest.exchange(
                eq("/explore/dataset/postes-electriques-rte/download/?format=csv"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class))).willReturn(new ResponseEntity<>(substationsBytes, HttpStatus.OK));

        given(openDataRest.exchange(
                eq("/explore/dataset/lignes-aeriennes-rte/download/?format=csv"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class))).willReturn(new ResponseEntity<>(aerialLinesBytes, HttpStatus.OK));

        given(openDataRest.exchange(
                eq("/explore/dataset/lignes-souterraines-rte/download/?format=csv"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class))).willReturn(new ResponseEntity<>(undergroundLinesBytes, HttpStatus.OK));
    }

    @Test
    public void testDownloadClientImpl() throws FileNotFoundException {

        OdreDownloadClientImpl odreOpenDataClientImpl = new OdreDownloadClientImpl();
        odreOpenDataClientImpl.setOpenDataRest(openDataRest);

        List<LineGeoData> linesGeoData = odreOpenDataClientImpl.getLines();

        List<SubstationGeoData> substationGeoData = odreOpenDataClientImpl.getSubstations();

        checkContent(linesGeoData, substationGeoData);
    }

    @Test
    public void testCSVClientImpl() throws FileNotFoundException {

        OdreCsvClientImpl odreCsvClient = new OdreCsvClientImpl();

        List<LineGeoData> linesGeoData = odreCsvClient.getLines(ResourceUtils.getFile("classpath:lignes-aeriennes-rte-light.csv").toPath(),
                ResourceUtils.getFile("classpath:lignes-souterraines-rte-light.csv").toPath());

        List<SubstationGeoData> substationGeoData = odreCsvClient.getSubstations(ResourceUtils.getFile("classpath:postes-electriques-rte-light.csv").toPath());

        checkContent(linesGeoData, substationGeoData);
    }

    private void checkContent(List<LineGeoData> linesGeoData, List<SubstationGeoData> substationGeoData) {
        assertEquals(7, linesGeoData.size());

        List<String> ids = linesGeoData.stream().map(LineGeoData::getId).collect(Collectors.toList());

        //aerial lines
        assertTrue(ids.contains("DINARL31PLAN6"));
        assertTrue(ids.contains("PREGUL41VAUX"));
        assertTrue(ids.contains("ARDOIL61MOTT5"));
        assertTrue(ids.contains("BEUVRL42GOSNA"));
        assertTrue(ids.contains("BELIEL31MASQU"));
        assertTrue(ids.contains("ROYANL41ZTHAI"));
        assertTrue(ids.contains("PLAN6L31RAN.P"));

        //discarded aerial lines
        assertFalse(ids.contains("COULOL31ZB.CH"));

        assertEquals(5, substationGeoData.size());

        List<String> ids2 = substationGeoData.stream().map(SubstationGeoData::getId).collect(Collectors.toList());

        assertTrue(ids2.contains("TREVI"));
        assertTrue(ids2.contains("NERAC"));
        assertTrue(ids2.contains("P.SEI"));
        assertTrue(ids2.contains("VALIN"));

        assertEquals(49.5000166667, substationGeoData.stream().filter(s -> s.getId().equals("CAZE5")).collect(Collectors.toList()).get(0).getCoordinate().getLat(), 0.001);
        assertEquals(1.25761944444, substationGeoData.stream().filter(s -> s.getId().equals("CAZE5")).collect(Collectors.toList()).get(0).getCoordinate().getLon(), 0.001);
    }
}
