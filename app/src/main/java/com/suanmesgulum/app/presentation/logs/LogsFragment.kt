package com.suanmesgulum.app.presentation.logs

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suanmesgulum.app.R
import com.suanmesgulum.app.databinding.FragmentLogsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Geçmiş Loglar Ekranı.
 *
 * Tüm işlenmiş arama kayıtlarını kronolojik sırayla listeler.
 * Tek tek veya toplu silme desteği sunar.
 */
@AndroidEntryPoint
class LogsFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogsViewModel by viewModels()
    private lateinit var logAdapter: LogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClearButton()
        observeState()
    }

    /**
     * RecyclerView kurulumu.
     */
    private fun setupRecyclerView() {
        logAdapter = LogAdapter { logItem ->
            showDeleteConfirmation(logItem)
        }

        binding.rvLogs.apply {
            adapter = logAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * "Tümünü Temizle" butonu kurulumu.
     */
    private fun setupClearButton() {
        binding.btnClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }

    /**
     * State gözlemle.
     */
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.logs.collect { logs ->
                        logAdapter.submitList(logs)

                        // Boş durum
                        binding.tvEmptyLogs.visibility = if (logs.isEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                        // Temizle butonu
                        binding.btnClearAll.visibility = if (logs.isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is LogsViewModel.UiEvent.ShowMessage -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Tek log silme onay dialog'u.
     */
    private fun showDeleteConfirmation(logItem: com.suanmesgulum.app.domain.model.CallLogItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_log_title))
            .setMessage(getString(R.string.delete_log_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteLog(logItem)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Toplu silme onay dialog'u.
     */
    private fun showClearAllConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.clear_all_logs_title))
            .setMessage(getString(R.string.clear_all_logs_message))
            .setPositiveButton(getString(R.string.clear)) { _, _ ->
                viewModel.clearAllLogs()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
