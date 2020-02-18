/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server.dto;

import com.powsybl.iidm.network.Country;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class LineGeoDataTest {

    @Test
    public void test() {
        LineGeoData lineGeoData = new LineGeoData("l", Country.FR, Country.FR, new ArrayList<>());

        assertEquals("l", lineGeoData.getId());
        assertEquals(Country.FR, lineGeoData.getCountry1());
        assertEquals(Country.FR, lineGeoData.getCountry2());
        assertTrue(lineGeoData.getCoordinates().isEmpty());
    }
}
