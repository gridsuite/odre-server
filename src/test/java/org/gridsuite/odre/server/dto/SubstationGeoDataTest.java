/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
class SubstationGeoDataTest {
    @Test
    void test() {
        SubstationGeoData substationGeoData = new SubstationGeoData("id", "FR", new Coordinate(1, 1));

        assertEquals("id", substationGeoData.getId());
        assertEquals("FR", substationGeoData.getCountry());
        assertEquals(new Coordinate(1, 1), substationGeoData.getCoordinate());
    }
}
