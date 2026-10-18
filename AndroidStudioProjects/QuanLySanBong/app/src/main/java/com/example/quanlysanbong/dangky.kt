package com.example.quanlysanbong

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class dangky : AppCompatActivity() { // Đổi tên class thành dangky cho khớp với tên file dangky.kt

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dangky)

        dbHelper = DatabaseHelper(this)

        val etHoTen = findViewById<EditText>(R.id.etHoTen)
        val etTaiKhoan = findViewById<EditText>(R.id.etTaiKhoan)
        val etMatKhau = findViewById<EditText>(R.id.etMatKhau)
        val etSoDienThoai = findViewById<EditText>(R.id.etSoDienThoai)
        val btnDangKy = findViewById<Button>(R.id.btnDangKy)

        btnDangKy.setOnClickListener {
            val hoTen = etHoTen.text.toString().trim()
            val taiKhoan = etTaiKhoan.text.toString().trim()
            val matKhau = etMatKhau.text.toString().trim()
            val soDienThoai = etSoDienThoai.text.toString().trim()

            if (hoTen.isEmpty() || taiKhoan.isEmpty() || matKhau.isEmpty() || soDienThoai.isEmpty()) {
                Toast.makeText(this, "Vui long nhap day du thong tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.kiemTraTaiKhoanTonTai(taiKhoan)) {
                Toast.makeText(this, "Tai khoan da ton tai!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = NguoiDung(
                taiKhoan = taiKhoan,
                matKhau = matKhau,
                hoTen = hoTen,
                soDienThoai = soDienThoai,
                vaiTro = "KHACH_HANG"
            )

            if (dbHelper.dangKy(newUser)) {
                Toast.makeText(this, "Dang ky thanh cong!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Dang ky that bai, vui long thu lai!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}