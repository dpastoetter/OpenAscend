package com.openascend.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object OpenAscendMigrations {

    val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profile ADD COLUMN avatarRelativePath TEXT")
        }
    }

    val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profile ADD COLUMN archetypeSuffix TEXT")
            db.execSQL("ALTER TABLE habits ADD COLUMN isRestDay INTEGER NOT NULL DEFAULT 0")
        }
    }

    val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profile ADD COLUMN starterPath TEXT")
            db.execSQL("ALTER TABLE habits ADD COLUMN bossPrep INTEGER NOT NULL DEFAULT 0")
        }
    }

    val ALL: Array<Migration> = arrayOf(migration2To3, migration3To4, migration4To5)
}
