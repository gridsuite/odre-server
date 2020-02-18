/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.rte_france.powsybl.odre.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class DistanceCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistanceCalculator.class);

    public static void main(String[] args) {
        LOGGER.warn("{} Meters", distance(32.9697, -96.80322, 29.46786, -98.53506, "M"));
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("M")) {
                dist = dist * 1.609344 * 1000;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return dist;
        }
    }
}
