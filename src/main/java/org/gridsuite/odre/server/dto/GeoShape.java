/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.gridsuite.odre.server.utils.GeoShapeDeserializer;

import java.util.List;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */

@JsonDeserialize(using = GeoShapeDeserializer.class)
public record GeoShape(List<Coordinate> coordinates) { }
