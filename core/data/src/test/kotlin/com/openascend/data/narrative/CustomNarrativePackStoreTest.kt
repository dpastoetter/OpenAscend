package com.openascend.data.narrative

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CustomNarrativePackStoreTest {

    @Test
    fun importPack_roundTrip() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = CustomNarrativePackStore(context)
        val json = """
            {
              "id": "test_pack",
              "actTitles": ["Act One"],
              "bossTellTemplates": ["{boss} tests {stat}."]
            }
        """.trimIndent()
        val id = store.importPack("test.json", json).getOrThrow()
        assertEquals("test_pack", id)
        val loaded = store.loadPack("test_pack")
        assertTrue(loaded != null)
        assertEquals("test_pack", loaded!!.id)
    }
}
