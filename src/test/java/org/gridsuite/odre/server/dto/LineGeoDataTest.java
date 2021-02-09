/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.dto;

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
        LineGeoData lineGeoData = new LineGeoData("l", "FR", "FR", "ALAMO", new ArrayList<>());

        assertEquals("l", lineGeoData.getId());
        assertEquals("FR", lineGeoData.getCountry1());
        assertEquals("FR", lineGeoData.getCountry2());
        assertEquals("ALAMO", lineGeoData.getSubstationStart());
        assertTrue(lineGeoData.getCoordinates().isEmpty());
    }
}
