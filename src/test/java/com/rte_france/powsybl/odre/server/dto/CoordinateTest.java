/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class CoordinateTest {

    @Test
    public void test() {
        Coordinate coordinate = new Coordinate(1, 2);
        assertEquals(1, coordinate.getLat(), 0);
        assertEquals(2, coordinate.getLon(), 0);
        assertEquals("Coordinate(lat=1.0, lon=2.0)", coordinate.toString());
    }
}
