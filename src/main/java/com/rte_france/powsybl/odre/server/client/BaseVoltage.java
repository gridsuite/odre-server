/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server.client;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum BaseVoltage {
    VL_400_KV(400),
    VL_225_KV(225),
    VL_150_KV(150),
    VL_90_KV(90),
    VL_63_KV(63),
    VL_45_KV(45),
    VL_INF_45_KV(-1),
    VL_OFF(0),
    VL_CONTINOUS_CURRENT(-99);

    private final int value;

    BaseVoltage(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BaseVoltage parse(String str) {
        switch (str) {
            case "400KV":
            case "400kV":
                return VL_400_KV;
            case "225KV":
            case "225kV":
                return VL_225_KV;
            case "150KV":
            case "150kV":
                return VL_150_KV;
            case "90KV":
            case "90kV":
                return VL_90_KV;
            case "63KV":
            case "63kV":
                return VL_63_KV;
            case "45KV":
            case "45kV":
                return VL_45_KV;
            case "INFERIEUR A 45kV":
            case "INF 45kV":
            case "<45kV":
                return VL_INF_45_KV;
            case "HORS TENSION":
                return VL_OFF;
            case "COURANT CONTINU":
                return VL_CONTINOUS_CURRENT;
            default:
                throw new IllegalStateException("Unknown base voltage: " + str);
        }
    }
}
