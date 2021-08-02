/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.odre.server.services.OdreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + OdreController.API_VERSION)
@Tag(name = "Odre")
public class OdreController {

    static final String API_VERSION = "v1";

    @Autowired
    private OdreService odreService;

    @PostMapping("substations")
    @Operation(summary = "Get Substations coordinates from Open Data Reseaux Energies and send them to geo data service")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "the list of substation was updated")})
    public void pushSubstations() {
        odreService.pushSubstations();
    }

    @PostMapping("lines")
    @Operation(summary = "Get lines coordinates from Open Data Reseaux Energies and send them to geo data service")
    @ApiResponses (value = {@ApiResponse(responseCode = "200", description = "the list of lines was updated")})
    public void pushLines() {
        odreService.pushLines();
    }
}
