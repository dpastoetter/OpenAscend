package com.openascend.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OpenAscendMigration4To5Test {

  @Test
  fun migration4To5_addsStarterPathAndBossPrep() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    context.deleteDatabase(TEST_DB)
    val config =
      SupportSQLiteOpenHelper.Configuration.builder(context)
        .name(TEST_DB)
        .callback(
          object : SupportSQLiteOpenHelper.Callback(4) {
            override fun onCreate(db: SupportSQLiteDatabase) {
              createVersion4Schema(db)
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
          },
        )
        .build()
    val helper = FrameworkSQLiteOpenHelperFactory().create(config)
    val db = helper.writableDatabase
    OpenAscendMigrations.migration4To5.migrate(db)
    db.query("PRAGMA table_info(profile)").use { cursor ->
      val columns = mutableListOf<String>()
      while (cursor.moveToNext()) {
        columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
      }
      assertTrue(columns.contains("starterPath"))
    }
    db.query("PRAGMA table_info(habits)").use { cursor ->
      val columns = mutableListOf<String>()
      while (cursor.moveToNext()) {
        columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
      }
      assertTrue(columns.contains("bossPrep"))
    }
    db.close()
    helper.close()
  }

  private fun createVersion4Schema(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE profile (
        id INTEGER PRIMARY KEY NOT NULL,
        displayName TEXT NOT NULL,
        onboardingComplete INTEGER NOT NULL,
        goalsJson TEXT NOT NULL,
        streakDays INTEGER NOT NULL,
        lastLoggedEpochDay INTEGER,
        avatarRelativePath TEXT,
        archetypeSuffix TEXT
      )
      """.trimIndent(),
    )
    db.execSQL(
      """
      CREATE TABLE habits (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT NOT NULL,
        frequencyPerWeek INTEGER NOT NULL,
        difficulty INTEGER NOT NULL,
        linkedStat TEXT NOT NULL,
        isRestDay INTEGER NOT NULL DEFAULT 0
      )
      """.trimIndent(),
    )
  }

  companion object {
    private const val TEST_DB = "migration-test"
  }
}
