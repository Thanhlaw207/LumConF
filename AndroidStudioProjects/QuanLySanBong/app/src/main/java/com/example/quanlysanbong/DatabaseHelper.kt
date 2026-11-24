package com.example.quanlysanbong

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QuanLySanBong.db"
        private const val DATABASE_VERSION = 3 // Tang version len 3 de cap nhat lai bang NguoiDung

        // Bang NguoiDung
        const val TABLE_NGUOI_DUNG = "NguoiDung"
        const val COLUMN_ID = "id"
        const val COLUMN_TAI_KHOAN = "taiKhoan"
        const val COLUMN_MAT_KHAU = "matKhau"
        const val COLUMN_HO_TEN = "hoTen"
        const val COLUMN_SO_DIEN_THOAI = "soDienThoai"
        const val COLUMN_VAI_TRO = "vaiTro" // "CHU_SAN" hoac "KHACH_HANG"
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
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NGUOI_DUNG")
        onCreate(db)
    }

    // 1. Ham Dang ky tai khoan
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

    // 2. Kiem tra tai khoan da ton tai chua
    fun kiemTraTaiKhoanTonTai(taiKhoan: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NGUOI_DUNG WHERE $COLUMN_TAI_KHOAN = ?", arrayOf(taiKhoan))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // 3. Ham Dang nhap (Lay ve day du thong tin gom ca vaiTro)
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
}