/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.client;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.gridsuite.odre.server.dto.LineGeoData;
import org.gridsuite.odre.server.dto.SubstationGeoData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
public interface OdreCsvClient {

    List<SubstationGeoData> getSubstationsFromCsv(MultipartFile file);

    List<LineGeoData> getLinesFromCsv(List<MultipartFile> files);

    static BOMInputStream toBOMInputStream(InputStream inputStream) throws IOException {
        return BOMInputStream.builder().setInputStream(inputStream).setByteOrderMarks(ByteOrderMark.UTF_8).get();
    }
}
