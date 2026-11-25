package com.example.quanlysanbong

import org.junit.Assert.*
import org.junit.Test

class SanNhaRulesTest {
    @Test fun adjacentBookingsDoNotOverlap() { assertFalse(SanNhaRules.overlaps(1080,1140,1140,1200)) }
    @Test fun partialOverlapIsBlockedInBothDirections() {
        assertTrue(SanNhaRules.overlaps(1080,1170,1140,1200))
        assertTrue(SanNhaRules.overlaps(1140,1200,1080,1170))
    }
    @Test fun enclosingBookingOverlaps() { assertTrue(SanNhaRules.overlaps(960,1260,1080,1140)) }
    @Test fun ninetyMinuteEveningBookingIsProrated() { assertEquals(525000,SanNhaRules.quote(1080,90,250000,300000,350000)) }
    @Test fun priceChangesAtSixteenAreSplit() { assertEquals(325000,SanNhaRules.quote(930,60,250000,300000,350000)) }
    @Test fun priceChangesAtNoonAreSplit() { assertEquals(425000,SanNhaRules.quote(690,90,250000,300000,350000)) }
    @Test fun closingTimeIsAllowed() { assertEquals(350000,SanNhaRules.quote(1320,60,250000,300000,350000)) }
    @Test(expected=IllegalArgumentException::class) fun pastClosingIsRejected() { SanNhaRules.quote(1350,60,250000,300000,350000) }
}
