
package com.example.quanlysanbong

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class SanBongKhachHangAdapter(
    private var danhSachSan: List<SanBong>,
    private val onDatSanClick: (SanBong) -> Unit,
    private val hienThiGioDaDat: (SanBong) -> String
) : RecyclerView.Adapter<SanBongKhachHangAdapter.SanBongViewHolder>() {

    class SanBongViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvTenSan: TextView =
            itemView.findViewById(R.id.tvTenSan)

        val tvLoaiSan: TextView =
            itemView.findViewById(R.id.tvLoaiSan)

        val tvGiaThue: TextView =
            itemView.findViewById(R.id.tvGiaThue)

        val tvTrangThai: TextView =
            itemView.findViewById(R.id.tvTrangThai)

        // Dùng nút có sẵn trong layout
        val btnDatSan: Button =
            itemView.findViewById(R.id.btnXoaSan)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SanBongViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_san_bong_chu_san,
                parent,
                false
            )

        return SanBongViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SanBongViewHolder,
        position: Int
    ) {

        val sanBong = danhSachSan[position]

        holder.tvTenSan.text = sanBong.tenSan
        holder.tvLoaiSan.text = sanBong.loaiSan

        holder.tvGiaThue.text =
            String.format(
                Locale("vi", "VN"),
                "Giá: %,d đ/giờ",
                sanBong.giaThue.toLong()
            )

        // Hiển thị tình trạng sân và các giờ đã đặt
        holder.tvTrangThai.text =
            "${sanBong.trangThai}\n${hienThiGioDaDat(sanBong)}"

        // Đổi nút thành Đặt sân
        holder.btnDatSan.text = "Đặt sân"

        holder.btnDatSan.setTextColor(
            android.graphics.Color.parseColor("#4CAF50")
        )

        // Xử lý khi khách hàng bấm đặt sân
        holder.btnDatSan.setOnClickListener {
            onDatSanClick(sanBong)
        }
    }

    override fun getItemCount(): Int {
        return danhSachSan.size
    }

    fun updateData(newList: List<SanBong>) {
        danhSachSan = newList
        notifyDataSetChanged()
    }
}
