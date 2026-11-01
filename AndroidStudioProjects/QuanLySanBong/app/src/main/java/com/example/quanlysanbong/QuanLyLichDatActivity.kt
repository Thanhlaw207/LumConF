
package com.example.quanlysanbong

import android.app.AlertDialog
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LichDatChuSan(
    val id: Int,
    val tenSan: String,
    val tenKhachHang: String,
    val soDienThoai: String,
    val ngayDa: String,
    val gioBatDau: Int,
    val giaThue: Double
)

class QuanLyLichDatActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var danhSachLayout: LinearLayout
    private var chuSanId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DatabaseHelper(this)
        chuSanId = intent.getIntExtra("USER_ID", -1)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.rgb(246, 248, 250))
        }

        val btnQuayLai = Button(this).apply {
            text = "← Quay lại"
            setOnClickListener { finish() }
        }
        root.addView(btnQuayLai)

        val tieuDe = TextView(this).apply {
            text = "QUẢN LÝ LỊCH ĐẶT SÂN"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(16), 0, dp(20))
        }
        root.addView(tieuDe)

        val scroll = ScrollView(this)

        danhSachLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(danhSachLayout)

        root.addView(
            scroll,
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
        taiDanhSachLichDat()
    }

    private fun taiDanhSachLichDat() {
        danhSachLayout.removeAllViews()

        if (chuSanId <= 0) {
            hienThongBao("Không tìm thấy tài khoản chủ sân.")
            return
        }

        val danhSach = dbHelper.layLichDatTheoChuSan(chuSanId)

        if (danhSach.isEmpty()) {
            hienThongBao("Chưa có khách hàng đặt sân.")
            return
        }

        for (lich in danhSach) {
            hienThiLichDat(lich)
        }
    }

    private fun hienThiLichDat(lich: LichDatChuSan) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            elevation = dp(3).toFloat()
        }

        fun dong(text: String, dam: Boolean = false): TextView {
            return TextView(this).apply {
                this.text = text
                textSize = if (dam) 18f else 15f
                setTextColor(Color.DKGRAY)
                setPadding(0, dp(5), 0, dp(5))
                if (dam) setTypeface(null, Typeface.BOLD)
            }
        }

        card.addView(dong(lich.tenSan, true))
        card.addView(dong("Khách hàng: ${lich.tenKhachHang}"))
        card.addView(dong("Số điện thoại: ${lich.soDienThoai}"))
        card.addView(dong("Ngày đá: ${lich.ngayDa}"))

        val batDau = String.format(
            Locale.US, "%02d:00", lich.gioBatDau
        )
        val ketThuc = String.format(
            Locale.US, "%02d:00", lich.gioBatDau + 1
        )

        card.addView(dong("Khung giờ: $batDau - $ketThuc"))

        val gia = String.format(
            Locale("vi", "VN"),
            "%,.0f VNĐ",
            lich.giaThue
        )
        card.addView(dong("Giá thuê: $gia"))

        val trangThai = tinhTrangThai(lich)
        val tvTrangThai = dong("Trạng thái: $trangThai", true)

        tvTrangThai.setTextColor(
            when (trangThai) {
                "Sắp diễn ra" -> Color.rgb(22, 163, 74)
                "Đang diễn ra" -> Color.rgb(230, 130, 20)
                else -> Color.GRAY
            }
        )
        card.addView(tvTrangThai)

        if (trangThai == "Sắp diễn ra") {
            val btnHuy = Button(this).apply {
                text = "Hủy lịch đặt"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(211, 47, 47))
                setOnClickListener {
                    xacNhanHuyLich(lich)
                }
            }

            card.addView(btnHuy)
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(14)
        }

        danhSachLayout.addView(card, params)
    }

    private fun tinhTrangThai(lich: LichDatChuSan): String {
        return try {
            val format = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.US
            ).apply { isLenient = false }

            val batDau = format.parse(
                "${lich.ngayDa} %02d:00".format(
                    Locale.US, lich.gioBatDau
                )
            ) ?: return "Không xác định"

            val ketThuc = format.parse(
                "${lich.ngayDa} %02d:00".format(
                    Locale.US, lich.gioBatDau + 1
                )
            ) ?: return "Không xác định"

            val now = Date().time

            when {
                now < batDau.time -> "Sắp diễn ra"
                now < ketThuc.time -> "Đang diễn ra"
                else -> "Đã qua"
            }
        } catch (e: Exception) {
            "Không xác định"
        }
    }

    private fun xacNhanHuyLich(lich: LichDatChuSan) {

        AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy lịch")
            .setMessage(
                "Bạn có chắc muốn hủy lịch đặt này?\n\n" +
                        "Sân: ${lich.tenSan}\n" +
                        "Khách: ${lich.tenKhachHang}\n" +
                        "Ngày: ${lich.ngayDa}\n" +
                        "Giờ: ${lich.gioBatDau}:00"
            )
            .setNegativeButton("Không", null)
            .setPositiveButton("Hủy lịch") { _, _ ->

                val thanhCong = dbHelper.huyLichDatChuSan(
                    lich.id,
                    chuSanId
                )

                if (thanhCong) {
                    Toast.makeText(
                        this,
                        "Đã hủy lịch đặt thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    taiDanhSachLichDat()
                } else {
                    Toast.makeText(
                        this,
                        "Không thể hủy lịch đặt",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun hienThongBao(noiDung: String) {
        val tv = TextView(this).apply {
            text = noiDung
            textSize = 16f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(40), 0, 0)
        }
        danhSachLayout.addView(tv)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
