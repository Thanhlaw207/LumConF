package com.example.quanlysanbong

/** Booking intervals are half open: 18:00–19:00 does not block 19:00. */
object SanNhaRules {
    fun overlaps(start: Int, end: Int, otherStart: Int, otherEnd: Int) = start < otherEnd && end > otherStart
    fun quote(start: Int, duration: Int, morning: Int, afternoon: Int, evening: Int): Int {
        require(start >= 360 && duration >= 60 && start + duration <= 1380)
        require(listOf(morning,afternoon,evening).all { it in 1000..10050000 })
        val ranges=listOf(Triple(360,720,morning),Triple(720,960,afternoon),Triple(960,1380,evening))
        val weighted=ranges.sumOf { (from,to,price) ->
            (minOf(start+duration,to)-maxOf(start,from)).coerceAtLeast(0).toLong()*price
        }
        return (weighted/60).toInt()
    }
}
