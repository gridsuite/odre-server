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
import org.gridsuite.odre.server.dto.FileUploadResponse;
import org.gridsuite.odre.server.services.OdreService;
import org.gridsuite.odre.server.utils.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    @PostMapping(value = "/lines/upload", consumes = "multipart/form-data")
    @Operation(summary = "Get lines coordinates from csv files and send them to geo data service")
    @ApiResponses (value = {@ApiResponse(responseCode = "200", description = "the list of lines was updated"),
            @ApiResponse(responseCode = "500", description = "fail to upload file(s)"),
            @ApiResponse(responseCode = "400", description = "invalid csv file or missing file(s)"),
    })
    public ResponseEntity<FileUploadResponse> pushLinesFromCsv(@RequestParam("files") MultipartFile[] files) {
        //check if all files are present
        try {
            List<MultipartFile> fileList = new ArrayList<>();
            if (Arrays.stream(files).filter(file -> FileValidator.hasCSVFormat(file)).count() != 3) {
                return new ResponseEntity<>(new FileUploadResponse(HttpStatus.BAD_REQUEST.value(), "Please upload all csv files Lines(AERIAL,UNDERGROUND) and Substations !"), HttpStatus.BAD_REQUEST);
            } else {
                Arrays.stream(files).forEach(file -> fileList.add(file));
                return new ResponseEntity<>(odreService.pushLinesFromCsv(fileList), HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new FileUploadResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to upload files ! " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/substations/upload", consumes = "multipart/form-data")
    @Operation(summary = "Get Substations coordinates from Given CSV file and send them to geo data service")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "the list of substation was updated"),
            @ApiResponse(responseCode = "500", description = "fail to upload file"),
            @ApiResponse(responseCode = "400", description = "invalid csv file"),
    })
    public ResponseEntity<FileUploadResponse>  pushSubstationsFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (FileValidator.hasCSVFormat(file)) {
                return new ResponseEntity<>(odreService.pushSubstationsFromCsv(file), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new FileUploadResponse(HttpStatus.BAD_REQUEST.value(), "Please upload a csv file !"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new FileUploadResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to upload file ! " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
