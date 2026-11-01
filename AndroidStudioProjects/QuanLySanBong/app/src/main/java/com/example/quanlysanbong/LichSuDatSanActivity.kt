
package com.example.quanlysanbong

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Model lưu thông tin lịch sử đặt sân
data class LichSuDatSan(
    val id: Int,
    val tenSan: String,
    val loaiSan: String,
    val giaThue: Double,
    val ngayDa: String,
    val gioBatDau: Int
)

class LichSuDatSanActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var danhSachLayout: LinearLayout

    private var khachHangId = -1

    private val mauXanh = Color.rgb(22, 163, 74)
    private val mauNen = Color.rgb(245, 247, 250)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DatabaseHelper(this)

        // Nhận ID khách hàng từ màn hình trang chủ
        khachHangId = intent.getIntExtra(
            "KHACH_HANG_ID",
            -1
        )

        // Giao diện chính
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(mauNen)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        // Nút quay lại
        val btnQuayLai = Button(this).apply {
            text = "← Quay lại"
            setOnClickListener {
                finish()
            }
        }

        root.addView(btnQuayLai)

        // Tiêu đề
        val tieuDe = TextView(this).apply {
            text = "LỊCH SỬ ĐẶT SÂN"
            textSize = 23f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(18), 0, dp(18))
        }

        root.addView(tieuDe)

        // Danh sách cuộn
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        danhSachLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scrollView.addView(danhSachLayout)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        hienThiLichSu()
    }

    // Lấy dữ liệu từ SQLite và hiển thị
    private fun hienThiLichSu() {

        danhSachLayout.removeAllViews()

        if (khachHangId <= 0) {
            hienThiThongBao(
                "Không tìm thấy tài khoản khách hàng."
            )
            return
        }

        val danhSach = dbHelper.layLichSuDatSan(
            khachHangId
        )

        if (danhSach.isEmpty()) {
            hienThiThongBao(
                "Bạn chưa có lịch đặt sân nào."
            )
            return
        }

        for (lich in danhSach) {
            taoTheLichSu(lich)
        }
    }

    // Tạo thẻ thông tin cho mỗi lịch đặt
    private fun taoTheLichSu(lich: LichSuDatSan) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(14).toFloat()
            }

            elevation = dp(3).toFloat()
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(14)
            leftMargin = dp(3)
            rightMargin = dp(3)
        }

        // Tên sân
        card.addView(
            taoText(
                lich.tenSan,
                20f,
                Color.BLACK,
                true
            )
        )

        // Loại sân
        card.addView(
            taoText(
                "Loại sân: ${lich.loaiSan}",
                15f
            )
        )

        // Ngày đá
        val ngayHienThi = try {

            val input = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            val output = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale("vi", "VN")
            )

            output.format(input.parse(lich.ngayDa)!!)

        } catch (e: Exception) {
            lich.ngayDa
        }

        card.addView(
            taoText(
                "Ngày đá: $ngayHienThi",
                15f
            )
        )

        // Khung giờ đá
        val gioBatDau = String.format(
            Locale.US,
            "%02d:00",
            lich.gioBatDau
        )

        val gioKetThuc = String.format(
            Locale.US,
            "%02d:00",
            lich.gioBatDau + 1
        )

        card.addView(
            taoText(
                "Giờ đá: $gioBatDau - $gioKetThuc",
                15f
            )
        )

        // Giá thuê
        val giaThue = String.format(
            Locale("vi", "VN"),
            "%,.0f VNĐ",
            lich.giaThue
        )

        card.addView(
            taoText(
                "Giá thuê: $giaThue",
                15f,
                mauXanh,
                true
            )
        )

        // Trạng thái lịch
        val trangThai = tinhTrangThai(lich)

        val mauTrangThai = when (trangThai) {
            "Sắp diễn ra" -> mauXanh
            "Đang diễn ra" -> Color.rgb(234, 126, 19)
            else -> Color.GRAY
        }

        card.addView(
            taoText(
                "Trạng thái: $trangThai",
                15f,
                mauTrangThai,
                true
            )
        )

        danhSachLayout.addView(card, params)
    }

    // Kiểm tra thời gian lịch đặt
    private fun tinhTrangThai(
        lich: LichSuDatSan
    ): String {

        return try {

            val dinhDang = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.US
            ).apply {
                isLenient = false
            }

            val gioBatDau = String.format(
                Locale.US,
                "%02d:00",
                lich.gioBatDau
            )

            val gioKetThuc = String.format(
                Locale.US,
                "%02d:00",
                lich.gioBatDau + 1
            )

            val batDau = dinhDang.parse(
                "${lich.ngayDa} $gioBatDau"
            ) ?: return "Không xác định"

            val ketThuc = dinhDang.parse(
                "${lich.ngayDa} $gioKetThuc"
            ) ?: return "Không xác định"

            val hienTai = Calendar.getInstance().timeInMillis

            when {
                hienTai < batDau.time -> "Sắp diễn ra"
                hienTai < ketThuc.time -> "Đang diễn ra"
                else -> "Đã qua"
            }

        } catch (e: Exception) {
            "Không xác định"
        }
    }

    // Hiển thị khi chưa có lịch đặt
    private fun hienThiThongBao(noiDung: String) {

        val tv = taoText(
            noiDung,
            16f,
            Color.GRAY
        )

        tv.gravity = Gravity.CENTER

        tv.setPadding(
            dp(12),
            dp(50),
            dp(12),
            dp(20)
        )

        danhSachLayout.addView(tv)
    }

    // Hàm tạo TextView
    private fun taoText(
        noiDung: String,
        kichThuoc: Float,
        mau: Int = Color.DKGRAY,
        inDam: Boolean = false
    ): TextView {

        return TextView(this).apply {
            text = noiDung
            textSize = kichThuoc
            setTextColor(mau)
            setPadding(0, dp(5), 0, dp(5))

            if (inDam) {
                setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }
}
