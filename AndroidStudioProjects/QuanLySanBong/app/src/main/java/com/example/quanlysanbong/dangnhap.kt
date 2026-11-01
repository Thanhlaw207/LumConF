
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
        val tvDangKy =
            findViewById<TextView>(R.id.tvChuyenSangDangKy)

        btnDangNhap.setOnClickListener {

            val taiKhoan = etTaiKhoan.text.toString().trim()
            val matKhau = etMatKhau.text.toString().trim()

            if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
                Toast.makeText(
                    this,
                    "Vui lòng nhập tài khoản và mật khẩu",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val user = dbHelper.dangNhap(taiKhoan, matKhau)

<<<<<<< HEAD
                // Chuyen sang MainActivity (Man hinh chinh)
                val intent = Intent(this, activity_main_khach_hang::class.java)
=======
            if (user != null) {

                val intent = when (user.vaiTro) {
                    "CHU_SAN" -> Intent(
                        this,
                        activity_main_chu_san::class.java
                    )

                    "KHACH_HANG" -> Intent(
                        this,
                        activity_main_khach_hang::class.java
                    )

                    else -> {
                        Toast.makeText(
                            this,
                            "Vai trò tài khoản không hợp lệ",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }

                // Chủ sân và khách hàng dùng đúng ID tài khoản
                intent.putExtra("USER_ID", user.id)
                intent.putExtra("KHACH_HANG_ID", user.id)

                Toast.makeText(
                    this,
                    "Đăng nhập thành công",
                    Toast.LENGTH_SHORT
                ).show()

>>>>>>> 2c239c1 (Cap nhat tinh nang quan ly dat san va lich su)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(
                    this,
                    "Tài khoản hoặc mật khẩu không chính xác",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tvDangKy.setOnClickListener {
            startActivity(
                Intent(this, dangky::class.java)
            )
        }
    }
}
