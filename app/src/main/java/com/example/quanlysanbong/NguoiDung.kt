package com.example.quanlysanbong

data class NguoiDung(
    val id: Int = 0,
    val taiKhoan: String,        // Ten dang nhap
    val matKhau: String,         // Mat khau
    val hoTen: String,           // Ho va ten
    val soDienThoai: String,     // So dien thoai
    val vaiTro: String           // "CHU_SAN" hoac "KHACH_HANG"
)