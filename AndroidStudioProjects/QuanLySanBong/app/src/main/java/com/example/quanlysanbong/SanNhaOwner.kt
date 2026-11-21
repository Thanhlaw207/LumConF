package com.example.quanlysanbong

import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.widget.*
import java.time.LocalDate

fun SanNhaActivity.ownerBookings() = store.bookings().filter { it.user.startsWith("demo-")==demo && it.pitchId==1 }
fun SanNhaActivity.courts() = store.courts(pitchId)

fun SanNhaActivity.ownerSchedule() {
    pitchId=1
    if(court !in courts()) court=courts().first()
    title("Lịch đặt sân","Sân bóng Gia Định",false)
    if(demo) notice(content,"Dữ liệu mẫu để xem giao diện chủ sân. Không phải lịch thật của sân.")
    else notice(content,"Đang chạy thử: lịch mẫu và lịch khách đặt được dùng chung trên thiết bị này.")
    dates(content,ownerSelectedDate) { ownerSelectedDate=it; render() }
    chips(content,courts(),court) { court=it; render() }
    label(content,"● Trống     ● Đã đặt     ▧ Khóa lịch",11,muted,top=18)
    section(content,date(ownerSelectedDate),"Khóa giờ") {
        pickTime(1140) { t ->
            pickTime(t+60) { end ->
                if(end<=t || !store.available(1,court,ownerSelectedDate,t,end-t)) toast("Giờ khóa không hợp lệ hoặc đã có lịch đặt")
                else { store.block(1,court,ownerSelectedDate,t,end); toast("Đã khóa giờ"); render() }
            }
        }
    }
    val list=ownerBookings().filter { it.date==ownerSelectedDate.toString() && it.court==court && it.status!="CANCELLED" }
    val occupied=mutableSetOf<Int>()
    (360 until 1380 step 60).forEach { t ->
        val b=list.firstOrNull { t>=it.start && t<it.end }
        if(b!=null && b.start in occupied) return@forEach
        if(b!=null) occupied.add(b.start)
        val locked=store.blocked(1,court,ownerSelectedDate,t,60)
        val r=row(); val time=column(); label(time,clock(b?.start ?: t),12,dark,true); label(time,clock(b?.end ?: t+60),10,muted,top=6)
        r.addView(time,LinearLayout.LayoutParams(dp(62),-2))
        val box=column(12).apply { background=bg(if(locked) surface else if(b!=null) mint else Color.WHITE,10,line); minimumHeight=dp(68) }
        label(box,if(locked) "Khóa lịch" else b?.name ?: "Khung giờ trống",14,if(locked) muted else dark,true)
        label(box,if(locked) "Do chủ sân thiết lập" else if(b!=null) "${if(b.status=="DEMO") "Lịch mẫu" else "Đã xác nhận"} · Đã thu ${money(b.deposit)}" else "${money(hourlyRate)} / giờ · Tạo lịch  +",11,if(locked) muted else green,top=8)
        box.setOnClickListener {
            if(b!=null) { selectedBooking=b.id; go("owner_booking") }
            else if(!locked) { day=ownerSelectedDate; start=t; duration=60; go("owner_manual") }
            else toast("Khung giờ đã bị khóa")
        }
        r.addView(box,LinearLayout.LayoutParams(0,-2,1f)); add(content,r,10)
    }
    val pending=ownerBookings().count { it.changeDate.isNotBlank() && it.status!="CANCELLED" }
    if(pending>0) { notice(content,"Có $pending yêu cầu đổi lịch chờ xác nhận.",true)
        ownerBookings().filter { it.changeDate.isNotBlank() && it.status!="CANCELLED" }.forEach { b -> menuRow(content,"${b.name} · Đổi lịch","${b.changeDate} · ${clock(b.changeStart)}","calendar") { selectedBooking=b.id; go("owner_booking") } }
    }
    button(footer,"＋ Tạo lịch đặt") { day=ownerSelectedDate; duration=60; start=maxOf(1080,java.time.LocalTime.now().hour*60+60).coerceAtMost(1320); go("owner_manual") }
    bottom("owner_schedule")
}

fun SanNhaActivity.ownerBooking() {
    val b=currentBooking ?: return
    title("Chi tiết đơn đặt sân","Mã đơn #${b.id} · Đặt thủ công")
    notice(content,if(b.status=="CANCELLED") "Đã hủy" else if(b.status=="DEMO") "Lịch mẫu · Không có giao dịch thật" else "Đã xác nhận · ${if(b.deposit>0) "Đã thu ${money(b.deposit)}" else "Chưa thanh toán"}")
    val customer=card(content); menuRow(customer,b.name,b.phone,"user") { external("tel:${b.phone}") }
    section(content,"Thông tin đặt sân","Đổi lịch") {
        choose("Đổi ngày",listOf("Chọn ngày mới")) { pickDate(LocalDate.parse(b.date)) { newDay -> pickTime(b.start) { newStart ->
            if(store.available(b.pitchId,b.court,newDay,newStart,b.duration,b.id)) { store.save(b.copy(date=newDay.toString(),start=newStart)); render() }
            else toast("Khung giờ không trống")
        } } }
    }
    val info=card(content); label(info,"${b.court} · 7 người",16,green,true); pair(info,"Ngày đá",date(LocalDate.parse(b.date))); pair(info,"Khung giờ",b.timeLabel); pair(info,"Thời lượng","${b.duration} phút")
    section(content,"Thanh toán"); val pay=card(content); pair(pay,"Tổng tiền",money(b.total),true,green); pair(pay,"Đã nhận",money(b.deposit)); pair(pay,"Còn phải thu",money(b.total-b.deposit),true,gold)
    section(content,"Ghi chú"); label(content,b.note.ifBlank { "Không có ghi chú" },14,muted)
    if(b.changeDate.isNotEmpty()) {
        notice(content,"Khách đề xuất ${b.changeDate} · ${clock(b.changeStart)}. Lịch cũ vẫn đang giữ.",true)
        button(content,"Duyệt yêu cầu đổi lịch") {
            val proposed=LocalDate.parse(b.changeDate)
            if(store.available(b.pitchId,b.court,proposed,b.changeStart,b.duration,b.id)) { store.save(b.copy(date=b.changeDate,start=b.changeStart,changeDate="")); toast("Đã duyệt đổi lịch"); render() }
            else toast("Khung giờ mới không trống. Không thể duyệt.")
        }
        button(content,"Từ chối đổi lịch",false,true) { store.save(b.copy(changeDate="")); render() }
    }
    notice(content,"Chỉ ghi nhận khoản tiền thực tế đã nhận từ khách. Thao tác này không tự thu tiền qua ngân hàng.")
    if(b.status!="CANCELLED") {
        if(b.deposit<b.total) button(footer,"Thu nốt ${money(b.total-b.deposit)}") {
            confirm("Ghi nhận đã nhận đủ tiền?","Xác nhận bạn đã thực tế nhận ${money(b.total-b.deposit)} từ khách.") { store.save(b.copy(deposit=b.total)); render() }
        }
        button(footer,"Hủy đơn",false,true) { confirm("Hủy đơn?","Đơn ${b.id} sẽ được hủy và giải phóng giờ đặt.") { store.save(b.copy(status="CANCELLED")); go("owner_schedule",true) } }
    }
}

fun SanNhaActivity.ownerManual() {
    title("Tạo lịch đặt thủ công","Sân bóng Gia Định · Đơn đặt tại quầy")
    section(content,"Khách hàng")
    val name=input(content,"Họ và tên khách *","Nhập họ và tên")
    val phone=input(content,"Số điện thoại *","Nhập số điện thoại",type=InputType.TYPE_CLASS_PHONE)
    section(content,"Sân và thời gian")
    if(court !in courts()) court=courts().first()
    chips(content,courts(),court) { court=it }
    var chosenDay=day; var chosenStart=start; var chosenDuration=duration
    val dateButton=button(content,date(chosenDay),false) { }
    dateButton.setOnClickListener { pickDate(chosenDay) { chosenDay=it; dateButton.text=date(it) } }
    val timeButton=button(content,"${clock(chosenStart)} – ${clock(chosenStart+chosenDuration)}",false) { }
    timeButton.setOnClickListener { pickTime(chosenStart) { t -> chosenStart=t; choose("Thời lượng",listOf("60 phút","90 phút","120 phút")) { chosenDuration=it.substringBefore(' ').toInt(); timeButton.text="${clock(t)} – ${clock(t+chosenDuration)}" } } }
    label(content,"Giờ hoạt động 06:00 – 23:00",11,muted,top=12)
    section(content,"Giá và thanh toán")
    val rate=input(content,"Giá thuê / giờ (đ)","350000",hourlyRate.toString(),InputType.TYPE_CLASS_NUMBER)
    val deposit=input(content,"Tiền cọc đã nhận (đ)","0","0",InputType.TYPE_CLASS_NUMBER)
    var method="Tiền mặt"; chips(content,listOf("Tiền mặt","Chuyển khoản"),method) { method=it }
    val note=input(content,"Ghi chú","Chuẩn bị áo phân đội, bóng…",multiline=true)
    notice(content,"Xác nhận sẽ tạo đơn đã chốt. Chỉ nhập tiền cọc bạn đã thực tế nhận được.")
    button(footer,"Xác nhận tạo lịch") {
        val n=name.text?.toString().orEmpty().trim(); val ph=phone.text?.toString().orEmpty().trim(); val r=rate.text?.toString().orEmpty().toIntOrNull(); val d=deposit.text?.toString().orEmpty().toIntOrNull()
        if(n.isEmpty()) { name.error="Nhập tên khách"; return@button }
        if(!ph.matches(Regex("0[0-9]{9}"))) { phone.error="Nhập số điện thoại hợp lệ"; return@button }
        if(r==null || r<1000 || r>10000000) { rate.error="Giá từ 1.000 đến 10.000.000đ"; return@button }
        val amount=(r.toLong()*chosenDuration/60).toInt()
        if(d==null || d<0 || d>amount) { deposit.error="Tiền cọc từ 0 đến ${money(amount)}"; return@button }
        if(!store.available(1,court,chosenDay,chosenStart,chosenDuration)) { toast("Giờ đặt không hợp lệ, đã có lịch hoặc đã qua"); return@button }
        val b=SnBooking(user=if(demo) "demo-customer" else store.account,pitchId=1,court=court,date=chosenDay.toString(),start=chosenStart,duration=chosenDuration,total=amount,name=n,phone=ph,note=note.text?.toString().orEmpty()+"\nNhận cọc: $method",deposit=d,status=if(demo) "DEMO" else "CONFIRMED")
        confirm("Xác nhận tạo lịch","${date(chosenDay)} · ${b.timeLabel}\nTổng ${money(amount)} · Đã nhận ${money(d)}") {
            store.save(b); selectedBooking=b.id; ownerSelectedDate=chosenDay; toast("Đã tạo lịch đặt"); go("owner_booking")
        }
    }
}

fun SanNhaActivity.ownerPitches() {
    title("Sân của tôi","Quản lý cụm Sân bóng Gia Định",false)
    val venue=card(content,padding=0); image(venue,R.drawable.pitch_giadinh,180)
    val info=column(14); label(info,"Sân bóng Gia Định",20,dark,true); label(info,"2A Phan Chu Trinh, Bình Thạnh",12,muted,top=8); pair(info,"${courts().size} sân đang hoạt động","06:00 – 23:00"); add(venue,info)
    val pricing=card(content,mint); menuRow(pricing,"Quản lý bảng giá","Giá theo giờ · Ngày thường & cuối tuần","wallet") { go("owner_prices") }
    section(content,"Danh sách sân","${courts().size} sân")
    courts().forEach { name ->
        val box=card(content); menuRow(box,name,"7 người · Cỏ nhân tạo","pitch") { court=name; go("owner_edit") }
        val active=store.venueSetting("active:$name",true)
        pair(box,if(active) "Đang hoạt động" else "Tạm ngừng", "${ownerBookings().count { it.court==name && it.date==LocalDate.now().toString() && it.status!="CANCELLED" }} lượt hôm nay")
        link(box,"Xem lịch →") { court=name; go("owner_schedule",true) }
    }
    button(content,"＋ Thêm sân mới") {
        val edit=EditText(this).apply { hint="Tên sân mới"; setSingleLine() }
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Thêm sân mới").setView(edit).setNegativeButton("Hủy",null).setPositiveButton("Thêm") { _,_ ->
            val name=edit.text?.toString().orEmpty().trim()
            if(name.isBlank() || name.contains(',') || name in courts()) toast("Tên sân chưa hợp lệ hoặc đã tồn tại")
            else { store.setVenue("courts",(courts()+name).joinToString(",")); court=name; render() }
        }.show()
    }
    bottom("owner_pitches")
}

fun SanNhaActivity.ownerEdit() {
    title("Chỉnh sửa sân","$court · Sân bóng Gia Định")
    section(content,"Hình ảnh sân"); val box=card(content,padding=0); image(box,R.drawable.pitch_giadinh,200)
    section(content,"Thông tin cơ bản")
    val name=input(content,"Tên sân *","Nhập tên sân",court)
    val type=input(content,"Loại sân *","Sân 7 người",store.venue("type:$court","Sân 7 người"))
    val surfaceType=input(content,"Mặt sân","Cỏ nhân tạo",store.venue("surface:$court","Cỏ nhân tạo"))
    section(content,"Tiện ích của sân"); chips(content,listOf("Đèn chiếu sáng","Bãi giữ xe","Nhà vệ sinh","Áo phân đội"),"",false) { }
    section(content,"Giờ hoạt động"); pair(content,"Mở cửa 06:00","Đóng cửa 23:00")
    val active=check(content,"Đang hoạt động · Cho phép nhận lịch đặt mới",store.venueSetting("active:$court",true))
    button(footer,"Lưu thay đổi") {
        val n=name.text?.toString().orEmpty().trim()
        if(n.isBlank() || n.contains(',') || (n!=court && n in courts())) { name.error="Tên sân rỗng, chứa dấu phẩy hoặc trùng tên"; return@button }
        if(n!=court) {
            store.setVenue("courts",courts().map { if(it==court) n else it }.joinToString(","))
            ownerBookings().filter { it.court==court }.forEach { store.save(it.copy(court=n)) }
        }
        store.setVenue("type:$n",type.text?.toString().orEmpty()); store.setVenue("surface:$n",surfaceType.text?.toString().orEmpty()); store.setVenueSetting("active:$n",active.isChecked)
        court=n; toast("Đã lưu thông tin sân"); go("owner_pitches",true)
    }
}

fun SanNhaActivity.ownerPrices() {
    title("Quản lý bảng giá","Sân bóng Gia Định")
    if(court !in courts()) court=courts().first()
    chips(content,courts(),court) { court=it; render() }
    notice(content,"Bảng giá đang áp dụng\nGiá có hiệu lực với các đơn đặt mới. Đơn đã xác nhận giữ nguyên giá.")
    section(content,"Giá thuê theo giờ","đ / 60 phút")
    listOf("06:00 – 12:00" to 250000,"12:00 – 16:00" to 300000,"16:00 – 23:00" to hourlyRate).forEachIndexed { index,(range,default) ->
        val box=card(content); label(box,range+if(index==2) "  ·  Cao điểm" else "",16,dark,true)
        val value=store.venue("price:$court:$index",default.toString()).toIntOrNull() ?: default
        pair(box,"Ngày thường",money(value),true,green); pair(box,"Cuối tuần",money(value+50000))
        link(box,"Chỉnh sửa") {
            val edit=EditText(this).apply { inputType=InputType.TYPE_CLASS_NUMBER; setText(value.toString()) }
            androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Giá thuê $range").setView(edit).setNegativeButton("Hủy",null).setPositiveButton("Lưu") { _,_ ->
                val v=edit.text?.toString().orEmpty().toIntOrNull()
                if(v==null || v !in 1000..10000000) toast("Giá không hợp lệ")
                else { store.setVenue("price:$court:$index",v.toString()); if(index==2) store.setVenue("rate:$court",v.toString()); render() }
            }.show()
        }
    }
    val rules=card(content,surface); label(rules,"Quy tắc tính tiền",16,dark,true)
    label(rules,"Tối thiểu 60 phút, thêm mỗi 30 phút tính 50% giá giờ. Lịch thủ công dùng giá/giờ bạn nhập tại thời điểm tạo.",13,muted,top=12)
    bottom("owner_pitches")
}

fun SanNhaActivity.ownerRevenue() {
    title("Doanh thu","Sân bóng Gia Định",false)
    val period=store.profile("revenue-period","Hôm nay")
    chips(content,listOf("Hôm nay","Tuần này","Tháng này"),period) { store.setProfile("revenue-period",it); render() }
    val now=LocalDate.now(); val from=when(period) { "Tuần này" -> now.minusDays((now.dayOfWeek.value-1).toLong()); "Tháng này" -> now.withDayOfMonth(1); else -> now }
    val list=ownerBookings().filter { it.status!="CANCELLED" && LocalDate.parse(it.date)>=from && LocalDate.parse(it.date)<=now }
    val expected=list.sumOf { it.total }; val received=list.sumOf { it.deposit }
    val summary=card(content,green,22); label(summary,"Doanh thu dự kiến · ${period.lowercase()}",14,Color.WHITE)
    label(summary,money(expected),32,Color.WHITE,true,18)
    pair(summary,"Đã thu",money(received),true,Color.WHITE); pair(summary,"Còn phải thu",money(expected-received),true,Color.WHITE)
    label(summary,"${list.size} lượt đã chốt · ${list.count { it.isPast }} lượt hoàn thành",12,Color.WHITE,top=14)
    if(demo) notice(content,"Đang hiển thị dữ liệu mẫu, không phải doanh thu thực tế.",true)
    section(content,"Doanh thu theo giờ")
    val chart=card(content,surface)
    val buckets=listOf(360 to 720,720 to 960,960 to 1200,1200 to 1380)
    val values=buckets.map { (a,b) -> list.filter { it.start in a until b }.sumOf { it.total } }
    val max=(values.maxOrNull() ?: 1).coerceAtLeast(1)
    val bars=row().apply { gravity=Gravity.BOTTOM }
    values.forEachIndexed { i,v ->
        val col=column(5); label(col,money(v),10,green,true).gravity=Gravity.CENTER
        add(col,android.view.View(this).apply { background=bg(if(i==2) green else Color.rgb(201,229,210),6); layoutParams=LinearLayout.LayoutParams(-1,dp((140L*v/max).toInt().coerceAtLeast(3))) },8)
        label(col,"${buckets[i].first/60}–${buckets[i].second/60}h",10,muted,top=8).gravity=Gravity.CENTER
        bars.addView(col,LinearLayout.LayoutParams(0,-2,1f))
    }; add(chart,bars,12)
    section(content,"Doanh thu theo sân","${list.size} lượt")
    val byCourt=card(content); courts().forEach { name -> pair(byCourt,"$name · ${list.count { it.court==name }} lượt",money(list.filter { it.court==name }.sumOf { it.total })) }
    section(content,"Giao dịch gần đây")
    list.filter { it.deposit>0 }.asReversed().take(10).forEach { b -> val box=card(content); menuRow(box,"${b.name} · ${b.court}","${b.date} · Đã nhận ${money(b.deposit)}","wallet") { selectedBooking=b.id; go("owner_booking") } }
    notice(content,"Không gồm đơn đã hủy. Đã thu là số tiền chủ sân ghi nhận trên thiết bị.")
    button(content,"Xuất báo cáo",false) {
        val report="Sân Nhà · $period\n${date(from)} – ${date(now)}\nDự kiến: ${money(expected)}\nĐã thu: ${money(received)}\nCòn thu: ${money(expected-received)}\n${list.size} lượt đặt"
        val share=android.content.Intent(android.content.Intent.ACTION_SEND).apply { type="text/plain"; putExtra(android.content.Intent.EXTRA_TEXT,report) }
        startActivity(android.content.Intent.createChooser(share,"Chia sẻ báo cáo"))
    }
    bottom("owner_revenue")
}

fun SanNhaActivity.ownerAccount() {
    title("Tài khoản chủ sân","Thông tin cá nhân và cài đặt vận hành",false)
    val profile=card(content); menuRow(profile,store.profile("name","Nguyễn Anh Tuấn"),if(demo) "Chủ sân · Đang xem thử" else "Chủ sân","user") { go("profile") }; pair(profile,"Số điện thoại",store.profile("phone")); label(profile,store.profile("email"),12,muted,top=8)
    section(content,"Cụm sân của tôi","Chỉnh sửa") { go("owner_pitches",true) }
    val venue=card(content); label(venue,"Sân bóng Gia Định",18,dark,true); label(venue,"2A Phan Chu Trinh, Bình Thạnh",12,muted,top=8); pair(venue,"${courts().size} sân 7 người","06:00 – 23:00")
    section(content,"Nhận thanh toán","Thay đổi") {
        val form=column(20); val bank=input(form,"Ngân hàng","Tên ngân hàng",store.profile("bank")); val number=input(form,"Số tài khoản","Nhập số tài khoản",store.profile("bankNumber"),InputType.TYPE_CLASS_NUMBER)
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Tài khoản nhận cọc").setView(form).setNegativeButton("Hủy",null).setPositiveButton("Lưu") { _,_ -> store.setProfile("bank",bank.text?.toString().orEmpty()); store.setProfile("bankNumber",number.text?.toString().orEmpty()); render() }.show()
    }
    val payment=card(content); label(payment,store.profile("bank","Chưa thiết lập ngân hàng"),17,green,true); pair(payment,"Số tài khoản",store.profile("bankNumber","Chưa có")); label(payment,"Ứng dụng chưa kết nối ngân hàng, không tự động nhận hoặc xác minh tiền cọc.",12,muted,top=12)
    section(content,"Cài đặt thông báo")
    val settings=card(content)
    listOf(Triple("newBookings","Lịch đặt mới","Nhận khi có yêu cầu hoặc thay đổi lịch"),Triple("paymentNotices","Thanh toán","Nhận thông báo tiền cọc và thu tiền"),Triple("dailyReport","Báo cáo cuối ngày","Tùy chọn lưu trên thiết bị")).forEach { (key,title,sub) -> toggle(settings,title,sub,store.setting(key,key!="dailyReport")) { store.setSetting(key,it) } }
    section(content,"Hỗ trợ"); val support=card(content); menuRow(support,"Liên hệ Sân Nhà","Thông tin và câu hỏi thường gặp","phone") { go("help") }; menuRow(support,"Hướng dẫn quản lý sân",name="info") { go("help") }
    button(content,"Đăng xuất",false,true) { logout() }; label(content,"Sân Nhà · Phiên bản 1.0.0",11,muted,top=16).gravity=Gravity.CENTER
    bottom("owner_account")
}
