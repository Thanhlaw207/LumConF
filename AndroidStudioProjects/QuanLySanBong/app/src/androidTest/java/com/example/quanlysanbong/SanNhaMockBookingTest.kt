package com.example.quanlysanbong

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SanNhaMockBookingTest {
    @Test
    fun customerBooksFreeTimeAndOwnerSeesSameBooking() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        val preferencesName="booking-test-${UUID.randomUUID()}"
        val isolatedContext=object : ContextWrapper(context) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                context.getSharedPreferences(preferencesName,mode)
        }
        val store=SanNhaStore(isolatedContext)
        store.account="booking-customer-test"
        store.setProfile("name","Khách thử đặt sân")
        store.setProfile("phone","0900000001")
        val tomorrow=LocalDate.now().plusDays(1)
        store.seedMockSchedule()
        val seededIds=store.bookings().map { it.id }
        store.seedMockSchedule()
        assertEquals(seededIds,store.bookings().map { it.id })
        assertEquals(listOf("Sân 1","Sân 2","Sân 3"),store.courts())
        assertFalse(store.available(1,"Sân 1",tomorrow,1020,60))
        assertTrue(store.available(1,"Sân 1",tomorrow,1170,90))
        val activity=instrumentation.startActivitySync(
            Intent(context,SanNhaActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as SanNhaActivity
        try {
            instrumentation.runOnMainSync {
                activity.store=store
                activity.ownerRole=false
                activity.day=tomorrow
                // Even old restrictive filters must not hide the browse list.
                activity.players=5; activity.maxPrice=50000; activity.query="no matching pitch"
                activity.go("home",true)
            }
            onView(withContentDescription("Sân bóng Gia Định")).perform(scrollTo(),click())
            onView(withText("Chọn lịch ra sân")).perform(click())
            onView(withText("Sân 1")).perform(scrollTo(),click())
            onView(withContentDescription("Sân 1 · 17:00 · Không trống"))
                .check(matches(not(isEnabled())))
            onView(withContentDescription("Sân 1 · 19:30 · Trống")).perform(scrollTo(),click())
            onView(withText("Tiếp tục đặt sân  →")).perform(click())
            onView(withText("Tôi đã đọc và đồng ý điều khoản đặt sân, chính sách cọc và hủy của sân."))
                .perform(scrollTo(),click())
            onView(withText("Tiếp tục thanh toán cọc")).perform(click())
            onView(withText("Xem thử thanh toán thành công")).perform(click())
            instrumentation.runOnMainSync {
                assertEquals("success",activity.screen)
                val booking=store.bookings().first { it.id==activity.selectedBooking }
                assertEquals("booking-customer-test",booking.user)
                assertEquals("Sân 1",booking.court)
                assertEquals(1170,booking.start)
                assertEquals(90,booking.duration)
                assertFalse(store.available(1,"Sân 1",tomorrow,1170,90))
                assertFalse(store.available(1,"Sân 1",tomorrow,1200,60))
                assertTrue(store.available(1,"Sân 1",tomorrow,1260,60))
                store.account="booking-owner-test"; store.owner=true
                activity.ownerRole=true; activity.ownerSelectedDate=tomorrow
                activity.go("owner_schedule",true)
                assertTrue(activity.ownerBookings().any { it.id==booking.id })
            }
            onView(withText("Khách thử đặt sân")).perform(scrollTo()).check(matches(isDisplayed()))
            instrumentation.runOnMainSync {
                val booking=store.bookings().first { it.id==activity.selectedBooking }
                store.save(booking.copy(status="CANCELLED"))
                assertTrue(store.available(1,"Sân 1",tomorrow,1170,90))
            }
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
            context.deleteSharedPreferences(preferencesName)
        }
    }
}
