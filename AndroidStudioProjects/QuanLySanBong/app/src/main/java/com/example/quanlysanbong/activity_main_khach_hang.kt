package com.example.quanlysanbong

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class activity_main_khach_hang : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_khach_hang)

        // Xử lý padding tránh bị che khuất bởi status bar / navigation bar
        // Lưu ý: Nếu layout của bạn không có id là 'main', bạn có thể trỏ tới layout gốc hoặc bỏ qua đoạn này nếu dùng ScrollView
        val mainView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Giữ đáy bằng 0 để bottom bar đẹp hơn
            insets
        }

        // 1. Xử lý nút Đăng xuất
        val btnDangXuat = findViewById<ImageButton>(R.id.btnDangXuat)
        btnDangXuat.setOnClickListener {
            Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, dangnhap::class.java)
            // Xóa sạch lịch sử activity trước đó để người dùng không bấm nút Back quay lại được
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 2. Xử lý thanh điều hướng BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.nav_home // Mặc định chọn tab Trang chủ

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Đang ở trang chủ, giữ nguyên
                    true
                }
                R.id.nav_history -> {
                    Toast.makeText(this, "Chuyển sang Lịch sử đặt sân", Toast.LENGTH_SHORT).show()
                    // Sau này bạn có thể start Activity Lịch sử ở đây
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Chuyển sang trang Cá nhân", Toast.LENGTH_SHORT).show()
                    // Sau này bạn có thể start Activity Cá nhân ở đây
                    true
                }
                else -> false
            }
        }
    }
}