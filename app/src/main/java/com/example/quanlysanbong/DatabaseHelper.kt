package com.example.quanlysanbong

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QuanLySanBong.db"
        private const val DATABASE_VERSION = 4 // Tăng version lên 4 để thêm bảng SanBong

        // Bảng NguoiDung
        const val TABLE_NGUOI_DUNG = "NguoiDung"
        const val COLUMN_ID = "id"
        const val COLUMN_TAI_KHOAN = "taiKhoan"
        const val COLUMN_MAT_KHAU = "matKhau"
        const val COLUMN_HO_TEN = "hoTen"
        const val COLUMN_SO_DIEN_THOAI = "soDienThoai"
        const val COLUMN_VAI_TRO = "vaiTro" // "CHU_SAN" hoặc "KHACH_HANG"

        // Bảng SanBong
        const val TABLE_SAN_BONG = "SanBong"
        const val COLUMN_SAN_ID = "id"
        const val COLUMN_TEN_SAN = "tenSan"
        const val COLUMN_LOAI_SAN = "loaiSan" // "Sân 5", "Sân 7", v.v.
        const val COLUMN_GIA_THUE = "giaThue"
        const val COLUMN_TRANG_THAI = "trangThai" // "Trống", "Đang đá", "Bảo trì"
        const val COLUMN_CHU_SAN_ID = "chuSanId" // ID của chủ sân quản lý
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableNguoiDung = ("CREATE TABLE " + TABLE_NGUOI_DUNG + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TAI_KHOAN + " TEXT UNIQUE,"
                + COLUMN_MAT_KHAU + " TEXT,"
                + COLUMN_HO_TEN + " TEXT,"
                + COLUMN_SO_DIEN_THOAI + " TEXT,"
                + COLUMN_VAI_TRO + " TEXT" + ")")
        db?.execSQL(createTableNguoiDung)

        val createTableSanBong = ("CREATE TABLE " + TABLE_SAN_BONG + "("
                + COLUMN_SAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TEN_SAN + " TEXT,"
                + COLUMN_LOAI_SAN + " TEXT,"
                + COLUMN_GIA_THUE + " REAL,"
                + COLUMN_TRANG_THAI + " TEXT,"
                + COLUMN_CHU_SAN_ID + " INTEGER" + ")")
        db?.execSQL(createTableSanBong)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NGUOI_DUNG")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SAN_BONG")
        onCreate(db)
    }

    // ================= QUẢN LÝ NGƯỜI DÙNG =================

    fun dangKy(user: NguoiDung): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TAI_KHOAN, user.taiKhoan)
            put(COLUMN_MAT_KHAU, user.matKhau)
            put(COLUMN_HO_TEN, user.hoTen)
            put(COLUMN_SO_DIEN_THOAI, user.soDienThoai)
            put(COLUMN_VAI_TRO, user.vaiTro)
        }
        val result = db.insert(TABLE_NGUOI_DUNG, null, values)
        db.close()
        return result != -1L
    }

    fun kiemTraTaiKhoanTonTai(taiKhoan: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NGUOI_DUNG WHERE $COLUMN_TAI_KHOAN = ?", arrayOf(taiKhoan))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun dangNhap(taiKhoan: String, matKhau: String): NguoiDung? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NGUOI_DUNG WHERE $COLUMN_TAI_KHOAN = ? AND $COLUMN_MAT_KHAU = ?"
        val cursor = db.rawQuery(query, arrayOf(taiKhoan, matKhau))

        var user: NguoiDung? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val tk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAI_KHOAN))
            val mk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAT_KHAU))
            val hoTen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HO_TEN))
            val soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SO_DIEN_THOAI))
            val vaiTro = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VAI_TRO))

            user = NguoiDung(id, tk, mk, hoTen, soDienThoai, vaiTro)
        }
        cursor.close()
        db.close()
        return user
    }

    // ================= QUẢN LÝ SÂN BÓNG (DÀNH CHO CHỦ SÂN) =================

    // 1. Thêm sân bóng mới
    fun themSanBong(tenSan: String, loaiSan: String, giaThue: Double, trangThai: String, chuSanId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TEN_SAN, tenSan)
            put(COLUMN_LOAI_SAN, loaiSan)
            put(COLUMN_GIA_THUE, giaThue)
            put(COLUMN_TRANG_THAI, trangThai)
            put(COLUMN_CHU_SAN_ID, chuSanId)
        }
        val result = db.insert(TABLE_SAN_BONG, null, values)
        db.close()
        return result != -1L
    }

    // 2. Lấy danh sách sân bóng thuộc quyền quản lý của một Chủ sân cụ thể
    fun layDanhSachSanTheoChuSan(chuSanId: Int): List<SanBong> {
        val danhSachSan = mutableListOf<SanBong>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAN_BONG WHERE $COLUMN_CHU_SAN_ID = ?", arrayOf(chuSanId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SAN_ID))
                val tenSan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEN_SAN))
                val loaiSan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOAI_SAN))
                val giaThue = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GIA_THUE))
                val trangThai = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANG_THAI))
                val idChuSan = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHU_SAN_ID))

                val sanBong = SanBong(id, tenSan, loaiSan, giaThue, trangThai, idChuSan)
                danhSachSan.add(sanBong)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return danhSachSan
    }

    // 3. Xóa sân bóng theo ID
    fun xoaSanBong(sanId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_SAN_BONG, "$COLUMN_SAN_ID = ?", arrayOf(sanId.toString()))
        db.close()
        return result > 0
    }

    // 4. Lấy toàn bộ danh sách tất cả các sân bóng cho Khách hàng xem
    fun layTatCaSanBong(): List<SanBong> {
        val danhSachSan = mutableListOf<SanBong>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAN_BONG", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SAN_ID))
                val tenSan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEN_SAN))
                val loaiSan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOAI_SAN))
                val giaThue = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GIA_THUE))
                val trangThai = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANG_THAI))
                val idChuSan = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHU_SAN_ID))

                val sanBong = SanBong(id, tenSan, loaiSan, giaThue, trangThai, idChuSan)
                danhSachSan.add(sanBong)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return danhSachSan
    }

    // Lọc danh sách sân bóng theo loại sân (VD: "Sân 5 người", "Sân 7 người", ...)
    fun layDanhSachSanTheoLoai(loaiSan: String): List<SanBong> {
        val danhSachSan = mutableListOf<SanBong>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAN_BONG WHERE $COLUMN_LOAI_SAN = ?", arrayOf(loaiSan))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SAN_ID))
                val tenSan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEN_SAN))
                val loai = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOAI_SAN))
                val giaThue = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GIA_THUE))
                val trangThai = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANG_THAI))
                val idChuSan = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHU_SAN_ID))

                val sanBong = SanBong(id, tenSan, loai, giaThue, trangThai, idChuSan)
                danhSachSan.add(sanBong)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return danhSachSan
    }
}