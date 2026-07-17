package dev.ividi.weatherapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitsTest {

    @Test
    fun `toggled flips metric to imperial and back`() {
        assertEquals(Units.IMPERIAL, Units.METRIC.toggled())
        assertEquals(Units.METRIC, Units.IMPERIAL.toggled())
    }

    @Test
    fun `fromWireValue is case-insensitive and defaults to metric`() {
        assertEquals(Units.IMPERIAL, Units.fromWireValue("Imperial"))
        assertEquals(Units.METRIC, Units.fromWireValue("metric"))
        assertEquals(Units.METRIC, Units.fromWireValue("anything-else"))
    }

    @Test
    fun `wireValue round-trips through fromWireValue`() {
        Units.entries.forEach { unit ->
            assertEquals(unit, Units.fromWireValue(unit.wireValue))
        }
    }
}
