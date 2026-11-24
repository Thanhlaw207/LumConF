package com.example.quanlysanbong

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class dangnhap : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dangnhap)

        dbHelper = DatabaseHelper(this)

        val etTaiKhoan = findViewById<EditText>(R.id.etTaiKhoan)
        val etMatKhau = findViewById<EditText>(R.id.etMatKhau)
        val btnDangNhap = findViewById<Button>(R.id.btnDangNhap)
        val tvChuyenSangDangKy = findViewById<TextView>(R.id.tvChuyenSangDangKy)

        // Xu ly nut Dang nhap
        btnDangNhap.setOnClickListener {
            val taiKhoan = etTaiKhoan.text.toString().trim()
            val matKhau = etMatKhau.text.toString().trim()

            if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
                Toast.makeText(this, "Vui long nhap tai khoan va mat khau!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = dbHelper.dangNhap(taiKhoan, matKhau)
            if (user != null) {
                Toast.makeText(this, "Dang nhap thanh cong! Xin chao ${user.hoTen}", Toast.LENGTH_SHORT).show()

                // PHÂN QUYỀN ĐIỀU HƯỚNG MÀN HÌNH
                val intent = if (user.vaiTro == "CHU_SAN") {
                    // Nếu là chủ sân -> Mở giao diện Quản lý của Chủ sân
                    Intent(this, activity_main_chu_san::class.java)
                } else {
                    // Mặc định hoặc KHACH_HANG -> Mở giao diện Khách hàng
                    Intent(this, activity_main_khach_hang::class.java)
                }

                startActivity(intent)
                finish() // Dong man hinh dang nhap
            } else {
                Toast.makeText(this, "Tai khoan hoac mat khau khong chinh xac!", Toast.LENGTH_SHORT).show()
            }
        }

        // Chuyen sang man hinh Dang ky khi bam vao Text
        tvChuyenSangDangKy.setOnClickListener {
            val intent = Intent(this, dangky::class.java)
            startActivity(intent)
        }
    }
}