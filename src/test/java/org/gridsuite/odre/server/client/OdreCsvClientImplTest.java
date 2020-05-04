/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class OdreCsvClientImplTest {

    @Test
    public void test() throws FileNotFoundException {

        OdreCsvClientImpl odreCsvClient = new OdreCsvClientImpl();

        List<LineGeoData> linesGeoData = odreCsvClient.getLines(ResourceUtils.getFile("classpath:lignes-aeriennes-rte-light.csv").toPath(),
                ResourceUtils.getFile("classpath:lignes-souterraines-rte-light.csv").toPath());

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

        List<SubstationGeoData> substationGeoData = odreCsvClient.getSubstations(ResourceUtils.getFile("classpath:postes-electriques-rte-light.csv").toPath());
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
