package com.example.quanlysanbong

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.LocalDate
import java.time.LocalTime

class SanNhaActivity : SanNhaUi() {
    lateinit var store: SanNhaStore
    lateinit var content: LinearLayout
    lateinit var footer: LinearLayout
    lateinit var navigation: LinearLayout
    var screen = "login"
    val history = mutableListOf<String>()
    var ownerRole = false
    var pitchId = 1
    var court = "Sân 1"
    var day: LocalDate = LocalDate.now().plusDays(1)
    var start = 18 * 60
    var duration = 90
    var players = 7
    var query = ""
    var radius = 5
    var maxPrice = 500000
    var sortNearest = true
    var contactName = ""
    var contactPhone = ""
    var bookingNote = ""
    var discount = ""
    var paymentMethod = "Ví MoMo"
    var selectedBooking = ""
    var bookingTab = "Sắp tới"
    var pendingDate: LocalDate = day
    var pendingStart = start
    var showAllTimes = false
    var ownerSelectedDate: LocalDate = LocalDate.now()
    val pitch get() = store.pitches.first { it.id == pitchId }
    val currentBooking get() = store.bookings().find { it.id == selectedBooking }
    val hourlyRate get() = if(pitchId==1) store.venue("rate:$court",pitch.rate.toString()).toIntOrNull() ?: pitch.rate else pitch.rate
    val subtotal get() = if(start>=360 && start+duration<=1380) store.quote(pitchId,court,day,start,duration) else hourlyRate*duration/60
    val reduction get() = when { discount == "SAN50" && subtotal >= 500000 -> 50000; discount == "RASAN20" && subtotal >= 300000 -> 20000; else -> 0 }
    val total get() = subtotal-reduction
    val demo get() = store.account.startsWith("demo-")
    fun money(value: Int) = SanNhaStore.money(value)
    fun date(value: LocalDate) = SanNhaStore.date(value)
    fun clock(value: Int) = SnBooking.clock(value)
    fun toast(value: String) = Toast.makeText(this,value,Toast.LENGTH_SHORT).show()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        store=SanNhaStore(this)
        ownerRole=store.owner
        if(savedInstanceState!=null) {
            screen=savedInstanceState.getString("screen","login"); pitchId=savedInstanceState.getInt("pitch",1)
            court=savedInstanceState.getString("court","Sân 1"); day=LocalDate.parse(savedInstanceState.getString("day",day.toString()))
            start=savedInstanceState.getInt("start",start); duration=savedInstanceState.getInt("duration",90)
            selectedBooking=savedInstanceState.getString("booking",""); history.addAll(savedInstanceState.getStringArrayList("history") ?: emptyList())
            ownerRole=savedInstanceState.getBoolean("ownerRole",ownerRole)
            contactName=savedInstanceState.getString("contactName",""); contactPhone=savedInstanceState.getString("contactPhone","")
            bookingNote=savedInstanceState.getString("note",""); discount=savedInstanceState.getString("discount","")
            ownerSelectedDate=LocalDate.parse(savedInstanceState.getString("ownerDate",ownerSelectedDate.toString()))
            pendingDate=LocalDate.parse(savedInstanceState.getString("pendingDate",day.toString())); pendingStart=savedInstanceState.getInt("pendingStart",start)
            players=savedInstanceState.getInt("players",7); radius=savedInstanceState.getInt("radius",5); maxPrice=savedInstanceState.getInt("maxPrice",500000)
            query=savedInstanceState.getString("query",""); paymentMethod=savedInstanceState.getString("paymentMethod","Ví MoMo")
        } else screen=if(store.account.isNotBlank() && store.remember) { if(ownerRole) "owner_schedule" else "home" } else "login"
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { back() }
        })
        render()
    }
    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putString("screen",screen); out.putInt("pitch",pitchId); out.putString("court",court); out.putString("day",day.toString())
        out.putInt("start",start); out.putInt("duration",duration); out.putString("booking",selectedBooking)
        out.putStringArrayList("history",ArrayList(history)); out.putBoolean("ownerRole",ownerRole)
        out.putString("contactName",contactName); out.putString("contactPhone",contactPhone); out.putString("note",bookingNote); out.putString("discount",discount)
        out.putString("ownerDate",ownerSelectedDate.toString()); out.putString("pendingDate",pendingDate.toString()); out.putInt("pendingStart",pendingStart)
        out.putInt("players",players); out.putInt("radius",radius); out.putInt("maxPrice",maxPrice); out.putString("query",query); out.putString("paymentMethod",paymentMethod)
    }
    fun go(route: String, root: Boolean = false) {
        currentFocus?.let { (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(it.windowToken,0) }
        if(root) history.clear() else if(route!=screen) history.add(screen)
        screen=route; render()
    }
    fun back() {
        if(history.isNotEmpty()) { screen=history.removeAt(history.lastIndex); render() }
        else if(screen !in listOf("login","home","owner_schedule")) go(if(ownerRole) "owner_schedule" else "home",true)
        else finish()
    }
    fun render() {
        store.seedMockSchedule()
        setContentView(R.layout.activity_san_nha)
        val root=findViewById<LinearLayout>(R.id.sanNhaRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v,ins ->
            val bars=ins.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom); ins
        }
        content=findViewById(R.id.snContent); footer=findViewById(R.id.snFooter); navigation=findViewById(R.id.snNavigation)
        header(findViewById(R.id.snHeader))
        when(screen) {
            "login" -> auth(false)
            "register" -> auth(true)
            "home","search" -> home(screen=="search")
            "filter" -> filters()
            "pitch" -> pitchDetails()
            "slots" -> slots()
            "checkout" -> checkout()
            "payment" -> payment()
            "success" -> success()
            "bookings" -> bookingList()
            "booking" -> bookingDetails()
            "change" -> changeBooking()
            "cancel" -> cancelBooking()
            "favorites" -> favorites()
            "offers" -> offers()
            "notifications" -> notifications()
            "review" -> review()
            "account" -> account()
            "profile" -> profile()
            "help" -> help()
            "security" -> security()
            "owner_schedule" -> ownerSchedule()
            "owner_booking" -> ownerBooking()
            "owner_manual" -> ownerManual()
            "owner_pitches" -> ownerPitches()
            "owner_edit" -> ownerEdit()
            "owner_prices" -> ownerPrices()
            "owner_revenue" -> ownerRevenue()
            "owner_account" -> ownerAccount()
        }
        if(footer.childCount==0) footer.visibility=View.GONE
        if(navigation.childCount==0) navigation.visibility=View.GONE
        ViewCompat.requestApplyInsets(root)
    }
    private fun header(frame: FrameLayout) {
        val r=row().apply { setPadding(dp(24),0,dp(24),0) }
        val logo=FrameLayout(this).apply { background=bg(green,9); addView(icon("pitch",Color.WHITE,27),FrameLayout.LayoutParams(dp(27),dp(27),Gravity.CENTER)); setOnClickListener { if(screen !in listOf("login","register")) go(if(ownerRole) "owner_schedule" else "home",true) } }
        r.addView(logo,LinearLayout.LayoutParams(dp(36),dp(36)))
        r.addView(text("sân nhà",24,green,true).apply { setPadding(dp(10),0,0,0) },LinearLayout.LayoutParams(0,-2,1f))
        if(ownerRole && screen !in listOf("login","register")) r.addView(text("Chủ sân",10,green,true).apply { background=bg(mint,6); setPadding(dp(6),dp(5),dp(6),dp(5)) },LinearLayout.LayoutParams(-2,-2))
        val action=if(screen in listOf("home","owner_schedule","owner_pitches","owner_revenue","owner_account")) icon("bell",dark) else text("⊙ Trợ giúp",12,green,true)
        if(action is TextView) action.gravity=Gravity.CENTER
        action.setOnClickListener { go(if(screen in listOf("home","owner_schedule","owner_pitches","owner_revenue","owner_account")) "notifications" else "help") }
        r.addView(action,LinearLayout.LayoutParams(dp(if(action is SnIcon) 48 else 82),dp(48)))
        frame.addView(r,FrameLayout.LayoutParams(-1,-1))
    }
    fun bottom(selected: String) {
        val items=if(ownerRole) listOf(Triple("owner_schedule","Trang chủ","home"),Triple("owner_schedule","Lịch sân","calendar"),Triple("owner_pitches","Sân của tôi","pitch"),Triple("owner_revenue","Doanh thu","chart"),Triple("owner_account","Tài khoản","user"))
        else listOf(Triple("home","Trang chủ","home"),Triple("bookings","Lịch đặt","calendar"),Triple("favorites","Yêu thích","heart"),Triple("account","Tài khoản","user"))
        items.forEachIndexed { index,(route,title,name) ->
            val active=route==selected && !(ownerRole && index==0)
            val c=column().apply { gravity=Gravity.CENTER; setPadding(0,dp(4),0,dp(4)); minimumHeight=dp(54); setOnClickListener { go(route,true) } }
            c.addView(icon(name,if(active) green else muted,23))
            c.addView(text(title,if(ownerRole) 9 else 10,if(active) green else muted,active).apply { gravity=Gravity.CENTER; setPadding(0,dp(5),0,0) })
            navigation.addView(c,LinearLayout.LayoutParams(0,-2,1f))
        }
        navigation.background=bg(Color.WHITE,0,line)
    }
    fun title(value: String, sub: String = "", back: Boolean = true) = heading(content,value,sub,if(back) ({ back() }) else null)
    fun pickDate(value: LocalDate = day, minimum: Boolean = true, result: (LocalDate)->Unit) {
        DatePickerDialog(this,{_,y,m,d -> result(LocalDate.of(y,m+1,d))},value.year,value.monthValue-1,value.dayOfMonth).apply {
            if(minimum) datePicker.minDate=System.currentTimeMillis()-1000
        }.show()
    }
    fun pickTime(value: Int = start, result: (Int)->Unit) { TimePickerDialog(this,{_,h,m -> result(h*60+m)},value/60,value%60,true).show() }
    fun choose(title: String, values: List<String>, result: (String)->Unit) { AlertDialog.Builder(this).setTitle(title).setItems(values.toTypedArray()) { _,i -> result(values[i]) }.setNegativeButton("Đóng",null).show() }
    fun confirm(title: String, message: String, action: () -> Unit) { AlertDialog.Builder(this).setTitle(title).setMessage(message).setNegativeButton("Quay lại",null).setPositiveButton("Xác nhận") { _,_ -> action() }.show() }
    fun external(uri: String) { try { startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(uri))) } catch(_: android.content.ActivityNotFoundException) { toast("Chưa có ứng dụng phù hợp trên thiết bị.") } }
    fun directions(p: SnPitch = pitch) = external("geo:0,0?q="+Uri.encode(p.name+" "+p.address+" TP. Hồ Chí Minh"))
    fun selectPitch(id: Int) { pitchId=id; players=pitch.players; court=store.courts(id).firstOrNull().orEmpty(); go("pitch") }
    fun courtLabel(value: String) = value
    fun dates(parent: LinearLayout, selected: LocalDate = day, change: (LocalDate)->Unit) {
        val r=row()
        val beginning=if(selected<LocalDate.now()) selected else LocalDate.now()
        (0..4).forEach { offset ->
            val d=beginning.plusDays(offset.toLong()); val c=column(6).apply {
                gravity=Gravity.CENTER; background=bg(if(d==selected) green else surface,10,if(d==selected) green else line)
                setOnClickListener { change(d) }
            }
            label(c,d.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT,SanNhaStore.vi),11,if(d==selected) Color.WHITE else muted).gravity=Gravity.CENTER
            label(c,d.dayOfMonth.toString(),20,if(d==selected) Color.WHITE else dark,true,7).gravity=Gravity.CENTER
            r.addView(c,LinearLayout.LayoutParams(0,dp(72),1f).apply { marginEnd=dp(5) })
        }; add(parent,r,14)
        link(parent,"${date(selected)}  ▾") { pickDate(selected) { change(it) } }
    }
    fun auth(register: Boolean) {
        val hero=row(); val copy=column()
        label(copy,if(register) "Tạo tài khoản" else "Vào sân thôi!",30,dark,true)
        label(copy,if(register) "Tạo tài khoản, kết nối đam mê.\nSẵn sàng cho trận đấu mới." else "Đặt sân dễ dàng, quản lý tiện lợi.\nĐam mê luôn sẵn sàng.",14,muted,top=12)
        hero.addView(copy,LinearLayout.LayoutParams(0,-2,1f))
        hero.addView(ImageView(this).apply { setImageResource(R.drawable.auth_illustration); scaleType=ImageView.ScaleType.FIT_CENTER },LinearLayout.LayoutParams(dp(106),dp(116)))
        add(content,hero,28,18)
        val roles=listOf("Người đặt sân","Chủ sân")
        chips(content,roles,if(ownerRole) roles[1] else roles[0]) { ownerRole=it==roles[1] }
        val name=if(register) input(content,"Họ và tên","Nhập họ và tên") else null
        val phone=input(content,"Số điện thoại","Nhập số điện thoại",type=InputType.TYPE_CLASS_PHONE)
        val password=input(content,"Mật khẩu",if(register) "Tạo mật khẩu" else "Nhập mật khẩu",type=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        if(register) label(content,"Tối thiểu 8 ký tự, gồm chữ và số.",11,muted,top=8)
        val repeat=if(register) input(content,"Xác nhận mật khẩu","Nhập lại mật khẩu",type=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) else null
        val accepted=check(content,if(register) "Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật của Sân Nhà." else "Ghi nhớ đăng nhập",!register && store.remember)
        if(!register) link(content,"Quên mật khẩu?") { confirm("Khôi phục mật khẩu","Liên hệ quản trị sân để xác minh tài khoản. Ứng dụng hiện chưa kết nối dịch vụ OTP.") { go("help") } }
        button(content,if(register) "Đăng ký  →" else "Đăng nhập  →",top=20) {
            val ph=phone.text?.toString().orEmpty().trim(); val pass=password.text?.toString().orEmpty()
            if(ph.isEmpty()) { phone.error="Nhập số điện thoại"; return@button }
            if(pass.isEmpty()) { password.error="Nhập mật khẩu"; return@button }
            val db=DatabaseHelper(this)
            if(register) {
                if(name!!.text?.toString().orEmpty().trim().isEmpty()) { name.error="Nhập họ và tên"; return@button }
                if(!ph.matches(Regex("0[0-9]{9}"))) { phone.error="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"; return@button }
                if(pass.length<8 || !pass.any { it.isLetter() } || !pass.any { it.isDigit() }) { password.error="Cần ít nhất 8 ký tự, gồm chữ và số"; return@button }
                if(pass!=repeat!!.text?.toString().orEmpty()) { repeat.error="Mật khẩu chưa khớp"; return@button }
                if(!accepted.isChecked) { toast("Vui lòng đồng ý điều khoản để đăng ký"); return@button }
                if(db.kiemTraTaiKhoanTonTai(ph)) { phone.error="Số điện thoại đã được đăng ký"; return@button }
                if(db.dangKy(NguoiDung(taiKhoan=ph,matKhau=pass,hoTen=name.text?.toString().orEmpty().trim(),soDienThoai=ph,vaiTro=if(ownerRole) "CHU_SAN" else "KHACH_HANG"))) {
                    toast("Đăng ký thành công. Mời bạn đăng nhập."); go("login",true)
                } else toast("Chưa tạo được tài khoản. Vui lòng thử lại.")
            } else {
                val user=db.dangNhap(ph,pass)
                if(user==null) { password.error="Tài khoản hoặc mật khẩu không chính xác"; return@button }
                if((user.vaiTro=="CHU_SAN")!=ownerRole) { toast("Vui lòng chọn đúng vai trò của tài khoản"); return@button }
                store.account=user.taiKhoan; store.owner=ownerRole; store.remember=accepted.isChecked
                store.setProfile("name",user.hoTen); store.setProfile("phone",user.soDienThoai)
                go(if(ownerRole) "owner_schedule" else "home",true)
            }
            db.close()
        }
        divider(content,24)
        link(content,if(register) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký ngay") { go(if(register) "login" else "register") }
        label(content,if(register) "♧ Thông tin an toàn. Đam mê trọn vẹn." else "♧ An tâm đăng nhập. Sẵn sàng ra sân.",12,muted,top=14).gravity=Gravity.CENTER
        if(!register) link(content,"Xem thử giao diện") {
            store.account=if(ownerRole) "demo-owner" else "demo-customer"; store.owner=ownerRole; store.remember=false
            store.seedDemo()
            store.setProfile("name",if(ownerRole) "Nguyễn Anh Tuấn" else "Nguyễn Minh")
            store.setProfile("phone",if(ownerRole) "0909123456" else "0901234567")
            go(if(ownerRole) "owner_schedule" else "home",true)
        }
    }
}
