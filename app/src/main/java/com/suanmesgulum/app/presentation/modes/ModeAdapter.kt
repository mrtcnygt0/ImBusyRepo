package com.suanmesgulum.app.presentation.modes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.suanmesgulum.app.R
import com.suanmesgulum.app.databinding.ItemModeBinding
import com.suanmesgulum.app.domain.model.CustomMode

/**
 * Modlar listesi için RecyclerView Adapter'ı.
 */
class ModeAdapter(
    private val onEditClick: (CustomMode) -> Unit,
    private val onDeleteClick: (CustomMode) -> Unit
) : ListAdapter<CustomMode, ModeAdapter.ModeViewHolder>(ModeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeViewHolder {
        val binding = ItemModeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ModeViewHolder(
        private val binding: ItemModeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mode: CustomMode) {
            binding.apply {
                tvModeName.text = mode.name
                tvModeText.text = mode.text

                // Varsayılan mod rozeti
                if (mode.isDefault) {
                    tvModeName.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_star, 0
                    )
                } else {
                    tvModeName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }

                btnEdit.setOnClickListener { onEditClick(mode) }
                btnDelete.setOnClickListener { onDeleteClick(mode) }

                // Varsayılan mod silinemez - sil butonunu gizle
                btnDelete.alpha = if (mode.isDefault) 0.3f else 1.0f
                btnDelete.isEnabled = !mode.isDefault
            }
        }
    }

    private class ModeDiffCallback : DiffUtil.ItemCallback<CustomMode>() {
        override fun areItemsTheSame(oldItem: CustomMode, newItem: CustomMode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomMode, newItem: CustomMode): Boolean {
            return oldItem == newItem
        }
    }
}
