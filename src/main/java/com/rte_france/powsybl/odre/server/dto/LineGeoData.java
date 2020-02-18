/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server.dto;

import com.powsybl.iidm.network.Country;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@AllArgsConstructor
@Getter
public class LineGeoData {

    private  String id;

    private Country country1;

    private Country country2;

    private List<Coordinate> coordinates;
}
