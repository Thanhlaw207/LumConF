package com.example.quanlysanbong

import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import java.time.LocalDate

fun SanNhaActivity.pitchCard(parent: LinearLayout, p: SnPitch, favoritePage: Boolean = false) {
    val box=card(parent,padding=12)
    val r=row()
    val photo=ImageView(this).apply { setImageResource(p.image); scaleType=ImageView.ScaleType.CENTER_CROP; background=bg(surface,10); clipToOutline=true; contentDescription=p.name; setOnClickListener { selectPitch(p.id) } }
    r.addView(photo,LinearLayout.LayoutParams(dp(100),dp(94)))
    val info=column().apply { setPadding(dp(12),0,0,0); setOnClickListener { selectPitch(p.id) } }
    label(info,p.name,16,dark,true); label(info,p.address,11,muted,top=6)
    label(info,"★ ${p.rating}  ·  ${p.distance.toString().replace('.',',')} km",11,gold,top=7)
    val hasSelectedTime=store.courts(p.id).any { store.available(p.id,it,day,start,duration) }
    label(info,if(favoritePage) "Mở cửa 06:00 – 23:00" else if(hasSelectedTime) "Trống ${clock(start)} – ${clock(start+duration)}" else "Chọn ngày và giờ trống khác",11,green,top=7)
    r.addView(info,LinearLayout.LayoutParams(0,-2,1f))
    r.addView(icon("heart",if(store.favorite(p.id)) green else muted,23).apply {
        contentDescription=if(store.favorite(p.id)) "Bỏ lưu ${p.name}" else "Lưu ${p.name}"
        setOnClickListener { store.toggleFavorite(p.id); render() }
    },LinearLayout.LayoutParams(dp(40),dp(48)))
    add(box,r)
    label(box,"Sân ${p.players} người",10,green,true,5)
    divider(box,10)
    val last=row(); last.addView(text(money(p.rate)+" / giờ",17,green,true),LinearLayout.LayoutParams(0,-2,1f))
    val buttonContainer=column(); button(buttonContainer,if(favoritePage) "Đặt lại" else "Đặt sân",top=0) { pitchId=p.id; players=p.players; court=store.courts(p.id).firstOrNull { store.available(p.id,it,day,start,duration) } ?: store.courts(p.id).firstOrNull().orEmpty(); go("slots") }.apply { textSize=13f; layoutParams=LinearLayout.LayoutParams(dp(92),dp(42)) }
    last.addView(buttonContainer,LinearLayout.LayoutParams(dp(92),-2)); add(box,last)
    if(favoritePage) link(box,"Bỏ lưu sân") { store.toggleFavorite(p.id); render() }
}

fun SanNhaActivity.home(search: Boolean) {
    if(search) title("Tìm sân trống","Sân đẹp, giờ hợp. Lên kèo ngay thôi!")
    else {
        label(content,"Chào ${store.profile("name","Minh").substringAfterLast(' ')},",13,muted,top=10)
        title("Hôm nay, đá sân nào?",back=false)
        label(content,"⌖  Bình Thạnh, TP. Hồ Chí Minh  ⌄",12,green,true,16).apply { setOnClickListener { go("filter") } }
    }
    if(!demo && !search) notice(content,"Đặt thử tại Sân bóng Gia Định: Sân 1, Sân 2, Sân 3 có lịch mẫu trong 7 ngày tới. Chọn ô Trống để đặt, ô Không trống không thể chọn.")
    val box=card(content,surface,16,20)
    val searchInput=input(box,if(search) "Tên sân hoặc địa điểm" else "","Tên sân, đường hoặc khu vực",query)
    val datesRow=row(); val dateBox=column(); label(dateBox,"Ngày đá",11,muted); label(dateBox,day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))+"  ▾",13,dark,true,7)
    dateBox.setOnClickListener { query=searchInput.text?.toString().orEmpty(); pickDate { day=it; render() } }
    datesRow.addView(dateBox,LinearLayout.LayoutParams(0,dp(60),1f))
    val timeBox=column(); label(timeBox,"Khung giờ",11,muted); label(timeBox,"${clock(start)} – ${clock(start+duration)}  ▾",13,dark,true,7)
    timeBox.setOnClickListener { query=searchInput.text?.toString().orEmpty(); pickTime { start=it; render() } }
    datesRow.addView(timeBox,LinearLayout.LayoutParams(0,dp(60),1f)); add(box,datesRow,14)
    label(box,"Loại sân",11,muted)
    chips(box,listOf("Sân 5 người","Sân 7 người","Sân 11 người"),"Sân $players người") { players=it.filter { c -> c.isDigit() }.toInt() }
    button(box,"⌕  Tìm sân trống") { query=searchInput.text?.toString().orEmpty().trim(); go("search") }
    if(search) {
        val actions=row(); val a=column(); button(a,"Bộ lọc",false,top=0) { go("filter") }; actions.addView(a,LinearLayout.LayoutParams(0,-2,1f)); val b=column(); button(b,"Bản đồ",false,top=0) { directions() }; actions.addView(b,LinearLayout.LayoutParams(0,-2,1f).apply { marginStart=dp(8) }); add(content,actions,16)
    }
    val results=store.pitches.filter { p -> !search || (p.players==players && p.distance<=radius && p.rate<=maxPrice &&
        (query.isBlank() || (p.name+" "+p.address).contains(query,true)) &&
        store.courts(p.id).any { store.available(p.id,it,day,start,duration) }) }.let { if(sortNearest) it.sortedBy { p -> p.distance } else it.sortedBy { p -> p.rate } }
    section(content,if(search) "${results.size} sân phù hợp" else "Sân gần bạn","Bản đồ") { directions() }
    label(content,if(search) "${results.size} sân trống theo lựa chọn của bạn" else "${results.size} địa điểm · Chọn sân để xem khung giờ trống",12,muted)
    link(content,if(sortNearest) "☷ Gần nhất    ·    Trong bán kính $radius km" else "☷ Giá thấp nhất    ·    Trong bán kính $radius km") {
        choose("Sắp xếp",listOf("Gần nhất","Giá thấp nhất")) { sortNearest=it=="Gần nhất"; render() }
    }
    if(results.isEmpty()) {
        notice(content,"Chưa có sân phù hợp. Hãy thử sân 7 người, đổi ngày/giờ hoặc mở rộng bộ lọc.")
        button(content,"Xem tất cả sân",false) { query=""; players=7; radius=5; maxPrice=500000; day=LocalDate.now().plusDays(1); start=1080; duration=90; go("home",true) }
    }
    results.forEach { pitchCard(content,it) }
    if(!search && results.isNotEmpty()) button(content,"Xem tất cả ${results.size} sân  →",false) { query=searchInput.text?.toString().orEmpty().trim(); go("search") }
    if(search) label(content,"Đã hiển thị tất cả sân phù hợp trong bán kính $radius km.",11,muted,top=20).gravity=Gravity.CENTER
    bottom("home")
}

fun SanNhaActivity.filters() {
    title("Lọc sân phù hợp","Chọn điều bạn cần cho trận đấu sắp tới.")
    section(content,"Khu vực")
    val city=input(content,"Thành phố","TP. Hồ Chí Minh","TP. Hồ Chí Minh")
    val district=input(content,"Quận / huyện","Bình Thạnh","Bình Thạnh")
    chips(content,listOf("Trong 3 km","Trong 5 km","Trong 10 km"),"Trong $radius km") { radius=it.filter { c -> c.isDigit() }.toInt() }
    section(content,"Loại sân")
    chips(content,listOf("5 người","7 người","11 người"),"$players người") { players=it.filter { c -> c.isDigit() }.toInt() }
    section(content,"Giá thuê mỗi giờ")
    val priceLabel=label(content,"Tối đa ${money(maxPrice)}",14,green,true)
    add(content,SeekBar(this).apply { max=19; progress=(maxPrice-50000)/50000; progressTintList=android.content.res.ColorStateList.valueOf(green)
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?,v: Int,user: Boolean) { maxPrice=(v+1)*50000; priceLabel.text="Tối đa ${money(maxPrice)}" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        }) },16)
    section(content,"Tiện ích")
    chips(content,listOf("Bãi giữ xe","Đèn chiếu sáng","Phòng thay đồ","Nước uống","Cho thuê giày","Wi-Fi"),"",false) { }
    section(content,"Ngày và giờ đá")
    button(content,date(day),false) { pickDate { day=it; render() } }
    button(content,"Bắt đầu ${clock(start)}  ·  $duration phút",false) { pickTime { start=it; choose("Thời lượng",listOf("60 phút","90 phút","120 phút")) { duration=it.substringBefore(' ').toInt(); render() } } }
    notice(content,"Chỉ hiển thị sân còn trống trong toàn bộ khung giờ bạn chọn.")
    button(footer,"Xem sân phù hợp") {
        if(!city.text?.toString().orEmpty().contains("Hồ Chí Minh",true)) { city.error="Hiện có dữ liệu tại TP. Hồ Chí Minh"; return@button }
        query=district.text?.toString().orEmpty().trim(); go("search")
    }
    link(footer,"Đặt lại") { radius=5; players=7; maxPrice=500000; query=""; render() }
}

fun SanNhaActivity.pitchDetails() {
    title("Chi tiết sân")
    val box=card(content,padding=0); image(box,pitch.image,220)
    section(content,pitch.name,if(store.favorite(pitchId)) "♥ Đã lưu" else "♡ Lưu sân") { store.toggleFavorite(pitchId); render() }
    label(content,"⌖ ${pitch.address}, TP. Hồ Chí Minh\nCách bạn ${pitch.distance} km",13,muted)
    label(content,"★ ${pitch.rating} đánh giá",14,gold,true,14)
    notice(content,"Sẵn sàng cho kèo tối\nChọn lịch để xem sân trống. Mặt cỏ nhân tạo, đèn sáng toàn sân.")
    section(content,"Tiện ích tại sân")
    chips(content,listOf("Bãi giữ xe","Đèn chiếu sáng","Phòng thay đồ","Nước uống","Wi-Fi","Cho thuê giày"),"",false) { }
    val info=card(content); label(info,"Thông tin sân",16,dark,true)
    pair(info,"Loại sân","5 và 7 người"); pair(info,"Giờ mở cửa","06:00 – 23:00 hằng ngày"); pair(info,"Liên hệ sân","028 3551 2468")
    button(info,"Gọi sân",false) { external("tel:02835512468") }
    section(content,"Cọc và chính sách đổi, hủy")
    label(content,"Hủy trước giờ đá ít nhất 24 giờ: hoàn 100% tiền cọc. Hủy dưới 24 giờ hoặc không đến: không hoàn cọc. Đổi lịch cần chủ sân xác nhận.",13,muted)
    label(content,"Phần thanh toán trong bản này dùng để xem giao diện, chưa thu tiền hoặc giữ chỗ trên hệ thống của sân.",12,muted,top=12)
    val review=card(content); label(review,"Người chơi nói gì?",16,dark,true); pair(review,"Hoàng Nam","5/5 ★")
    label(review,"“Cỏ êm, đèn sáng. Anh quản lý hỗ trợ nhiệt tình, đội mình sẽ quay lại!”",13,muted,top=12)
    pair(footer,"${money(pitch.rate)} / giờ","Đang mở cửa",true,green)
    button(footer,"Chọn lịch ra sân") { go("slots") }
}

fun SanNhaActivity.slots() {
    title("Chọn lịch ra sân","${pitch.name} · Giá rõ ràng, lịch dễ chọn.")
    dates(content) { day=it; render() }
    section(content,"Chọn sân")
    chips(content,courts().map { courtLabel(it) },courtLabel(court)) { chosen -> court=courts().first { courtLabel(it)==chosen }; render() }
    label(content,"${courtLabel(court)} · Cỏ nhân tạo · ${money(hourlyRate)} / giờ",12,muted,top=12)
    section(content,"Giờ bắt đầu")
    val times=if(showAllTimes) (360..1320 step 30).toList() else listOf(960,1020,1080,1170,1230,1260)
    times.chunked(3).forEach { group ->
        val r=row(); group.forEach { t ->
            val available=store.available(pitchId,court,day,t,duration)
            val selected=t==start && available
            val c=column(8).apply { background=bg(if(!available) line else if(selected) mint else surface,10,if(selected) green else line); isEnabled=available; minimumHeight=dp(66)
                contentDescription="${courtLabel(court)} · ${clock(t)} · ${if(!available) "Không trống" else if(selected) "Đang chọn" else "Trống"}"
                setOnClickListener { if(available) { start=t; render() } } }
            label(c,clock(t),15,if(available) dark else muted,true).gravity=Gravity.CENTER
            label(c,if(!available) "Không trống" else if(selected) "Đang chọn" else "Trống",10,if(available) green else muted,top=6).gravity=Gravity.CENTER
            r.addView(c,LinearLayout.LayoutParams(0,-2,1f).apply { marginEnd=dp(6) })
        }; add(content,r,8)
    }
    link(content,if(showAllTimes) "Thu gọn khung giờ" else "Xem toàn bộ giờ trong ngày") { showAllTimes=!showAllTimes; render() }
    label(content,"Giờ đã đặt không thể chọn. Khung đang chọn được viền xanh.",11,muted,top=8)
    section(content,"Thời lượng trận đấu")
    chips(content,listOf("60 phút","90 phút","120 phút"),"$duration phút") { duration=it.substringBefore(' ').toInt(); render() }
    val summary=card(content,mint); label(summary,"Lịch bạn đang chọn",16,dark,true)
    pair(summary,"Ngày đá",date(day)); pair(summary,"Sân","${courtLabel(court)} · $players người"); pair(summary,"Khung giờ","${clock(start)} – ${clock(start+duration)}")
    pair(summary,"Tiền sân · $duration phút",money(subtotal),true,green)
    notice(content,"Kiểm tra lại lịch trước khi tiếp tục. Việc đặt lịch trong ứng dụng hiện chỉ lưu trên thiết bị.")
    pair(footer,"Tạm tính · Chưa áp dụng ưu đãi",money(subtotal),true,green)
    button(footer,"Tiếp tục đặt sân  →") {
        if(!store.available(pitchId,court,day,start,duration)) { toast("Khung giờ này không còn trống. Hãy chọn giờ khác."); render() }
        else { if(contactName.isBlank()) contactName=store.profile("name"); if(contactPhone.isBlank()) contactPhone=store.profile("phone"); go("checkout") }
    }
}

fun SanNhaActivity.bookingSummary(parent: LinearLayout, booking: SnBooking? = null) {
    val p=booking?.let { store.pitches.first { p -> p.id==it.pitchId } } ?: pitch
    val box=card(parent); val r=row(); r.addView(ImageView(this).apply { setImageResource(p.image); scaleType=ImageView.ScaleType.CENTER_CROP; background=bg(surface,8); clipToOutline=true },LinearLayout.LayoutParams(dp(84),dp(66)))
    val c=column().apply { setPadding(dp(12),0,0,0) }; label(c,p.name,15,dark,true); label(c,"${courtLabel(booking?.court ?: court)} · 7 người",12,muted,top=6); label(c,p.address,11,muted,top=5)
    r.addView(c,LinearLayout.LayoutParams(0,-2,1f)); add(box,r); divider(box)
    label(box,date(booking?.let { LocalDate.parse(it.date) } ?: day),14,dark,true)
    label(box,"${booking?.timeLabel ?: "${clock(start)} – ${clock(start+duration)}"} · ${booking?.duration ?: duration} phút",13,muted,top=8)
}

fun SanNhaActivity.checkout() {
    title("Kiểm tra lịch đặt","Còn một bước nữa là chốt kèo cùng đồng đội.")
    bookingSummary(content); notice(content,"Đến trước 15 phút để nhận sân và khởi động.")
    section(content,"Thông tin liên hệ")
    val name=input(content,"Họ và tên","Nhập họ và tên",contactName)
    val phone=input(content,"Số điện thoại","Nhập số điện thoại",contactPhone,InputType.TYPE_CLASS_PHONE)
    val note=input(content,"Ghi chú cho sân (không bắt buộc)","Đội mình cần thuê 2 áo bib…",bookingNote,multiline=true)
    section(content,"Ưu đãi")
    val promo=input(content,"","Nhập mã ưu đãi",discount)
    button(content,"Áp dụng mã",false) {
        contactName=name.text?.toString().orEmpty(); contactPhone=phone.text?.toString().orEmpty(); bookingNote=note.text?.toString().orEmpty()
        val code=promo.text?.toString().orEmpty().trim().uppercase()
        if((code=="SAN50" && subtotal>=500000)||(code=="RASAN20" && subtotal>=300000)) { discount=code; render() }
        else { promo.error="Mã chưa phù hợp. SAN50: đơn từ 500.000đ; RASAN20: đơn từ 300.000đ" }
    }
    val pay=card(content); label(pay,"Chi tiết thanh toán",16,dark,true)
    pair(pay,"Tiền sân · $duration phút",money(subtotal)); pair(pay,"Ưu đãi ${discount}","−${money(reduction)}"); pair(pay,"Phí dịch vụ","0đ"); divider(pay)
    pair(pay,"Tổng thanh toán",money(total),true,green); pair(pay,"Cọc cần thanh toán",money(150000)); pair(pay,"Còn trả tại sân",money((total-150000).coerceAtLeast(0)))
    section(content,"Điều kiện đặt sân")
    label(content,"Hủy trước giờ đá ít nhất 24 giờ: hoàn 100% tiền cọc. Hủy dưới 24 giờ hoặc không đến: không hoàn cọc. Đổi lịch cần chủ sân xác nhận.",12,muted)
    val agree=check(content,"Tôi đã đọc và đồng ý điều khoản đặt sân, chính sách cọc và hủy của sân.",store.setting("agree-booking"))
    agree.setOnCheckedChangeListener { _,checked -> store.setSetting("agree-booking",checked) }
    button(footer,"Tiếp tục thanh toán cọc") {
        if(name.text?.toString().orEmpty().trim().isBlank()) { name.error="Nhập họ và tên"; return@button }
        if(!phone.text?.toString().orEmpty().replace(" ","").matches(Regex("0[0-9]{9}"))) { phone.error="Nhập số điện thoại hợp lệ"; return@button }
        if(!agree.isChecked) { toast("Vui lòng đồng ý điều khoản đặt sân"); return@button }
        contactName=name.text?.toString().orEmpty().trim(); contactPhone=phone.text?.toString().orEmpty().replace(" ",""); bookingNote=note.text?.toString().orEmpty()
        if(!store.available(pitchId,court,day,start,duration)) { toast("Khung giờ không còn trống"); go("slots") } else go("payment")
    }
}

fun SanNhaActivity.payment() {
    title("Thanh toán cọc","Chọn phương thức thanh toán")
    val amount=card(content,mint,24); label(amount,"Số tiền cần thanh toán",14,muted).gravity=Gravity.CENTER
    label(amount,"150.000đ",34,green,true,14).gravity=Gravity.CENTER
    label(amount,"Giữ chỗ sau khi cọc được xác nhận",12,green,top=12).gravity=Gravity.CENTER
    section(content,"Chọn phương thức")
    listOf("Ví MoMo","VNPAY","Chuyển khoản ngân hàng").forEach { method ->
        val b=card(content,if(method==paymentMethod) mint else Color.WHITE)
        menuRow(b,method,when(method) { "Ví MoMo" -> "Ví điện tử"; "VNPAY" -> "Ứng dụng ngân hàng / thẻ ATM nội địa"; else -> "Quét mã VietQR bằng ứng dụng ngân hàng" },"wallet") { paymentMethod=method; render() }
    }
    val how=card(content); label(how,"Thanh toán bằng $paymentMethod",16,dark,true)
    label(how,"1. Mở ứng dụng thanh toán.\n2. Kiểm tra người nhận và số tiền.\n3. Quay lại Sân Nhà để kiểm tra xác nhận.",13,muted,top=12)
    val summary=card(content); label(summary,"Tóm tắt đơn",16,dark,true); label(summary,"${pitch.name} · ${courtLabel(court)}",13,muted,top=10)
    label(summary,"${date(day)} · ${clock(start)} – ${clock(start+duration)}",12,muted,top=8)
    pair(summary,"Tổng sau giảm",money(total)); pair(summary,"Cọc thanh toán ngay","150.000đ"); pair(summary,"Còn trả tại sân",money((total-150000).coerceAtLeast(0)),true,green)
    notice(content,"Bản giao diện thử nghiệm: chưa kết nối MoMo, VNPAY hoặc ngân hàng. Không chuyển tiền. Xác nhận bên dưới chỉ tạo lịch thử trên thiết bị.",true)
    button(footer,"Xem thử thanh toán thành công") {
        if(!store.available(pitchId,court,day,start,duration)) { toast("Khung giờ vừa được đặt. Vui lòng chọn lại."); go("slots"); return@button }
        val b=SnBooking(user=store.account,pitchId=pitchId,court=court,date=day.toString(),start=start,duration=duration,total=total,name=contactName,phone=contactPhone,note=bookingNote,status="DEMO")
        store.save(b); selectedBooking=b.id; go("success")
    }
}

fun SanNhaActivity.success() {
    title("Chốt kèo thành công!","Sân đã sẵn sàng. Rủ đồng đội ra sân thôi!")
    val check=icon("check",green,72).apply { background=bg(mint,50); setPadding(dp(14),dp(14),dp(14),dp(14)) }
    add(content,check,30,16); check.layoutParams=LinearLayout.LayoutParams(dp(88),dp(88)).apply { gravity=Gravity.CENTER_HORIZONTAL }
    notice(content,"Lịch thử đã được lưu trên thiết bị · Chưa thu tiền hoặc xác nhận với sân.")
    currentBooking?.let { b -> bookingSummary(content,b); val box=card(content); pair(box,"Mã đặt sân",b.id); pair(box,"Tổng thanh toán",money(b.total),true,green); pair(box,"Tiền thực tế đã thu",money(b.deposit)); pair(box,"Còn phải trả",money(b.total-b.deposit)) }
    button(content,"Xem lịch đặt của tôi") { go("bookings",true) }
    button(content,"Về trang chủ",false) { go("home",true) }
}

fun SanNhaActivity.bookingList() {
    title("Lịch đặt của tôi","Theo dõi lịch đá và những trận đã qua.")
    chips(content,listOf("Sắp tới","Hoàn thành","Đã hủy"),bookingTab) { bookingTab=it; render() }
    val list=store.bookings().filter { it.user==store.account }.filter { when(bookingTab) {
        "Đã hủy" -> it.status=="CANCELLED"; "Hoàn thành" -> it.status!="CANCELLED" && it.isPast
        else -> it.status!="CANCELLED" && !it.isPast
    } }
    if(list.isEmpty()) { notice(content,"Chưa có lịch trong mục này. Tìm sân phù hợp để lên kèo cùng đồng đội."); button(content,"Tìm sân trống") { go("home",true) } }
    list.forEach { b ->
        val box=card(content); pair(box,b.id,if(b.status=="CANCELLED") "Đã hủy" else if(b.status=="DEMO") "Lịch thử" else if(b.isPast) "Hoàn thành" else "Đã xác nhận")
        bookingSummary(box,b)
        pair(box,"Đã thu ${money(b.deposit)}","Còn ${money(b.total-b.deposit)}")
        if(b.changeDate.isNotBlank()) notice(box,"Đề xuất ${b.changeDate} đang chờ chủ sân duyệt. Lịch cũ vẫn được giữ.")
        button(box,"Xem chi tiết",false) { selectedBooking=b.id; go("booking") }
        if(b.isPast && b.status!="CANCELLED" && !b.reviewed) link(box,"Đánh giá") { selectedBooking=b.id; go("review") }
    }
    bottom("bookings")
}

fun SanNhaActivity.bookingDetails() {
    val b=currentBooking ?: run { title("Chi tiết lịch đặt"); notice(content,"Chưa có lịch được chọn."); return }
    title("Chi tiết lịch đặt","Mã đặt sân ${b.id}")
    notice(content,when(b.status) { "DEMO" -> "Lịch thử · Chưa thanh toán"; "CANCELLED" -> "Đã hủy"; else -> "Đã xác nhận · Đã thu ${money(b.deposit)}" })
    bookingSummary(content,b)
    button(content,"Chỉ đường",false) { directions(store.pitches.first { it.id==b.pitchId }) }
    val person=card(content); label(person,"Người đặt sân",16,dark,true); pair(person,"Họ và tên",b.name); pair(person,"Số điện thoại",b.phone); label(person,"Ghi chú: ${b.note.ifBlank { "Không có" }}",13,muted,top=12)
    val pay=card(content); label(pay,"Chi tiết thanh toán",16,dark,true); pair(pay,"Tổng thanh toán",money(b.total),true,green); pair(pay,"Thực tế đã thu",money(b.deposit)); pair(pay,"Còn trả tại sân",money(b.total-b.deposit))
    if(b.changeDate.isNotBlank()) notice(content,"Yêu cầu đổi sang ${b.changeDate} · ${clock(b.changeStart)} đang chờ chủ sân duyệt. Lịch cũ vẫn có hiệu lực.",true)
    section(content,"Chính sách áp dụng"); label(content,"Hủy trước giờ đá ít nhất 24 giờ: hoàn 100% tiền cọc. Hủy dưới 24 giờ hoặc không đến: không hoàn cọc. Đổi lịch cần chủ sân xác nhận.",13,muted)
    if(b.status!="CANCELLED" && !b.isPast) {
        button(footer,"Yêu cầu đổi lịch",false) { pendingDate=LocalDate.parse(b.date); pendingStart=b.start; go("change") }
        link(footer,"Hủy đặt sân") { go("cancel") }
    } else if(b.isPast && b.status!="CANCELLED" && !b.reviewed) button(footer,"Đánh giá trận vừa rồi") { go("review") }
}

fun SanNhaActivity.changeBooking() {
    val b=currentBooking ?: return
    title("Yêu cầu đổi lịch","${b.id} · ${store.pitches.first { it.id==b.pitchId }.name}")
    bookingSummary(content,b)
    notice(content,"Lịch hiện tại vẫn được giữ cho đến khi chủ sân duyệt yêu cầu.")
    dates(content,pendingDate) { pendingDate=it; render() }
    button(content,"Giờ mới: ${clock(pendingStart)}",false) { pickTime(pendingStart) { pendingStart=it; render() } }
    val reason=input(content,"Lý do đổi lịch","Nhập lý do đổi lịch",multiline=true)
    button(footer,"Gửi yêu cầu đổi lịch") {
        if(reason.text?.toString().orEmpty().isBlank()) { reason.error="Vui lòng nhập lý do"; return@button }
        if(!store.available(b.pitchId,b.court,pendingDate,pendingStart,b.duration,b.id)) { toast("Khung giờ đề xuất không còn trống"); return@button }
        if(pendingDate.toString()==b.date && pendingStart==b.start) { toast("Hãy chọn lịch mới khác lịch hiện tại"); return@button }
        store.save(b.copy(changeDate=pendingDate.toString(),changeStart=pendingStart)); toast("Đã gửi yêu cầu đổi lịch"); go("booking")
    }
}

fun SanNhaActivity.cancelBooking() {
    val b=currentBooking ?: return
    title("Hủy lịch đặt này?","Kiểm tra chính sách và khoản hoàn trước khi xác nhận.")
    bookingSummary(content,b)
    section(content,"Lý do hủy")
    val reasons=RadioGroup(this); listOf("Đội không đủ người","Thay đổi kế hoạch","Đã đặt nhầm lịch","Lý do khác").forEachIndexed { i,s -> reasons.addView(RadioButton(this).apply { id=View.generateViewId(); text=s; textSize=14f; setTextColor(dark); isChecked=i==0; minimumHeight=dp(48) }) }; add(content,reasons)
    val refundable=java.time.LocalDateTime.of(LocalDate.parse(b.date),java.time.LocalTime.MIDNIGHT).plusMinutes(b.start.toLong()).minusHours(24)>java.time.LocalDateTime.now()
    val refund=if(refundable) b.deposit else 0
    notice(content,if(refundable) "Đủ điều kiện hoàn toàn bộ cọc · Hủy trước giờ đá ít nhất 24 giờ." else "Hủy trong vòng 24 giờ trước trận: tiền cọc không được hoàn.",!refundable)
    val box=card(content,mint); label(box,"Khoản hoàn dự kiến",16,dark,true); pair(box,"Tiền cọc đã thanh toán",money(b.deposit)); pair(box,"Hoàn lại cho bạn",money(refund),true,green)
    if(b.status=="DEMO") label(box,"Lịch thử chưa thu tiền, không phát sinh giao dịch hoàn tiền.",12,muted,top=12)
    notice(content,"Sau khi hủy, khung giờ sẽ được mở cho người khác.",true)
    button(footer,"Xác nhận hủy đặt sân",danger=true) {
        confirm("Xác nhận hủy?","Lịch ${b.id} sẽ bị hủy và khung giờ được mở lại.") { store.save(b.copy(status="CANCELLED",changeDate="")); go("bookings",true) }
    }
    link(footer,"Giữ lại lịch đặt") { back() }
}

fun SanNhaActivity.favorites() {
    title("Sân quen, kèo mới","Lưu sân ưng ý để lần sau đặt nhanh hơn.")
    val saved=store.pitches.filter { store.favorite(it.id) }; section(content,"${saved.size} sân đã lưu")
    notice(content,"Giá dưới đây tính theo giờ. Chọn ngày và giờ để kiểm tra sân trống trước khi đặt.")
    if(saved.isEmpty()) { label(content,"Chưa có sân yêu thích. Chạm trái tim ở thẻ sân để lưu.",14,muted,top=20); button(content,"Khám phá sân") { go("home",true) } }
    saved.forEach { pitchCard(content,it,true) }; bottom("favorites")
}
