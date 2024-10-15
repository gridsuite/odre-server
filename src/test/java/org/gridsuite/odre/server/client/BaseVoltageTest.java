package org.gridsuite.odre.server.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseVoltageTest {
    @Test
    void test() {
        BaseVoltage baseVoltage = BaseVoltage.VL_90_KV;
        assertEquals(90, baseVoltage.getValue());

        assertEquals(400, BaseVoltage.parse("400KV").getValue());
        assertEquals(225, BaseVoltage.parse("225KV").getValue());
        assertEquals(150, BaseVoltage.parse("150KV").getValue());
        assertEquals(90, BaseVoltage.parse("90KV").getValue());
        assertEquals(63, BaseVoltage.parse("63KV").getValue());
        assertEquals(45, BaseVoltage.parse("45KV").getValue());
        assertEquals(-1, BaseVoltage.parse("INFERIEUR A 45kV").getValue());
        assertEquals(-1, BaseVoltage.parse("INF 45kV").getValue());
        assertEquals(-1, BaseVoltage.parse("<45kV").getValue());
        assertEquals(0, BaseVoltage.parse("HORS TENSION").getValue());
        assertEquals(-99, BaseVoltage.parse("COURANT CONTINU").getValue());
    }
}
