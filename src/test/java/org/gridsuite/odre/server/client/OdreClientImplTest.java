/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.apache.commons.io.IOUtils;
import org.gridsuite.odre.server.dto.Coordinate;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.gridsuite.odre.server.utils.GeographicDataParser;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                ResourceUtils.getFile("classpath:lignes-souterraines-rte-light.csv").toPath(),
                ResourceUtils.getFile("classpath:postes-electriques-rte-light.csv").toPath()
            );

        List<SubstationGeoData> substationGeoData = odreCsvClient.getSubstations(ResourceUtils.getFile("classpath:postes-electriques-rte-light.csv").toPath());

        checkContent(linesGeoData, substationGeoData);
    }

    private void checkContent(List<LineGeoData> linesGeoData, List<SubstationGeoData> substationGeoData) {
        assertEquals(5, linesGeoData.size());

        List<String> ids = linesGeoData.stream().map(LineGeoData::getId).collect(Collectors.toList());

        //aerial lines
        assertTrue(ids.contains("DINARL31PLAN6"));
        assertTrue(ids.contains("PREGUL41VAUX"));
        assertTrue(ids.contains("ARDOIL61MOTT5"));
        assertTrue(ids.contains("BEUVRL42GOSNA"));
        assertTrue(ids.contains("BELIEL31MASQU"));

        //discarded aerial lines
        assertFalse(ids.contains("COULOL31ZB"));

        assertEquals(5, substationGeoData.size());

        List<String> ids2 = substationGeoData.stream().map(SubstationGeoData::getId).collect(Collectors.toList());

        assertTrue(ids2.contains("TREVI"));
        assertTrue(ids2.contains("NERAC"));
        assertTrue(ids2.contains("P.SEI"));
        assertTrue(ids2.contains("VALIN"));

        assertEquals(49.5000166667, substationGeoData.stream().filter(s -> s.getId().equals("CAZE5")).collect(Collectors.toList()).get(0).getCoordinate().getLat(), 0.001);
        assertEquals(1.25761944444, substationGeoData.stream().filter(s -> s.getId().equals("CAZE5")).collect(Collectors.toList()).get(0).getCoordinate().getLon(), 0.001);
    }

    @Test
    public void testFindSubstationStart() {
        List<Coordinate> refList = List.of(new Coordinate(1, 1), new Coordinate(1, 5), new Coordinate(5, 1), new Coordinate(5, 5));
        List<Coordinate> reverseRef = new ArrayList<>(refList);
        Collections.reverse(reverseRef);
        Map<String, SubstationGeoData> substations = new HashMap<>();
        substations.put("CAIN", new SubstationGeoData("CAIN", "FR", new Coordinate(0, 1)));
        substations.put("RAMBO", new SubstationGeoData("RAMBO", "FR", new Coordinate(6, 5)));

        List<Coordinate> tmpList = new ArrayList<>(refList);
        String res = GeographicDataParser.findSubstationStart(substations, "CAIN  Z4RAMBO", tmpList);
        assertEquals("CAIN", res);
        assertEquals(refList, tmpList);

        res = GeographicDataParser.findSubstationStart(substations, "RAMBO Z4CAIN ", tmpList);
        assertEquals("CAIN", res);
        assertEquals(refList, tmpList);

        Collections.reverse(tmpList);
        /* RAMBO is now closer than CAIN */
        res = GeographicDataParser.findSubstationStart(substations, "CAIN  Z4RAMBO", tmpList);
        assertEquals("RAMBO", res);
        assertEquals(reverseRef, tmpList); // we have reversed tmp list

        // now the fun : missing substations :
        res = GeographicDataParser.findSubstationStart(substations, "MCCAIN__JOHN", tmpList);
        assertEquals("", res);
        assertEquals(reverseRef, tmpList);

        res = GeographicDataParser.findSubstationStart(substations, "JOHN  Z4RAMBO", tmpList);
        assertEquals("RAMBO", res);
        assertEquals(reverseRef, tmpList); // we have reversed tmp list

        res = GeographicDataParser.findSubstationStart(substations, "CAIN  Z4JOHN", tmpList);
        assertEquals("CAIN", res);
        assertEquals(refList, tmpList); // we have reversed tmp list

    }
}
