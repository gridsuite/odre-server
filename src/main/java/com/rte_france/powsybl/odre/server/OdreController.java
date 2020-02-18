/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server;

import com.rte_france.powsybl.odre.server.services.OdreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + OdreController.API_VERSION)
@Api(value = "Odre")
public class OdreController {

    static final String API_VERSION = "v1";

    @Autowired
    private OdreService odreService;

    @PostMapping("substations")
    @ApiOperation ("Get Substations coordinates from Open Data Reseaux Energies and send them to geo data service")
    @ApiResponses (value = {@ApiResponse(code = 200, message = "the list of substation was updated")})
    public void pushSubstations() {
        odreService.pushSubstations();
    }

    @PostMapping("lines")
    @ApiOperation (value = "Get lines coordinates from Open Data Reseaux Energies and send them to geo data service")
    @ApiResponses (value = {@ApiResponse(code = 200, message = "the list of lines was updated")})
    public void pushLines() {
        odreService.pushLines();
    }
}
