/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public interface OdreClient {

    List<SubstationGeoData> getSubstations();

    List<SubstationGeoData> getSubstations(Path path);

    List<LineGeoData> getLines();

    List<LineGeoData> getLines(Path aerialLinesFilePath, Path undergroundLinesFilePath);
}
