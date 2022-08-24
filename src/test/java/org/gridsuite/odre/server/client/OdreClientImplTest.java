/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gridsuite.odre.server.dto.Coordinate;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.gridsuite.odre.server.utils.GeographicDataParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private RestTemplate openDataRest;

    @Before
    public void setUp() throws IOException {
        byte[] aerialLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-aeriennes-rte.csv")));
        byte[] undergroundLinesBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:lignes-souterraines-rte.csv")));
        byte[] substationsBytes = IOUtils.toByteArray(new FileInputStream(ResourceUtils.getFile("classpath:postes-electriques-rte.csv")));

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

        List<LineGeoData> linesGeoData = odreCsvClient.getLines(ResourceUtils.getFile("classpath:lignes-aeriennes-rte.csv").toPath(),
                ResourceUtils.getFile("classpath:lignes-souterraines-rte.csv").toPath(),
                ResourceUtils.getFile("classpath:postes-electriques-rte.csv").toPath()
            );

        List<SubstationGeoData> substationGeoData = odreCsvClient.getSubstations(ResourceUtils.getFile("classpath:postes-electriques-rte.csv").toPath());

        checkContent(linesGeoData, substationGeoData);
    }

    private void checkContent(List<LineGeoData> linesGeoData, List<SubstationGeoData> substationGeoData) {
        assertEquals(6, linesGeoData.size());

        List<String> ids = linesGeoData.stream().map(LineGeoData::getId).collect(Collectors.toList());

        //aerial lines
        assertTrue(ids.contains("ARGOEL71MANDA"));
        assertTrue(ids.contains("COGNAL41JARNA"));
        assertTrue(ids.contains("FERRIL31ZMERC"));
        assertTrue(ids.contains("MOHONL31P.TER"));
        assertTrue(ids.contains("PALUNL31ROUS5"));

        assertEquals(10, substationGeoData.size());

        List<String> ids2 = substationGeoData.stream().map(SubstationGeoData::getId).collect(Collectors.toList());

        assertTrue(ids2.contains("V.SEP"));
        assertTrue(ids2.contains("V.POR"));
        assertTrue(ids2.contains("MONDI"));
        assertTrue(ids2.contains("B.THO"));
        assertTrue(ids2.contains("ZV.BE"));
        assertTrue(ids2.contains("1AVAL"));
        assertTrue(ids2.contains("1LART"));
        assertTrue(ids2.contains("1ONER"));
        assertTrue(ids2.contains("1SSFO"));
        assertTrue(ids2.contains("A.ADO"));

        assertEquals(49.35864164277698, substationGeoData.stream().filter(s -> s.getId().equals("V.SEP")).collect(Collectors.toList()).get(0).getCoordinate().getLat(), 0.001);
        assertEquals(2.2014874715868253, substationGeoData.stream().filter(s -> s.getId().equals("V.SEP")).collect(Collectors.toList()).get(0).getCoordinate().getLon(), 0.001);
    }

    @Test
    public void testFindSubstationStart() {
        List<Coordinate> refList = List.of(new Coordinate(1, 1), new Coordinate(1, 5), new Coordinate(5, 1), new Coordinate(5, 5));
        List<Coordinate> halfLine = List.of(new Coordinate(1, 1), new Coordinate(1, 3));
        Map<String, SubstationGeoData> substations = new HashMap<>();
        substations.put("CAIN", new SubstationGeoData("CAIN", "FR", new Coordinate(0, 1)));
        substations.put("RAMBO", new SubstationGeoData("RAMBO", "FR", new Coordinate(6, 5)));

        List<Coordinate> tmpList = new ArrayList<>(refList);
        Pair<String, String> res = GeographicDataParser.substationOrder(substations, "CAIN  Z4RAMBO", tmpList);
        assertEquals(Pair.of("CAIN", "RAMBO"), res);
        assertEquals(refList, tmpList);

        res = GeographicDataParser.substationOrder(substations, "CAIN  Z4RAMBO", halfLine);
        assertEquals(Pair.of("", ""), res);
        assertEquals(refList, tmpList);

        res = GeographicDataParser.substationOrder(substations, "RAMBO Z4CAIN ", tmpList);
        assertEquals(Pair.of("CAIN", "RAMBO"), res);
        assertEquals(refList, tmpList);

        Collections.reverse(tmpList);
        /* RAMBO is now closer than CAIN */
        res = GeographicDataParser.substationOrder(substations, "CAIN  Z4RAMBO", tmpList);
        assertEquals(Pair.of("RAMBO", "CAIN"), res);

        // now the fun : missing substations :
        res = GeographicDataParser.substationOrder(substations, "MCCAIN__JOHN", tmpList);
        assertEquals(Pair.of("", ""), res);

        res = GeographicDataParser.substationOrder(substations, "JOHN  Z4RAMBO", tmpList);
        assertEquals(Pair.of("RAMBO", ""), res);

        res = GeographicDataParser.substationOrder(substations, "CAIN  Z4JOHN", tmpList);
        assertEquals(Pair.of("", "CAIN"), res);

    }
}
