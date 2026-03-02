package com.suanmesgulum.app.presentation.logs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.suanmesgulum.app.R
import com.suanmesgulum.app.databinding.ItemLogBinding
import com.suanmesgulum.app.domain.model.CallLogItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Arama logları için RecyclerView Adapter'ı.
 */
class LogAdapter(
    private val onDeleteClick: (CallLogItem) -> Unit
) : ListAdapter<CallLogItem, LogAdapter.LogViewHolder>(LogDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("tr"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LogViewHolder(
        private val binding: ItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log: CallLogItem) {
            binding.apply {
                // Arayan kişi bilgisi
                tvCallerInfo.text = log.contactName ?: log.phoneNumber

                // Numara (isim varsa altında göster)
                if (log.contactName != null) {
                    tvPhoneNumber.text = log.phoneNumber
                    tvPhoneNumber.visibility = android.view.View.VISIBLE
                } else {
                    tvPhoneNumber.visibility = android.view.View.GONE
                }

                // Seçilen mod
                tvModeName.text = log.selectedModeName

                // Tarih ve saat
                tvTimestamp.text = dateFormat.format(Date(log.timestamp))

                // Premium rozeti
                if (log.isPaidFeatureUsed) {
                    tvModeName.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_premium, 0, 0, 0
                    )
                } else {
                    tvModeName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }

                // Sil butonu
                btnDeleteLog.setOnClickListener {
                    onDeleteClick(log)
                }
            }
        }
    }

    private class LogDiffCallback : DiffUtil.ItemCallback<CallLogItem>() {
        override fun areItemsTheSame(oldItem: CallLogItem, newItem: CallLogItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CallLogItem, newItem: CallLogItem): Boolean {
            return oldItem == newItem
        }
    }
}
