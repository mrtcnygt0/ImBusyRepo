package com.suanmesgulum.app.presentation.voicemail

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.suanmesgulum.app.R
import com.suanmesgulum.app.domain.model.Voicemail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

/**
 * Sesli Mesajlar Ekranı: Kaydedilmiş sesli mesajları listeler, oynatır.
 */
@AndroidEntryPoint
class VoicemailActivity : AppCompatActivity() {

    private val viewModel: VoicemailViewModel by viewModels()
    private lateinit var rvVoicemails: RecyclerView
    private lateinit var tvEmpty: TextView
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voicemail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvVoicemails = findViewById(R.id.rvVoicemails)
        tvEmpty = findViewById(R.id.tvVoicemailEmpty)
        rvVoicemails.layoutManager = LinearLayoutManager(this)

        val adapter = VoicemailAdapter(
            onPlayClick = { playVoicemail(it) },
            onItemClick = { showVoicemailDetail(it) }
        )
        rvVoicemails.adapter = adapter

        lifecycleScope.launch {
            viewModel.voicemails.collectLatest { list ->
                adapter.submitList(list)
                tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                rvVoicemails.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun playVoicemail(voicemail: Voicemail) {
        val path = voicemail.audioFilePath
        if (path == null || !File(path).exists()) {
            Toast.makeText(this, "Ses dosyası bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        stopPlayback()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
        viewModel.markAsListened(voicemail.id)
    }

    private fun showVoicemailDetail(voicemail: Voicemail) {
        val transcript = voicemail.transcript ?: getString(R.string.voicemail_no_transcript)
        AlertDialog.Builder(this)
            .setTitle(voicemail.callerName ?: voicemail.callerNumber)
            .setMessage("${getString(R.string.voicemail_transcript_label)}:\n$transcript")
            .setPositiveButton(getString(R.string.voicemail_play)) { _, _ -> playVoicemail(voicemail) }
            .setNeutralButton(getString(R.string.voicemail_archive)) { _, _ -> viewModel.archiveVoicemail(voicemail.id) }
            .setNegativeButton(getString(R.string.delete)) { _, _ ->
                AlertDialog.Builder(this)
                    .setMessage(R.string.voicemail_delete_confirm)
                    .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteVoicemail(voicemail) }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
            .show()

        viewModel.markAsListened(voicemail.id)
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }
}
