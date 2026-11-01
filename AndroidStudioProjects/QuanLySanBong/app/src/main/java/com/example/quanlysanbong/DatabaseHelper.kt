
package com.example.quanlysanbong

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QuanLySanBong.db"
<<<<<<< HEAD
        private const val DATABASE_VERSION = 3 // Tang version len 3 de cap nhat lai bang NguoiDung

        // Bang NguoiDung
=======
        private const val DATABASE_VERSION = 5

        // Người dùng
>>>>>>> 2c239c1 (Cap nhat tinh nang quan ly dat san va lich su)
        const val TABLE_NGUOI_DUNG = "NguoiDung"
        const val COLUMN_ID = "id"
        const val COLUMN_TAI_KHOAN = "taiKhoan"
        const val COLUMN_MAT_KHAU = "matKhau"
        const val COLUMN_HO_TEN = "hoTen"
        const val COLUMN_SO_DIEN_THOAI = "soDienThoai"
<<<<<<< HEAD
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
=======
        const val COLUMN_VAI_TRO = "vaiTro"

        // Sân bóng
        const val TABLE_SAN_BONG = "SanBong"
        const val COLUMN_SAN_ID = "id"
        const val COLUMN_TEN_SAN = "tenSan"
        const val COLUMN_LOAI_SAN = "loaiSan"
        const val COLUMN_GIA_THUE = "giaThue"
        const val COLUMN_TRANG_THAI = "trangThai"
        const val COLUMN_CHU_SAN_ID = "chuSanId"

        // Đặt sân
        const val TABLE_DAT_SAN = "DatSan"
        const val COLUMN_DAT_SAN_ID = "id"
        const val COLUMN_DAT_SAN_SAN_ID = "sanId"
        const val COLUMN_KHACH_HANG_ID = "khachHangId"
        const val COLUMN_NGAY_DA = "ngayDa"
        const val COLUMN_GIO_BAT_DAU = "gioBatDau"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL("""
            CREATE TABLE $TABLE_NGUOI_DUNG (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                taiKhoan TEXT UNIQUE,
                matKhau TEXT,
                hoTen TEXT,
                soDienThoai TEXT,
                vaiTro TEXT
            )
        """.trimIndent())

        db?.execSQL("""
            CREATE TABLE $TABLE_SAN_BONG (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tenSan TEXT,
                loaiSan TEXT,
                giaThue REAL,
                trangThai TEXT,
                chuSanId INTEGER
            )
        """.trimIndent())

        taoBangDatSan(db)
    }

    private fun taoBangDatSan(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS DatSan (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sanId INTEGER NOT NULL,
                khachHangId INTEGER NOT NULL,
                ngayDa TEXT NOT NULL,
                gioBatDau INTEGER NOT NULL,
                UNIQUE(sanId, ngayDa, gioBatDau)
            )
        """.trimIndent())
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 5) {
            taoBangDatSan(db)
        }
    }

    // ================= NGƯỜI DÙNG =================

>>>>>>> 2c239c1 (Cap nhat tinh nang quan ly dat san va lich su)
    fun dangKy(user: NguoiDung): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_TAI_KHOAN, user.taiKhoan)
            put(COLUMN_MAT_KHAU, user.matKhau)
            put(COLUMN_HO_TEN, user.hoTen)
            put(COLUMN_SO_DIEN_THOAI, user.soDienThoai)
            put(COLUMN_VAI_TRO, user.vaiTro)
        }

        return writableDatabase.insert(
            TABLE_NGUOI_DUNG, null, values
        ) != -1L
    }

    // 2. Kiem tra tai khoan da ton tai chua
    fun kiemTraTaiKhoanTonTai(taiKhoan: String): Boolean {
<<<<<<< HEAD
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
=======
        readableDatabase.rawQuery(
            "SELECT id FROM NguoiDung WHERE taiKhoan = ?",
            arrayOf(taiKhoan)
        ).use { cursor ->
            return cursor.moveToFirst()
>>>>>>> 2c239c1 (Cap nhat tinh nang quan ly dat san va lich su)
        }
    }
<<<<<<< HEAD
}
=======

    fun dangNhap(
        taiKhoan: String,
        matKhau: String
    ): NguoiDung? {

        readableDatabase.rawQuery(
            """
                SELECT * FROM NguoiDung
                WHERE taiKhoan = ? AND matKhau = ?
            """.trimIndent(),
            arrayOf(taiKhoan, matKhau)
        ).use { cursor ->

            if (!cursor.moveToFirst()) return null

            return NguoiDung(
                cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
                ),
                cursor.getString(
                    cursor.getColumnIndexOrThrow("taiKhoan")
                ),
                cursor.getString(
                    cursor.getColumnIndexOrThrow("matKhau")
                ),
                cursor.getString(
                    cursor.getColumnIndexOrThrow("hoTen")
                ),
                cursor.getString(
                    cursor.getColumnIndexOrThrow("soDienThoai")
                ),
                cursor.getString(
                    cursor.getColumnIndexOrThrow("vaiTro")
                )
            )
        }
    }

    // ================= QUẢN LÝ SÂN =================

    fun themSanBong(
        tenSan: String,
        loaiSan: String,
        giaThue: Double,
        trangThai: String,
        chuSanId: Int
    ): Boolean {

        val values = ContentValues().apply {
            put("tenSan", tenSan)
            put("loaiSan", loaiSan)
            put("giaThue", giaThue)
            put("trangThai", trangThai)
            put("chuSanId", chuSanId)
        }

        return writableDatabase.insert(
            TABLE_SAN_BONG, null, values
        ) != -1L
    }

    private fun docSanBong(
        cursor: android.database.Cursor
    ): SanBong {
        return SanBong(
            cursor.getInt(
                cursor.getColumnIndexOrThrow("id")
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow("tenSan")
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow("loaiSan")
            ),
            cursor.getDouble(
                cursor.getColumnIndexOrThrow("giaThue")
            ),
            cursor.getString(
                cursor.getColumnIndexOrThrow("trangThai")
            ),
            cursor.getInt(
                cursor.getColumnIndexOrThrow("chuSanId")
            )
        )
    }

    fun layDanhSachSanTheoChuSan(
        chuSanId: Int
    ): List<SanBong> {

        val result = mutableListOf<SanBong>()

        readableDatabase.rawQuery(
            "SELECT * FROM SanBong WHERE chuSanId = ?",
            arrayOf(chuSanId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(docSanBong(cursor))
            }
        }
        return result
    }

    fun layTatCaSanBong(): List<SanBong> {
        val result = mutableListOf<SanBong>()

        readableDatabase.rawQuery(
            "SELECT * FROM SanBong", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(docSanBong(cursor))
            }
        }
        return result
    }

    fun layDanhSachSanTheoLoai(
        loaiSan: String
    ): List<SanBong> {

        val result = mutableListOf<SanBong>()

        readableDatabase.rawQuery(
            "SELECT * FROM SanBong WHERE loaiSan = ?",
            arrayOf(loaiSan)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(docSanBong(cursor))
            }
        }
        return result
    }

    fun xoaSanBong(sanId: Int): Boolean {
        val db = writableDatabase
        var thanhCong = false

        db.beginTransaction()
        try {
            db.delete(
                TABLE_DAT_SAN,
                "sanId = ?",
                arrayOf(sanId.toString())
            )

            thanhCong = db.delete(
                TABLE_SAN_BONG,
                "id = ?",
                arrayOf(sanId.toString())
            ) > 0

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return thanhCong
    }

    // ================= ĐẶT SÂN =================

    fun layGioDaDat(
        sanId: Int,
        ngayDa: String
    ): Set<Int> {

        val result = mutableSetOf<Int>()

        readableDatabase.rawQuery(
            """
                SELECT gioBatDau FROM DatSan
                WHERE sanId = ? AND ngayDa = ?
            """.trimIndent(),
            arrayOf(sanId.toString(), ngayDa)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(cursor.getInt(0))
            }
        }

        return result
    }

    fun datSan(
        sanId: Int,
        khachHangId: Int,
        ngayDa: String,
        gioBatDau: Int
    ): Boolean {

        if (
            sanId <= 0 ||
            khachHangId <= 0 ||
            gioBatDau !in 6..22
        ) return false

        val values = ContentValues().apply {
            put("sanId", sanId)
            put("khachHangId", khachHangId)
            put("ngayDa", ngayDa)
            put("gioBatDau", gioBatDau)
        }

        return writableDatabase.insertWithOnConflict(
            TABLE_DAT_SAN,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    // ================= LỊCH SỬ KHÁCH HÀNG =================

    fun layLichSuDatSan(
        khachHangId: Int
    ): List<LichSuDatSan> {

        val result = mutableListOf<LichSuDatSan>()
        if (khachHangId <= 0) return result

        val sql = """
            SELECT
                d.id AS maDatSan,
                s.tenSan,
                s.loaiSan,
                s.giaThue,
                d.ngayDa,
                d.gioBatDau
            FROM DatSan d
            INNER JOIN SanBong s ON d.sanId = s.id
            WHERE d.khachHangId = ?
            ORDER BY d.ngayDa DESC, d.gioBatDau DESC
        """.trimIndent()

        readableDatabase.rawQuery(
            sql, arrayOf(khachHangId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                result.add(
                    LichSuDatSan(
                        c.getInt(c.getColumnIndexOrThrow("maDatSan")),
                        c.getString(c.getColumnIndexOrThrow("tenSan")),
                        c.getString(c.getColumnIndexOrThrow("loaiSan")),
                        c.getDouble(c.getColumnIndexOrThrow("giaThue")),
                        c.getString(c.getColumnIndexOrThrow("ngayDa")),
                        c.getInt(c.getColumnIndexOrThrow("gioBatDau"))
                    )
                )
            }
        }

        return result
    }

    // ================= LỊCH ĐẶT CỦA CHỦ SÂN =================

    fun layLichDatTheoChuSan(
        chuSanId: Int
    ): List<LichDatChuSan> {

        val result = mutableListOf<LichDatChuSan>()
        if (chuSanId <= 0) return result

        val sql = """
            SELECT
                d.id AS maDatSan,
                s.tenSan,
                k.hoTen,
                k.soDienThoai,
                d.ngayDa,
                d.gioBatDau,
                s.giaThue
            FROM DatSan d
            INNER JOIN SanBong s
                ON d.sanId = s.id
            INNER JOIN NguoiDung k
                ON d.khachHangId = k.id
            WHERE s.chuSanId = ?
            ORDER BY d.ngayDa DESC, d.gioBatDau DESC
        """.trimIndent()

        readableDatabase.rawQuery(
            sql,
            arrayOf(chuSanId.toString())
        ).use { c ->

            while (c.moveToNext()) {
                result.add(
                    LichDatChuSan(
                        id = c.getInt(
                            c.getColumnIndexOrThrow("maDatSan")
                        ),
                        tenSan = c.getString(
                            c.getColumnIndexOrThrow("tenSan")
                        ),
                        tenKhachHang = c.getString(
                            c.getColumnIndexOrThrow("hoTen")
                        ),
                        soDienThoai = c.getString(
                            c.getColumnIndexOrThrow("soDienThoai")
                        ) ?: "",
                        ngayDa = c.getString(
                            c.getColumnIndexOrThrow("ngayDa")
                        ),
                        gioBatDau = c.getInt(
                            c.getColumnIndexOrThrow("gioBatDau")
                        ),
                        giaThue = c.getDouble(
                            c.getColumnIndexOrThrow("giaThue")
                        )
                    )
                )
            }
        }

        return result
    }

    // ================= HỦY LỊCH ĐẶT =================

    fun huyLichDatChuSan(
        lichDatId: Int,
        chuSanId: Int
    ): Boolean {

        if (lichDatId <= 0 || chuSanId <= 0) {
            return false
        }

        val db = writableDatabase

        // Chỉ được hủy lịch thuộc sân của mình.
        // Không hủy lịch đã bắt đầu hoặc đã qua.
        val thoiDiemHienTai = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.US
        ).format(Date())

        val sql = """
            DELETE FROM DatSan
            WHERE id = ?
            AND EXISTS (
                SELECT 1 FROM SanBong s
                WHERE s.id = DatSan.sanId
                AND s.chuSanId = ?
            )
            AND (
                ngayDa || ' ' ||
                printf('%02d:00', gioBatDau)
            ) > ?
        """.trimIndent()

        db.execSQL(
            sql,
            arrayOf(
                lichDatId,
                chuSanId,
                thoiDiemHienTai
            )
        )

        // Kiểm tra chính xác số dòng đã xóa
        val soDongDaXoa = android.database.DatabaseUtils
            .longForQuery(
                db,
                "SELECT changes()",
                null
            )

        return soDongDaXoa > 0
    }

    // ================= THỐNG KÊ ĐƠN HÔM NAY =================

    fun demDonDatHomNay(chuSanId: Int): Int {

        val homNay = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())

        val sql = """
            SELECT COUNT(*)
            FROM DatSan d
            INNER JOIN SanBong s ON d.sanId = s.id
            WHERE s.chuSanId = ?
            AND d.ngayDa = ?
        """.trimIndent()

        readableDatabase.rawQuery(
            sql,
            arrayOf(chuSanId.toString(), homNay)
        ).use { cursor ->

            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }

        return 0
    }
}
>>>>>>> 2c239c1 (Cap nhat tinh nang quan ly dat san va lich su)
