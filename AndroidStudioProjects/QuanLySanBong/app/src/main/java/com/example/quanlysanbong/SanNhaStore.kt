package com.example.quanlysanbong

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class SnPitch(val id: Int, val name: String, val address: String, val rate: Int,
                   val rating: String, val distance: Double, val image: Int, val players: Int = 7)

data class SnBooking(
    val id: String = "SN" + UUID.randomUUID().toString().take(8).uppercase(),
    val user: String, val pitchId: Int, val court: String, val date: String,
    val start: Int, val duration: Int, val total: Int, val name: String, val phone: String,
    val note: String = "", val deposit: Int = 0, val status: String = "CONFIRMED",
    val changeDate: String = "", val changeStart: Int = 0, val reviewed: Boolean = false
) {
    val end get() = start + duration
    val timeLabel get() = "${clock(start)} – ${clock(end)}"
    val isPast get() = LocalDate.parse(date).atTime(LocalTime.MIDNIGHT).plusMinutes(end.toLong()) < java.time.LocalDateTime.now()
    companion object {
        fun clock(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
    }
}

class SanNhaStore(context: Context) {
    private val prefs = context.getSharedPreferences("san_nha_ui", Context.MODE_PRIVATE)
    val pitches = listOf(
        SnPitch(1, "Sân bóng Gia Định", "2A Phan Chu Trinh, Bình Thạnh", 350000, "4,9 (128)", 1.2, R.drawable.pitch_giadinh),
        SnPitch(2, "Sân bóng D3", "Đường D3, Bình Thạnh", 380000, "4,8 (96)", 2.4, R.drawable.pitch_d3),
        SnPitch(3, "Sân bóng Cầu Đỏ", "12 Nguyễn Xí, Bình Thạnh", 320000, "4,7 (84)", 3.1, R.drawable.pitch_caudo)
    )
    var account: String
        get() = prefs.getString("account", "")!!
        set(value) { prefs.edit().putString("account", value).apply() }
    var owner: Boolean
        get() = prefs.getBoolean("owner", false)
        set(value) { prefs.edit().putBoolean("owner", value).apply() }
    var remember: Boolean
        get() = prefs.getBoolean("remember", true)
        set(value) { prefs.edit().putBoolean("remember", value).apply() }
    fun profile(key: String, default: String = "") = prefs.getString("$account:$key", default)!!
    fun setProfile(key: String, value: String) { prefs.edit().putString("$account:$key", value).apply() }
    fun favorite(id: Int) = prefs.getStringSet("$account:favorites", emptySet())!!.contains(id.toString())
    fun toggleFavorite(id: Int) {
        val set = prefs.getStringSet("$account:favorites", emptySet())!!.toMutableSet()
        if (!set.add(id.toString())) set.remove(id.toString())
        prefs.edit().putStringSet("$account:favorites", set).apply()
    }
    fun bookings(): List<SnBooking> {
        val array = JSONArray(prefs.getString("bookings", "[]"))
        return (0 until array.length()).map { i -> array.getJSONObject(i).let { j ->
            SnBooking(j.getString("id"), j.getString("user"), j.getInt("pitchId"), j.getString("court"),
                j.getString("date"), j.getInt("start"), j.getInt("duration"), j.getInt("total"),
                j.getString("name"), j.getString("phone"), j.optString("note"), j.optInt("deposit"),
                j.getString("status"), j.optString("changeDate"), j.optInt("changeStart"), j.optBoolean("reviewed"))
        } }.sortedWith(compareBy<SnBooking> { it.date }.thenBy { it.start })
    }
    fun save(booking: SnBooking) {
        val list = bookings().filter { it.id != booking.id } + booking
        val array = JSONArray()
        list.forEach { b -> array.put(JSONObject().apply {
            put("id",b.id); put("user",b.user); put("pitchId",b.pitchId); put("court",b.court)
            put("date",b.date); put("start",b.start); put("duration",b.duration); put("total",b.total)
            put("name",b.name); put("phone",b.phone); put("note",b.note); put("deposit",b.deposit)
            put("status",b.status); put("changeDate",b.changeDate); put("changeStart",b.changeStart); put("reviewed",b.reviewed)
        }) }
        prefs.edit().putString("bookings", array.toString()).apply()
    }
    fun available(pitch: Int, court: String, day: LocalDate, start: Int, duration: Int, except: String = ""): Boolean {
        if (start < 360 || start + duration > 1380 || duration < 60) return false
        if (day.atStartOfDay().plusMinutes(start.toLong()) <= java.time.LocalDateTime.now()) return false
        if(court !in courts(pitch) || (pitch==1 && !venueSetting("active:$court",true))) return false
        return bookings().none { it.user.startsWith("demo-") == account.startsWith("demo-") && it.id != except && it.pitchId == pitch && it.court == court &&
            it.date == day.toString() && it.status != "CANCELLED" && SanNhaRules.overlaps(start,start+duration,it.start,it.end) }
            && !blocked(pitch,court,day,start,duration)
    }
    fun blocked(pitch: Int, court: String, date: LocalDate, start: Int, duration: Int): Boolean {
        val blocks=JSONArray(prefs.getString(namespace+":blocks","[]"))
        return (0 until blocks.length()).any { blocks.getJSONObject(it).let { b ->
            b.getInt("pitch") == pitch && b.getString("court") == court && b.getString("date") == date.toString() &&
                start < b.getInt("end") && start+duration > b.getInt("start")
        } }
    }
    fun block(pitch: Int, court: String, day: LocalDate, start: Int, end: Int) {
        val blocks=JSONArray(prefs.getString(namespace+":blocks","[]"))
        blocks.put(JSONObject().put("pitch",pitch).put("court",court).put("date",day.toString()).put("start",start).put("end",end))
        prefs.edit().putString(namespace+":blocks",blocks.toString()).apply()
    }
    private val namespace get() = if(account.startsWith("demo-")) "demo" else "local"
    fun courts(pitchId: Int = 1): List<String> =
        (if(pitchId==1) venue("courts","Sân 1,Sân 2,Sân 3") else "Sân 1,Sân 2,Sân 3")
            .split(',').map { it.trim() }.filter { it.isNotBlank() }

    /** Shared sample occupancy for signed-in customers and owners on this device. */
    fun seedMockSchedule(today: LocalDate = LocalDate.now()) {
        if(account.isBlank() || account.startsWith("demo-")) return
        if(prefs.getString("mockScheduleDate","")==today.toString()) return
        val existingIds=bookings().map { it.id }.toSet()
        for(offset in 0L..7L) {
            val date=today.plusDays(offset)
            courts().take(3).forEachIndexed { index,court ->
                val time=listOf(1020,1080,1200)[index]
                val id="MOCK-GD-${date}-$index"
                if(id !in existingIds && available(1,court,date,time,60)) {
                    save(SnBooking(id=id,user="mock-customer",pitchId=1,court=court,
                        date=date.toString(),start=time,duration=60,total=quote(1,court,date,time,60),
                        name="Đội mẫu ${index+1}",phone="0900000000",
                        note="Dữ liệu mẫu để thử lịch trống và đặt sân trên thiết bị.",status="DEMO"))
                }
            }
        }
        prefs.edit().putString("mockScheduleDate",today.toString()).apply()
    }
    fun venue(key: String, default: String = "") = prefs.getString("$namespace:venue:$key",default)!!
    fun setVenue(key: String, value: String) { prefs.edit().putString("$namespace:venue:$key",value).apply() }
    fun venueSetting(key: String, default: Boolean) = prefs.getBoolean("$namespace:venue:$key",default)
    fun setVenueSetting(key: String, value: Boolean) { prefs.edit().putBoolean("$namespace:venue:$key",value).apply() }
    fun quote(pitchId: Int, court: String, day: LocalDate, start: Int, duration: Int): Int {
        val p=pitches.first { it.id==pitchId }
        val extra=if(day.dayOfWeek.value>=6) 50000 else 0
        val defaults=if(pitchId==1) listOf(250000,300000,p.rate) else listOf(p.rate,p.rate,p.rate)
        val rates=defaults.mapIndexed { index,v -> (if(pitchId==1) venue("price:$court:$index",v.toString()).toIntOrNull() ?: v else v)+extra }
        return SanNhaRules.quote(start,duration,rates[0],rates[1],rates[2])
    }
    fun seedDemo() {
        if(prefs.getBoolean("demoSeeded",false)) return
        val today=LocalDate.now()
        val samples=listOf(
            SnBooking(id="DEMO-GD-01",user="demo-customer",pitchId=1,court="Sân 1",date=today.plusDays(2).toString(),start=1080,duration=90,total=475000,name="Nguyễn Minh",phone="0901234567",note="Đội mình cần thuê 2 áo bib.",status="DEMO"),
            SnBooking(id="DEMO-D3-02",user="demo-customer",pitchId=2,court="Sân 1",date=today.plusDays(4).toString(),start=1080,duration=90,total=570000,name="Nguyễn Minh",phone="0901234567",status="DEMO",changeDate=today.plusDays(5).toString(),changeStart=1080),
            SnBooking(id="DEMO-GD-03",user="demo-customer",pitchId=1,court="Sân 1",date=today.minusDays(7).toString(),start=1080,duration=60,total=350000,name="Nguyễn Minh",phone="0901234567",status="DEMO"),
            SnBooking(id="DEMO-GD-04",user="demo-customer",pitchId=1,court="Sân 1",date=today.minusDays(3).toString(),start=1080,duration=60,total=350000,name="Nguyễn Minh",phone="0901234567",status="CANCELLED"),
            SnBooking(id="DEMO-OWNER-01",user="demo-customer",pitchId=1,court="Sân 2",date=today.toString(),start=1020,duration=60,total=350000,name="Trần Huy",phone="0903456789",note="Đội Huy FC. Chuẩn bị 2 áo phân đội và 1 bóng.",status="DEMO"),
            SnBooking(id="DEMO-OWNER-02",user="demo-customer",pitchId=1,court="Sân 1",date=today.toString(),start=1200,duration=90,total=525000,name="Đội Thanh Bình",phone="0903456789",status="DEMO")
        )
        samples.forEach { save(it) }
        prefs.edit().putStringSet("demo-customer:favorites",setOf("1","2","3")).putBoolean("demoSeeded",true).apply()
    }
    fun setting(key: String, default: Boolean = false) = prefs.getBoolean("$account:$key",default)
    fun setSetting(key: String, value: Boolean) { prefs.edit().putBoolean("$account:$key",value).apply() }
    fun logout() { prefs.edit().remove("account").putBoolean("owner",false).apply() }
    companion object {
        val vi = Locale.forLanguageTag("vi-VN")
        fun money(value: Int) = java.text.NumberFormat.getIntegerInstance(vi).format(value) + "đ"
        fun date(day: LocalDate) = day.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", vi)).replaceFirstChar { it.uppercase() }
    }
}
