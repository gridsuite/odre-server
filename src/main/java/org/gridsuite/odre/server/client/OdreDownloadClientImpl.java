/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.gridsuite.odre.server.utils.GeographicDataParser;
import org.gridsuite.odre.server.utils.InputUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;

import java.util.*;

/**
 * Parse RTE substation and line segment coordinates.
 * <ul>
 *     <li>
 *         <a href="https://opendata.reseaux-energies.fr/explore/dataset/postes-electriques-rte/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true">postes-electriques-rte.csv</a>
 *     </li>
 *     <li>
 *         <a href="https://opendata.reseaux-energies.fr/explore/dataset/lignes-aeriennes-rte/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true">lignes-aeriennes-rte.csv</a>
 *     </li>
 *     <li>
 *         <a href="https://opendata.reseaux-energies.fr/explore/dataset/lignes-souterraines-rte/download/?format=csv&timezone=Europe/Berlin&use_labels_for_header=true">lignes-souterraines-rte.csv</a>
 *     </li>
 * </ul>
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

@Component
public class OdreDownloadClientImpl implements OdreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdreDownloadClientImpl.class);

    private RestTemplate openDataRest;

    @Autowired
    public OdreDownloadClientImpl() {
        String openDataBaseUri = "https://opendata.reseaux-energies.fr";
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        this.openDataRest = restTemplateBuilder.build();
        this.openDataRest.setUriTemplateHandler(new DefaultUriBuilderFactory(openDataBaseUri));
    }

    private ByteArrayInputStream downloadFile(String path) {
        String uri = UriComponentsBuilder.fromPath(path)
                .queryParam("format", "csv")
                .toUriString();

        ResponseEntity<byte[]> responseEntity = openDataRest.exchange(uri,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                byte[].class);

        return new ByteArrayInputStream(Objects.requireNonNull(responseEntity.getBody()));
    }

    @Override
    public List<SubstationGeoData> getSubstations() {
        try {
            ByteArrayInputStream byteArrayInputStream = downloadFile("/explore/dataset/postes-electriques-rte/download/");
            LOGGER.info("substations were downloaded from the open data server");
            return new ArrayList<>(GeographicDataParser.parseSubstations(new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(byteArrayInputStream)))).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<LineGeoData> getLines() {
        try {
            ByteArrayInputStream undergroundLinesByteArrayInputStream = downloadFile("/explore/dataset/lignes-souterraines-rte/download/");
            LOGGER.info("Underground lines were downloaded from the open data server");
            ByteArrayInputStream aerialLinesByteArrayInputStream = downloadFile("/explore/dataset/lignes-aeriennes-rte/download/");
            LOGGER.info("Aerial lines were downloaded from the open data server");
            ByteArrayInputStream substationInputStream = downloadFile("/explore/dataset/postes-electriques-rte/download/");
            LOGGER.info("substations were downloaded from the open data server");
            Map<String, SubstationGeoData> substationsGeoData = GeographicDataParser.parseSubstations(new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(substationInputStream))));
            return new ArrayList<>(
                GeographicDataParser.parseLines(new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(aerialLinesByteArrayInputStream))),
                    new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(undergroundLinesByteArrayInputStream))),
                    substationsGeoData).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void setOpenDataRest(RestTemplate openDataRest) {
        this.openDataRest = openDataRest;
    }
}
