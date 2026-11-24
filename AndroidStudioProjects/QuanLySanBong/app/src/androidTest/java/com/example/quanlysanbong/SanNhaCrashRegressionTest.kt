package com.example.quanlysanbong

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.view.ViewGroup
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputEditText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SanNhaCrashRegressionTest {
    @Test
    fun loginAndRegistrationOpenWithoutCrashing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.startActivitySync(
            Intent(instrumentation.targetContext, SanNhaActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as SanNhaActivity
        try {
            instrumentation.runOnMainSync {
                activity.go("login", true)
                assertEquals(2, inputCount(activity.content))
                activity.go("register")
                assertEquals(4, inputCount(activity.content))
                activity.back()
                assertEquals("login", activity.screen)
                // Opening the installed database must also work with existing data.
                DatabaseHelper(activity).use { helper ->
                    assertTrue(helper.readableDatabase.isOpen)
                }
            }
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test
    fun upgradingOlderDatabasesPreservesAccountsAndOtherTables() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (oldVersion in 3..5) {
            SQLiteDatabase.create(null).use { db ->
                DatabaseHelper(context).use { helper ->
                    db.execSQL("CREATE TABLE SanBong (id INTEGER PRIMARY KEY, tenSan TEXT)")
                    helper.onCreate(db)
                    db.execSQL("INSERT INTO NguoiDung (taiKhoan, hoTen) VALUES ('migration-test', 'Saved account')")
                    db.execSQL("INSERT INTO SanBong VALUES (1, 'Saved pitch')")
                    helper.onUpgrade(db, oldVersion, 6)
                    db.rawQuery("SELECT hoTen FROM NguoiDung WHERE taiKhoan = 'migration-test'", null).use {
                        assertTrue(it.moveToFirst())
                        assertEquals("Saved account", it.getString(0))
                    }
                    db.rawQuery("SELECT tenSan FROM SanBong WHERE id = 1", null).use {
                        assertTrue(it.moveToFirst())
                        assertEquals("Saved pitch", it.getString(0))
                    }
                }
            }
        }
    }

    @Test
    fun upgradingAccountOnlyDatabaseCreatesPitchTable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SQLiteDatabase.create(null).use { db ->
            db.execSQL("CREATE TABLE NguoiDung (id INTEGER PRIMARY KEY, taiKhoan TEXT)")
            db.execSQL("INSERT INTO NguoiDung VALUES (1, 'saved-account')")
            DatabaseHelper(context).use { helper ->
                helper.onUpgrade(db, 5, 6)
                helper.onCreate(db)
                db.execSQL("INSERT INTO SanBong (tenSan, loaiSan, giaThue, trangThai, chuSanId) VALUES ('Pitch', '5', 100000, 'Available', 1)")
                db.rawQuery("SELECT taiKhoan FROM NguoiDung WHERE id = 1", null).use {
                    assertTrue(it.moveToFirst())
                    assertEquals("saved-account", it.getString(0))
                }
                db.rawQuery("SELECT tenSan FROM SanBong", null).use {
                    assertTrue(it.moveToFirst())
                    assertEquals("Pitch", it.getString(0))
                }
            }
        }
    }

    private fun inputCount(view: View): Int = when (view) {
        is TextInputEditText -> 1
        is ViewGroup -> (0 until view.childCount).sumOf { inputCount(view.getChildAt(it)) }
        else -> 0
    }
}
