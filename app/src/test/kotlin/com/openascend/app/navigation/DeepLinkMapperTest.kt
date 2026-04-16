package com.openascend.app.navigation

import android.app.Application
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
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
        assertEquals(Routes.CompanionSequence, DeepLinkMapper.routeFromHost("companion_sequence"))
        assertEquals(Routes.CompanionGlide, DeepLinkMapper.routeFromHost("companion_glide"))
        assertEquals(Routes.CompanionPlay, DeepLinkMapper.routeFromHost("companion_hide"))
        assertEquals(Routes.CompanionPlay, DeepLinkMapper.routeFromHost("companion_hide_peek"))
    }

    @Test
    fun unknownHostReturnsNull() {
        assertNull(DeepLinkMapper.routeFromHost(null))
        assertNull(DeepLinkMapper.routeFromHost("unknown"))
    }

    @Test
    fun validatedDeepLinkRejectsWrongScheme() {
        assertNull(DeepLinkMapper.validatedDeepLinkRoute(Uri.parse("https://example.com/")))
        // Same host as a known route, but wrong scheme — must not navigate via deep link.
        assertNull(DeepLinkMapper.validatedDeepLinkRoute(Uri.parse("https://home")))
        assertEquals(Routes.Home, DeepLinkMapper.validatedDeepLinkRoute(Uri.parse("openascend://home")))
        assertEquals(Routes.Home, DeepLinkMapper.validatedDeepLinkRoute(Uri.parse("OpenAscend://HOME")))
    }
}
