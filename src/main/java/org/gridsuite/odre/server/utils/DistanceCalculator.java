/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.odre.server.utils;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public final class DistanceCalculator {

    private DistanceCalculator() {

    }

    /**
     * calculate distance between two geographical points
     * the calculation assume that the earth is spherical and its radius equal to 6_378_137
     * @param lat and lon of the two points
     * @return distance en meter
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        // source : https://geodesie.ign.fr/contenu/fichiers/Distance_longitude_latitude.pdf
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double dL = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(dL));
            dist = Math.acos(dist);
            //6 378 137 is the conventional earth radius
            return dist * 6_378_137;
        }
    }
}
