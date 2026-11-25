package com.example.quanlysanbong

import android.content.Context
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SanNhaAuthFlowTest {
    @Test
    fun registerWithPhoneAndLogInForBothRoles() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = context.getSharedPreferences("san_nha_ui", Context.MODE_PRIVATE)
        val previousAccount = prefs.getString("account", "")!!
        val previousOwner = prefs.getBoolean("owner", false)
        val previousRemember = prefs.getBoolean("remember", true)
        val activity = instrumentation.startActivitySync(
            Intent(context, SanNhaActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as SanNhaActivity
        val phones = mutableListOf<String>()
        try {
            for (owner in listOf(false, true)) {
                val phone = "099" + (System.nanoTime() % 10000000).toString().padStart(7, '0')
                phones.add(phone)
                instrumentation.runOnMainSync {
                    activity.ownerRole = false
                    activity.go("login", true)
                }
                onView(withText("Chưa có tài khoản? Đăng ký ngay")).perform(scrollTo(), click())
                if (owner) onView(withText("Chủ sân")).perform(scrollTo(), click())
                onView(withHint("Nhập họ và tên")).perform(scrollTo(), typeText("Auth test"), closeSoftKeyboard())
                onView(withHint("Nhập số điện thoại")).perform(scrollTo(), typeText(phone), closeSoftKeyboard())
                onView(withHint("Tạo mật khẩu")).perform(scrollTo(), typeText("SanNha123"), closeSoftKeyboard())
                onView(withHint("Nhập lại mật khẩu")).perform(scrollTo(), typeText("SanNha123"), closeSoftKeyboard())
                onView(withText("Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật của Sân Nhà."))
                    .perform(scrollTo(), click())
                onView(withText("Đăng ký  →")).perform(scrollTo(), click())
                instrumentation.runOnMainSync { assertEquals("login", activity.screen) }
                DatabaseHelper(context).use { assertNotNull(it.dangNhap(phone, "SanNha123")) }
                onView(withHint("Nhập số điện thoại")).perform(scrollTo(), typeText(phone), closeSoftKeyboard())
                onView(withHint("Nhập mật khẩu")).perform(scrollTo(), typeText("SanNha123"), closeSoftKeyboard())
                onView(withText("Đăng nhập  →")).perform(scrollTo(), click())
                instrumentation.runOnMainSync {
                    assertEquals(if (owner) "owner_schedule" else "home", activity.screen)
                    assertEquals(phone, activity.store.account)
                }
            }
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
            DatabaseHelper(context).use { helper ->
                phones.forEach { phone ->
                    helper.writableDatabase.delete("NguoiDung", "taiKhoan = ? AND hoTen = ?", arrayOf(phone, "Auth test"))
                }
            }
            prefs.edit().apply {
                phones.forEach { phone -> remove("$phone:name"); remove("$phone:phone") }
                putString("account", previousAccount)
                putBoolean("owner", previousOwner)
                putBoolean("remember", previousRemember)
            }.commit()
        }
    }
}
