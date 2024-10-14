/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
class LineGeoDataTest {
    @Test
    void test() {
        LineGeoData lineGeoData = new LineGeoData("l", "FR", "FR", "ALAMO", "CORAL", new ArrayList<>());

        assertEquals("l", lineGeoData.getId());
        assertEquals("FR", lineGeoData.getCountry1());
        assertEquals("FR", lineGeoData.getCountry2());
        assertEquals("ALAMO", lineGeoData.getSubstationStart());
        assertEquals("CORAL", lineGeoData.getSubstationEnd());
        assertTrue(lineGeoData.getCoordinates().isEmpty());
    }
}
