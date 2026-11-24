package com.example.quanlysanbong

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class activity_main_chu_san : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sanBongAdapter: SanBongAdapter
    private var danhSachSan: MutableList<SanBong> = mutableListOf()
    private var chuSanId: Int = -1 // Lưu ID của chủ sân đang đăng nhập

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_chu_san)

        val mainView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        dbHelper = DatabaseHelper(this)

        // Lấy ID chủ sân (Ở đây tạm gán hoặc lấy từ lúc đăng nhập,
        // bạn có thể truyền qua Intent nếu cần. Tạm thời minh họa lấy ID = 1 hoặc truyền từ Intent)
        chuSanId = intent.getIntExtra("USER_ID", 1)

        // Ánh xạ các thành phần giao diện
        val tvXinChao = findViewById<TextView>(R.id.tvXinChaoChuSan)
        val tvTongSoSan = findViewById<TextView>(R.id.tvTongSoSan)
        val tvDonHomNay = findViewById<TextView>(R.id.tvDonHomNay)
        val btnDangXuat = findViewById<ImageButton>(R.id.btnDangXuatChuSan)
        val fabThemSan = findViewById<FloatingActionButton>(R.id.fabThemSan)
        val rvDanhSachSan = findViewById<RecyclerView>(R.id.rvDanhSachSanChuSan)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationChuSan)

        // Cài đặt RecyclerView
        rvDanhSachSan.layoutManager = LinearLayoutManager(this)
        sanBongAdapter = SanBongAdapter(danhSachSan) { sanBong ->
            // Sự kiện khi bấm nút Xóa sân
            xoaSanBongDialog(sanBong)
        }
        rvDanhSachSan.adapter = sanBongAdapter

        // Load dữ liệu sân bóng từ SQLite lên
        loadDanhSachSan()

        // 1. Nút Đăng xuất
        btnDangXuat.setOnClickListener {
            Toast.makeText(this, "Đã đăng xuất tài khoản Chủ sân", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, dangnhap::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 2. Nút Thêm Sân Bóng Mới (Mở Dialog nhập thông tin)
        fabThemSan.setOnClickListener {
            hienDialogThemSan()
        }

        // 3. Thanh Bottom Navigation
        bottomNav.selectedItemId = R.id.nav_home_chusan
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home_chusan -> true
                R.id.nav_bookings -> {
                    Toast.makeText(this, "Quản lý Lịch đặt sân", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_revenue -> {
                    Toast.makeText(this, "Báo cáo Doanh thu", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    // Hàm load dữ liệu từ Database vào RecyclerView
    private fun loadDanhSachSan() {
        val list = dbHelper.layDanhSachSanTheoChuSan(chuSanId)
        danhSachSan.clear()
        danhSachSan.addAll(list)
        sanBongAdapter.notifyDataSetChanged()

        // Cập nhật tổng số sân lên thẻ thống kê
        val tvTongSoSan = findViewById<TextView>(R.id.tvTongSoSan)
        tvTongSoSan.text = danhSachSan.size.toString()
    }

    // Hiển thị Dialog thêm sân bóng
    private fun hienDialogThemSan() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_them_san, null)
        val etTenSan = dialogView.findViewById<EditText>(R.id.etTenSan)
        val spinnerLoaiSan = dialogView.findViewById<Spinner>(R.id.spinnerLoaiSan)
        val etGiaThue = dialogView.findViewById<EditText>(R.id.etGiaThue)

        // Cài đặt dữ liệu cho Spinner loại sân (Sân 5, Sân 7, Sân 11)
        val loaiSanList = arrayOf("Sân 5 người", "Sân 7 người", "Sân 11 người")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, loaiSanList)
        spinnerLoaiSan.adapter = adapterSpinner

        AlertDialog.Builder(this)
            .setTitle("Thêm Sân Bóng Mới ⚽")
            .setView(dialogView)
            .setPositiveButton("Thêm") { _, _ ->
                val tenSan = etTenSan.text.toString().trim()
                val loaiSan = spinnerLoaiSan.selectedItem.toString()
                val giaThueStr = etGiaThue.text.toString().trim()

                if (tenSan.isEmpty() || giaThueStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ tên và giá sân!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val giaThue = giaThueStr.toDoubleOrNull() ?: 0.0
                val trangThai = "Trống"

                val success = dbHelper.themSanBong(tenSan, loaiSan, giaThue, trangThai, chuSanId)
                if (success) {
                    Toast.makeText(this, "Thêm sân bóng thành công!", Toast.LENGTH_SHORT).show()
                    loadDanhSachSan() // Tải lại danh sách
                } else {
                    Toast.makeText(this, "Thêm sân thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Hàm xác nhận xóa sân
    private fun xoaSanBongDialog(sanBong: SanBong) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa sân '${sanBong.tenSan}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                val success = dbHelper.xoaSanBong(sanBong.id)
                if (success) {
                    Toast.makeText(this, "Đã xóa sân thành công!", Toast.LENGTH_SHORT).show()
                    loadDanhSachSan()
                } else {
                    Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}