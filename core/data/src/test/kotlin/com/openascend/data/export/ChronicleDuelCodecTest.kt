package com.openascend.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ChronicleDuelCodecTest {

  @Test
  fun roundTrip_encodesAndDecodes() {
    val summary = ChronicleDuelSummary(
      displayName = "Aldric",
      level = 7,
      recovery = 12,
      stamina = 10,
      stability = 8,
      discipline = 14,
      vitality = 9,
      bossName = "The Drowsy Warden",
      weekLabel = "2026-W21",
    )
    val text = ChronicleDuelCodec.encode(summary)
    val decoded = ChronicleDuelCodec.decode(text)
    assertEquals(summary, decoded)
  }

  @Test
  fun decode_invalidJson_returnsNull() {
    assertNull(ChronicleDuelCodec.decode("{not valid"))
  }

  @Test
  fun decode_unknownFields_ignored() {
    val json =
      """{"schemaVersion":1,"displayName":"X","level":1,"recovery":1,"stamina":1,"stability":1,"discipline":1,"vitality":1,"bossName":"B","weekLabel":"W","extra":true}"""
    assertNotNull(ChronicleDuelCodec.decode(json))
  }
}
