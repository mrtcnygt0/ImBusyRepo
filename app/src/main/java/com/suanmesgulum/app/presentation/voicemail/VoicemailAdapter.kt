package com.suanmesgulum.app.presentation.voicemail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.suanmesgulum.app.R
import com.suanmesgulum.app.domain.model.Voicemail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VoicemailAdapter(
    private val onPlayClick: (Voicemail) -> Unit,
    private val onItemClick: (Voicemail) -> Unit
) : ListAdapter<Voicemail, VoicemailAdapter.VH>(VoicemailDiffCallback()) {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCaller: TextView = itemView.findViewById(R.id.tvVoicemailCaller)
        val tvDate: TextView = itemView.findViewById(R.id.tvVoicemailDate)
        val tvDuration: TextView = itemView.findViewById(R.id.tvVoicemailDuration)
        val tvUnread: TextView = itemView.findViewById(R.id.tvUnreadBadge)
        val ivPlay: ImageView = itemView.findViewById(R.id.ivPlayVoicemail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voicemail, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        holder.tvCaller.text = item.callerName ?: item.callerNumber
        holder.tvDate.text = dateFormat.format(Date(item.receivedTime))
        holder.tvDuration.text = formatDuration(item.duration)
        holder.tvUnread.visibility = if (!item.isListened) View.VISIBLE else View.GONE

        holder.ivPlay.setOnClickListener { onPlayClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    private fun formatDuration(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%d:%02d", min, sec)
    }

    class VoicemailDiffCallback : DiffUtil.ItemCallback<Voicemail>() {
        override fun areItemsTheSame(oldItem: Voicemail, newItem: Voicemail) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Voicemail, newItem: Voicemail) = oldItem == newItem
    }
}
