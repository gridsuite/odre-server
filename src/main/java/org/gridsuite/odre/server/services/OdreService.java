/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public interface OdreService {

    void pushSubstations();

    void pushLines();

    void pushSubstationsFromCsv(MultipartFile file);

    void pushLinesFromCsv(Map<String, MultipartFile> files);
}
