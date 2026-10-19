package com.example.quanlysanbong

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SanBongKhachHangAdapter(
    private var danhSachSan: List<SanBong>,
    private val onDatSanClick: (SanBong) -> Unit
) : RecyclerView.Adapter<SanBongKhachHangAdapter.SanBongViewHolder>() {

    class SanBongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTenSan: TextView = itemView.findViewById(R.id.tvTenSan)
        val tvLoaiSan: TextView = itemView.findViewById(R.id.tvLoaiSan)
        val tvGiaThue: TextView = itemView.findViewById(R.id.tvGiaThue)
        val tvTrangThai: TextView = itemView.findViewById(R.id.tvTrangThai)
        val btnDatSan: Button = itemView.findViewById(R.id.btnXoaSan) // Tận dụng nút hoặc đổi ID thành btnDatSan
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SanBongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_san_bong_chu_san, parent, false)
        return SanBongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SanBongViewHolder, position: Int) {
        val sanBong = danhSachSan[position]
        holder.tvTenSan.text = sanBong.tenSan
        holder.tvLoaiSan.text = sanBong.loaiSan
        holder.tvGiaThue.text = "Giá: %,d đ/giờ".format(sanBong.giaThue.toLong())
        holder.tvTrangThai.text = "Trạng thái: ${sanBong.trangThai}"

        // Đổi chữ nút thành "Đặt sân" cho khách hàng
        holder.btnDatSan.text = "Đặt sân"
        holder.btnDatSan.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

        holder.btnDatSan.setOnClickListener {
            onDatSanClick(sanBong)
        }
    }

    override fun getItemCount(): Int = danhSachSan.size

    fun updateData(newList: List<SanBong>) {
        danhSachSan = newList
        notifyDataSetChanged()
    }
}