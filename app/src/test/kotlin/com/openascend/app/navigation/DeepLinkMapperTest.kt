package com.openascend.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkMapperTest {

    @Test
    fun mapsKnownHosts() {
        assertEquals(Routes.Home, DeepLinkMapper.routeFromHost("home"))
        assertEquals(Routes.CheckIn, DeepLinkMapper.routeFromHost("check_in"))
        assertEquals(Routes.CheckIn, DeepLinkMapper.routeFromHost("checkin"))
        assertEquals(Routes.Weekly, DeepLinkMapper.routeFromHost("weekly"))
        assertEquals(Routes.BossRitual, DeepLinkMapper.routeFromHost("boss"))
        assertEquals(Routes.Settings, DeepLinkMapper.routeFromHost("settings"))
    }

    @Test
    fun unknownHostReturnsNull() {
        assertNull(DeepLinkMapper.routeFromHost(null))
        assertNull(DeepLinkMapper.routeFromHost("unknown"))
    }
}
