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
        assertEquals(Routes.CompanionHub, DeepLinkMapper.routeFromHost("companion"))
        assertEquals(Routes.CompanionHub, DeepLinkMapper.routeFromHost("companion_games"))
        assertEquals(Routes.CompanionPlay, DeepLinkMapper.routeFromHost("companion_play"))
        assertEquals(Routes.CompanionMemory, DeepLinkMapper.routeFromHost("companion_memory"))
        assertEquals(Routes.CompanionPlay, DeepLinkMapper.routeFromHost("companion_hide"))
        assertEquals(Routes.CompanionPlay, DeepLinkMapper.routeFromHost("companion_hide_peek"))
    }

    @Test
    fun unknownHostReturnsNull() {
        assertNull(DeepLinkMapper.routeFromHost(null))
        assertNull(DeepLinkMapper.routeFromHost("unknown"))
    }
}
