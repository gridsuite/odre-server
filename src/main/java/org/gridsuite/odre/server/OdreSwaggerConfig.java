/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
@Configuration
public class OdreSwaggerConfig {
    @Bean
    public OpenAPI createOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Open Data Reseaux Energies geo data import API")
                .description("This is the documentation of Open Data Reseaux Energies geographical data import service")
                .version(OdreController.API_VERSION));
    }
}
