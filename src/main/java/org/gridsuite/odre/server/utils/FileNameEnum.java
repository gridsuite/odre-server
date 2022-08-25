/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.utils;

/**
 * @author bendaamerahm <ahmed.bendaamer at rte-france.com>
 */
public enum FileNameEnum {

    SUBSTATIONS("postes-electriques-rte.csv"), AERIAL_LINES("lignes-aeriennes-rte.csv"), UNDERGROUND_LINES("lignes-souterraines-rte.csv");

    private String value;

    private FileNameEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static boolean checkIfValueExist(String value) {
        for (FileNameEnum name : values()) {
            if (name.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
