package com.monoplayer.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {
    @Test fun rendersMinutesAndSeconds() { assertEquals("3:05", 185_000L.asTime()) }
    @Test fun rendersLongTracks() { assertEquals("65:00", 3_900_000L.asTime()) }
}
