package com.suanmesgulum.app.presentation.modes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suanmesgulum.app.R
import com.suanmesgulum.app.databinding.FragmentModesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Modları Yönet Ekranı.
 *
 * Kullanıcının tanımladığı meşgul modlarını listeler.
 * Mod ekleme, düzenleme ve silme işlemlerini sağlar.
 */
@AndroidEntryPoint
class ModesFragment : Fragment() {

    private var _binding: FragmentModesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ModesViewModel by viewModels()
    private lateinit var modeAdapter: ModeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeState()

        // İlk çalıştırmada varsayılan modları oluştur
        viewModel.createDefaultModes()
    }

    /**
     * RecyclerView kurulumu.
     */
    private fun setupRecyclerView() {
        modeAdapter = ModeAdapter(
            onEditClick = { mode ->
                viewModel.requestEdit(mode)
            },
            onDeleteClick = { mode ->
                showDeleteConfirmation(mode)
            }
        )

        binding.rvModes.apply {
            adapter = modeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * FAB (Floating Action Button) kurulumu.
     */
    private fun setupFab() {
        binding.fabAddMode.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    /**
     * State gözlemle.
     */
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Mod listesini gözlemle
                launch {
                    viewModel.modes.collect { modes ->
                        modeAdapter.submitList(modes)

                        // Boş durum göster/gizle
                        binding.tvEmptyModes.visibility = if (modes.isEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }

                // Olayları gözlemle
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is ModesViewModel.UiEvent.ShowMessage -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                            is ModesViewModel.UiEvent.EditMode -> {
                                showAddEditDialog(event.mode)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Mod ekleme/düzenleme dialog'unu göster.
     */
    private fun showAddEditDialog(mode: com.suanmesgulum.app.domain.model.CustomMode?) {
        val dialog = if (mode != null) {
            AddEditModeDialog.newInstance(mode)
        } else {
            AddEditModeDialog.newInstance()
        }

        dialog.onSave = { id, name, text ->
            if (id != null) {
                viewModel.updateMode(id, name, text)
            } else {
                viewModel.addMode(name, text)
            }
        }

        dialog.show(childFragmentManager, "add_edit_mode")
    }

    /**
     * Silme onay dialog'u göster.
     */
    private fun showDeleteConfirmation(mode: com.suanmesgulum.app.domain.model.CustomMode) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_mode_title))
            .setMessage(getString(R.string.delete_mode_message, mode.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteMode(mode)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
