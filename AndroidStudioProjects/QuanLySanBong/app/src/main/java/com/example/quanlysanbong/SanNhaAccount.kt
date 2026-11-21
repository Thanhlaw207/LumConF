package com.example.quanlysanbong

import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.widget.*

fun SanNhaActivity.offers() {
    title("Ưu đãi cho kèo hay","Thêm niềm vui, bớt tiền sân.")
    val code=input(content,"Bạn có mã ưu đãi?","Nhập mã ưu đãi")
    button(content,"Kiểm tra mã",false) { val s=code.text?.toString().orEmpty().trim().uppercase(); toast(if(s in listOf("SAN50","RASAN20")) "Mã hợp lệ trong bản thử nghiệm. Áp dụng tại bước kiểm tra lịch đặt." else "Không tìm thấy mã ưu đãi") }
    listOf(Triple("SAN50",50000,500000),Triple("RASAN20",20000,300000)).forEach { (id,amount,min) ->
        val box=card(content); val header=card(box,mint,14,0)
        label(header,"Giảm ${money(amount)}",22,green,true); label(header,id,14,green,true,8)
        label(box,"Đơn từ ${money(min)}, giảm trực tiếp tiền sân. Một mã cho mỗi đơn. Không cộng dồn mã khác.",13,muted,top=16)
        label(box,"Mã dùng để thử luồng đặt sân trên thiết bị.",11,muted,top=12)
        button(box,"Sử dụng ưu đãi") { discount=id; go("home",true) }
    }
    section(content,"Ưu đãi đã hết hạn")
    val old=card(content,surface); label(old,"Giảm 30.000đ",20,muted,true); label(old,"THANG10 · Đã hết hạn",13,muted,top=10)
    bottom("account")
}

fun SanNhaActivity.notifications() {
    title("Thông báo","Không bỏ lỡ lịch đá và tin mới từ Sân Nhà.")
    val list=store.bookings().filter { if(ownerRole) true else it.user==store.account }
    section(content,if(store.setting("notificationsRead")) "Đã đọc tất cả" else "${list.size} thông báo","Đọc tất cả") { store.setSetting("notificationsRead",true); render() }
    if(list.isEmpty()) notice(content,"Chưa có thông báo. Các cập nhật lịch đặt trên thiết bị sẽ xuất hiện ở đây.")
    list.asReversed().forEach { b ->
        val box=card(content,if(store.setting("notificationsRead")) surface else mint)
        menuRow(box,when(b.status) { "CANCELLED" -> "Lịch đặt đã hủy"; "DEMO" -> "Đã tạo lịch thử nghiệm"; else -> "Lịch đặt đã được xác nhận" },"${b.id} · ${b.date} · ${b.timeLabel}","calendar") { selectedBooking=b.id; go(if(ownerRole) "owner_booking" else "booking") }
        if(b.changeDate.isNotBlank()) menuRow(box,"Yêu cầu đổi lịch đang chờ duyệt","Đề xuất ${b.changeDate} · ${clock(b.changeStart)}","clock") { selectedBooking=b.id; go(if(ownerRole) "owner_booking" else "booking") }
    }
    bottom(if(ownerRole) "owner_account" else "account")
}

fun SanNhaActivity.account() {
    title("Tài khoản của tôi","Chào ${store.profile("name","Minh").substringAfterLast(' ')}, hôm nay lại thêm một kèo vui nhé!")
    val box=card(content,mint,20)
    label(box,store.profile("name","Nguyễn Minh"),23,green,true).gravity=Gravity.CENTER
    label(box,store.profile("phone"),14,muted,top=12).gravity=Gravity.CENTER
    label(box,if(demo) "Người đặt sân · Đang xem thử" else "Người đặt sân",12,green,top=10).gravity=Gravity.CENTER
    button(box,"Chỉnh sửa hồ sơ",false) { go("profile") }
    val next=store.bookings().count { it.user==store.account && !it.isPast && it.status!="CANCELLED" }
    val stats=row()
    listOf(next.toString() to "Lịch sắp tới",store.pitches.count { store.favorite(it.id) }.toString() to "Sân yêu thích","2" to "Ưu đãi thử").forEach { (n,t) ->
        val c=column(8); label(c,n,24,green,true).gravity=Gravity.CENTER; label(c,t,10,muted,top=8).gravity=Gravity.CENTER; stats.addView(c,LinearLayout.LayoutParams(0,-2,1f))
    }; add(content,stats,18)
    val menus=card(content); label(menus,"Dành cho bạn",16,dark,true)
    menuRow(menus,"Quản lý thông tin cá nhân","Họ tên, số điện thoại và email","user") { go("profile") }
    menuRow(menus,"Lịch đặt của tôi","Sắp tới, hoàn thành và đã hủy","calendar") { go("bookings",true) }
    menuRow(menus,"Sân yêu thích",name="heart") { go("favorites",true) }
    menuRow(menus,"Ưu đãi của tôi","Thêm niềm vui, bớt tiền sân","ticket") { go("offers") }
    menuRow(menus,"Thông báo",name="bell") { go("notifications") }
    val support=card(content); label(support,"Hỗ trợ và bảo mật",16,dark,true)
    menuRow(support,"Trung tâm trợ giúp","Câu hỏi thường gặp và liên hệ","info") { go("help") }
    menuRow(support,"Mật khẩu và bảo mật","Đổi mật khẩu, bảo vệ tài khoản","lock") { go("security") }
    menuRow(support,"Điều khoản và quyền riêng tư",name="shield") { terms() }
    button(content,"Đăng xuất",false,true) { logout() }
    label(content,"Sân Nhà · Phiên bản 1.0.0",11,muted,top=18).gravity=Gravity.CENTER
    bottom("account")
}

fun SanNhaActivity.profile() {
    title("Thông tin của bạn","Cập nhật để sân có thể liên hệ khi cần.")
    val avatar=label(content,store.profile("name","M").substringAfterLast(' ').take(1),34,green,true,28)
    avatar.gravity=Gravity.CENTER; avatar.background=bg(mint,70); avatar.layoutParams=LinearLayout.LayoutParams(dp(92),dp(92)).apply { gravity=Gravity.CENTER_HORIZONTAL; topMargin=dp(24) }
    label(content,"Hồ sơ cá nhân",13,muted,top=16).gravity=Gravity.CENTER
    val name=input(content,"Họ và tên","Nhập họ và tên",store.profile("name"))
    val phone=input(content,"Số điện thoại","Nhập số điện thoại",store.profile("phone"),InputType.TYPE_CLASS_PHONE)
    if(!demo) { phone.isEnabled=false; label(content,"Liên hệ hỗ trợ để thay số điện thoại đăng nhập.",11,muted,top=8) }
    val email=input(content,"Email (không bắt buộc)","Nhập email",store.profile("email"),InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
    notice(content,"Hồ sơ được bảo vệ\nSân chỉ nhận tên và số điện thoại phục vụ lịch đặt. Thông tin thanh toán không hiển thị tại đây.")
    label(content,"Hãy dùng tên thật để dễ nhận sân. Thông tin cập nhật không làm thay đổi các lịch đã xác nhận.",12,muted,top=20)
    button(footer,"Lưu thay đổi") {
        if(name.text?.toString().orEmpty().trim().isBlank()) { name.error="Nhập họ và tên"; return@button }
        val ph=phone.text?.toString().orEmpty().replace(" ","")
        if(!ph.matches(Regex("0[0-9]{9}"))) { phone.error="Số điện thoại chưa hợp lệ"; return@button }
        val em=email.text?.toString().orEmpty().trim()
        if(em.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()) { email.error="Email chưa hợp lệ"; return@button }
        store.setProfile("name",name.text?.toString().orEmpty().trim()); store.setProfile("phone",ph); store.setProfile("email",em)
        if(!demo) {
            val db=DatabaseHelper(this)
            db.writableDatabase.update(DatabaseHelper.TABLE_NGUOI_DUNG,android.content.ContentValues().apply {
                put(DatabaseHelper.COLUMN_HO_TEN,name.text?.toString().orEmpty().trim())
            },"${DatabaseHelper.COLUMN_TAI_KHOAN} = ?",arrayOf(store.account))
            db.close()
        }
        toast("Đã lưu hồ sơ"); back()
    }
}

fun SanNhaActivity.review() {
    val b=currentBooking ?: return
    title("Trận vừa rồi thế nào?","Chia sẻ thật để đồng đội chọn sân tốt hơn.")
    bookingSummary(content,b)
    if(!b.isPast || b.status=="CANCELLED") { notice(content,"Bạn chỉ có thể đánh giá sau khi trận đấu hoàn thành."); return }
    section(content,"Bạn hài lòng đến mức nào?")
    val rating=RatingBar(this).apply { numStars=5; stepSize=1f; this.rating=5f; progressTintList=android.content.res.ColorStateList.valueOf(gold); contentDescription="Điểm đánh giá" }
    add(content,rating,14); rating.layoutParams=LinearLayout.LayoutParams(-2,-2).apply { gravity=Gravity.CENTER_HORIZONTAL }
    section(content,"Điều bạn thích ở sân")
    chips(content,listOf("Mặt cỏ tốt","Đèn sáng","Đúng giờ","Nhân viên thân thiện","Giá hợp lý","Tiện ích sạch sẽ"),"",false) { }
    val comment=input(content,"Nhận xét của bạn","Chia sẻ trải nghiệm của bạn",multiline=true)
    comment.filters=arrayOf(android.text.InputFilter.LengthFilter(500))
    notice(content,"Đánh giá được lưu trên thiết bị trong bản này. Không chia sẻ thông tin cá nhân trong nhận xét.")
    button(footer,"Gửi đánh giá") {
        if(rating.rating<1) { toast("Hãy chọn số sao"); return@button }
        if(comment.text?.toString().orEmpty().trim().length<10) { comment.error="Nhập nhận xét ít nhất 10 ký tự"; return@button }
        store.setProfile("review:${b.id}","${rating.rating}|${comment.text}"); store.save(b.copy(reviewed=true)); toast("Đã lưu đánh giá"); go("bookings",true)
    }
}

fun SanNhaActivity.help() {
    title("Sân Nhà luôn ở đây","Bạn cần hỗ trợ điều gì? Tụi mình cùng giải quyết.")
    val search=input(content,"Tìm câu hỏi","Tìm về đặt sân, cọc, đổi lịch…")
    section(content,"Chọn chủ đề")
    chips(content,listOf("Đặt sân","Thanh toán cọc","Đổi và hủy lịch","Hoàn tiền"),"",false) { }
    val faq=listOf(
        "Khi nào lịch đặt được xác nhận?" to "Khi cọc đã thanh toán thành công và sân xác nhận. Bản thử nghiệm hiện chỉ lưu lịch trên thiết bị, chưa gửi tới sân.",
        "Đã trừ tiền nhưng đơn vẫn chờ?" to "Không thanh toán lại. Lưu mã giao dịch và liên hệ sân để kiểm tra. Bản ứng dụng này chưa có chức năng thu tiền.",
        "Đổi lịch có cần sân duyệt không?" to "Có. Lịch cũ vẫn được giữ cho đến khi chủ sân duyệt. Nếu khung giờ mới không còn trống, yêu cầu không được chấp nhận.",
        "Bao lâu tôi nhận lại tiền cọc?" to "Thời gian hoàn tùy phương thức và đơn vị xử lý. Chính sách mẫu trong thiết kế là 3–5 ngày làm việc, cần được sân xác nhận."
    )
    val answers=column(); add(content,answers)
    fun showFaq(query: String) {
        answers.removeAllViews()
        faq.filter { (it.first+it.second).contains(query,true) }.forEach { (q,a) ->
            val box=card(answers); val answer=text(a,13,muted).apply { visibility=android.view.View.GONE }
            menuRow(box,q,name="info") { answer.visibility=if(answer.visibility==android.view.View.GONE) android.view.View.VISIBLE else android.view.View.GONE }
            add(box,answer,10)
        }
    }
    showFaq("")
    search.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?,st: Int,c: Int,a: Int) {}
        override fun onTextChanged(s: CharSequence?,st: Int,b: Int,c: Int) { showFaq(s.toString()) }
        override fun afterTextChanged(e: android.text.Editable?) {}
    })
    val support=card(content,mint); label(support,"Liên hệ hỗ trợ",18,dark,true)
    label(support,"Thông tin liên hệ bên dưới lấy từ mẫu thiết kế, cần được đơn vị vận hành xác nhận trước khi sử dụng.",12,muted,top=12)
    button(support,"Email hỗ trợ",false) { external("mailto:hotro@sannha.vn") }
    notice(content,"Cần hỗ trợ cho lịch sắp tới?\nChuẩn bị mã đặt sân để chủ sân kiểm tra nhanh.")
    if(store.account.isNotBlank()) bottom(if(ownerRole) "owner_account" else "account")
}

fun SanNhaActivity.security() {
    title("Mật khẩu và bảo mật","Bảo vệ tài khoản của bạn.")
    if(demo) { notice(content,"Bạn đang xem thử. Hãy đăng ký tài khoản để đổi mật khẩu."); return }
    val current=input(content,"Mật khẩu hiện tại","Nhập mật khẩu hiện tại",type=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    val next=input(content,"Mật khẩu mới","Tối thiểu 8 ký tự, gồm chữ và số",type=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    val repeat=input(content,"Xác nhận mật khẩu","Nhập lại mật khẩu mới",type=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    button(footer,"Đổi mật khẩu") {
        val db=DatabaseHelper(this)
        if(db.dangNhap(store.account,current.text?.toString().orEmpty())==null) { current.error="Mật khẩu hiện tại không đúng"; return@button }
        val pass=next.text?.toString().orEmpty()
        if(pass.length<8 || !pass.any { it.isLetter() } || !pass.any { it.isDigit() }) { next.error="Cần ít nhất 8 ký tự, gồm chữ và số"; return@button }
        if(pass!=repeat.text?.toString().orEmpty()) { repeat.error="Mật khẩu chưa khớp"; return@button }
        val values=android.content.ContentValues().apply { put(DatabaseHelper.COLUMN_MAT_KHAU,pass) }
        db.writableDatabase.update(DatabaseHelper.TABLE_NGUOI_DUNG,values,"${DatabaseHelper.COLUMN_TAI_KHOAN} = ?",arrayOf(store.account)); db.close(); toast("Đã đổi mật khẩu"); back()
    }
}

fun SanNhaActivity.terms() {
    androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Điều khoản và quyền riêng tư")
        .setMessage("Thông tin cá nhân và lịch đặt trong bản ứng dụng này được lưu trên thiết bị. Chưa kết nối máy chủ hoặc đơn vị thanh toán. Chính sách đổi/hủy trong thiết kế là mẫu và cần được sân xác nhận khi vận hành thực tế.")
        .setPositiveButton("Đã hiểu",null).show()
}

fun SanNhaActivity.logout() { confirm("Đăng xuất?","Bạn sẽ quay về màn hình đăng nhập.") { store.logout(); ownerRole=false; go("login",true) } }
