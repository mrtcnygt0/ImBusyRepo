package com.suanmesgulum.app.presentation.modeselect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.suanmesgulum.app.databinding.ItemModeSelectBinding
import com.suanmesgulum.app.domain.model.CustomMode

/**
 * Mod seçim listesi için Adapter.
 */
class ModeSelectAdapter(
    private val onModeSelected: (CustomMode) -> Unit
) : ListAdapter<CustomMode, ModeSelectAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemModeSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemModeSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mode: CustomMode) {
            binding.tvSelectModeName.text = mode.name
            binding.tvSelectModeText.text = mode.text
            binding.root.setOnClickListener {
                onModeSelected(mode)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CustomMode>() {
        override fun areItemsTheSame(oldItem: CustomMode, newItem: CustomMode) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CustomMode, newItem: CustomMode) =
            oldItem == newItem
    }
}
