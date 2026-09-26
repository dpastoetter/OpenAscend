package com.openascend.data.narrative

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

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

    @Test
    fun importPack_rejectsPathTraversalId() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = CustomNarrativePackStore(context)
        val json = """
            {
              "id": "../../../shared_prefs/evil",
              "actTitles": ["Act One"]
            }
        """.trimIndent()
        val result = store.importPack("evil.json", json)
        assertTrue(result.isFailure)

        val escaped = File(context.filesDir, "shared_prefs/evil.json")
        assertTrue(!escaped.exists())
        assertNull(store.loadPack("../../../shared_prefs/evil"))
    }

    @Test
    fun importPack_rejectsOversizedPayload() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = CustomNarrativePackStore(context)
        val huge = "x".repeat(1_048_577)
        val result = store.importPack("big.json", huge)
        assertTrue(result.isFailure)
    }
}
