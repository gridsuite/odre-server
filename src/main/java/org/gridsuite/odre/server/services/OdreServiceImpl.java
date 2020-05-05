/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.services;

import org.gridsuite.odre.server.client.OdreClient;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@Service
public class OdreServiceImpl implements OdreService {

    private static final String GEO_DATA_API_VERSION = "v1";

    @Autowired
    @Qualifier("odreDownloadClientImpl")
    private OdreClient client;

    private RestTemplate geoDataServerRest;

    private String geoDataServerBaseUri;

    @Autowired
    public OdreServiceImpl(@Value("${geo-data-server.base.url}") String geoDataServerBaseUri) {
        this.geoDataServerBaseUri = Objects.requireNonNull(geoDataServerBaseUri);
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        this.geoDataServerRest = restTemplateBuilder.build();
        this.geoDataServerRest.setUriTemplateHandler(new DefaultUriBuilderFactory(geoDataServerBaseUri));
    }

    @Override
    public void pushSubstations() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(geoDataServerBaseUri + "/" + GEO_DATA_API_VERSION + "/substations");

        List<SubstationGeoData> substationsGeoData = client.getSubstations();

        HttpEntity<List<SubstationGeoData>> requestEntity = new HttpEntity<>(substationsGeoData, requestHeaders);

        geoDataServerRest.exchange(uriBuilder.toUriString(),
                HttpMethod.POST,
                requestEntity,
                Void.class);
    }

    @Override
    public void pushLines() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(geoDataServerBaseUri + "/" + GEO_DATA_API_VERSION + "/lines");

        List<LineGeoData> linesGeoData = client.getLines();

        HttpEntity<List<LineGeoData>> requestEntity = new HttpEntity<>(linesGeoData, requestHeaders);

        geoDataServerRest.exchange(uriBuilder.toUriString(),
                HttpMethod.POST,
                requestEntity,
                Void.class);
    }
}
