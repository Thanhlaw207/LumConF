package com.example.quanlysanbong

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip

class activity_main_khach_hang : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SanBongKhachHangAdapter
    private var danhSachSan: MutableList<SanBong> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_khach_hang)

        // Xử lý padding tránh bị che khuất bởi status bar / navigation bar
        val mainView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        dbHelper = DatabaseHelper(this)

        // 1. Khởi tạo RecyclerView hiển thị danh sách sân bóng
        val rvSanBong = findViewById<RecyclerView>(R.id.rvDanhSachSan)
        rvSanBong.layoutManager = LinearLayoutManager(this)

        adapter = SanBongKhachHangAdapter(danhSachSan) { sanBong ->
            Toast.makeText(this, "Bạn chọn đặt sân: ${sanBong.tenSan}", Toast.LENGTH_SHORT).show()
            // Sau này bạn có thể mở màn hình chi tiết hoặc xử lý đặt sân tại đây
        }
        rvSanBong.adapter = adapter

        // Tải toàn bộ dữ liệu sân bóng lên mặc định ban đầu
        loadSanBong("Tất cả")

        // 2. Xử lý các nút lọc (Chips) theo loại sân
        // (Lưu ý: Bạn nhớ kiểm tra các ID này trong file activity_main_khach_hang.xml đã khớp chưa nhé)
        val chipTatCa = findViewById<Chip>(R.id.chipTatCa)
        val chipSan5 = findViewById<Chip>(R.id.chipSan5)
        val chipSan7 = findViewById<Chip>(R.id.chipSan7)
        val chipSan11 = findViewById<Chip>(R.id.chipSan11)

        chipTatCa?.setOnClickListener { loadSanBong("Tất cả") }
        chipSan5?.setOnClickListener { loadSanBong("Sân 5 người") }
        chipSan7?.setOnClickListener { loadSanBong("Sân 7 người") }
        chipSan11?.setOnClickListener { loadSanBong("Sân 11 người") }

        // 3. Xử lý nút Đăng xuất
        val btnDangXuat = findViewById<ImageButton>(R.id.btnDangXuat)
        btnDangXuat.setOnClickListener {
            Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, dangnhap::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 4. Xử lý thanh điều hướng BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> {
                    Toast.makeText(this, "Chuyển sang Lịch sử đặt sân", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Chuyển sang trang Cá nhân", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSanBong("Tất cả") // Tải lại mặc định mỗi khi mở lại màn hình
    }

    // Hàm load dữ liệu có hỗ trợ lọc theo loại sân
    private fun loadSanBong(loaiSan: String) {
        val list = if (loaiSan == "Tất cả") {
            dbHelper.layTatCaSanBong()
        } else {
            dbHelper.layDanhSachSanTheoLoai(loaiSan)
        }

        danhSachSan.clear()
        danhSachSan.addAll(list)
        adapter.notifyDataSetChanged()
    }
}