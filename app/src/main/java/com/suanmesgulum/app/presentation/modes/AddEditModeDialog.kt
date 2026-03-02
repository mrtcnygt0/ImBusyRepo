package com.suanmesgulum.app.presentation.modes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.suanmesgulum.app.R
import com.suanmesgulum.app.databinding.DialogAddEditModeBinding
import com.suanmesgulum.app.domain.model.CustomMode

/**
 * Mod ekleme/düzenleme dialog'u.
 * Kullanıcı mod adı ve metin girebilir.
 */
class AddEditModeDialog : DialogFragment() {

    companion object {
        private const val ARG_MODE_ID = "arg_mode_id"
        private const val ARG_MODE_NAME = "arg_mode_name"
        private const val ARG_MODE_TEXT = "arg_mode_text"

        /**
         * Yeni mod eklemek için factory method.
         */
        fun newInstance(): AddEditModeDialog {
            return AddEditModeDialog()
        }

        /**
         * Var olan modu düzenlemek için factory method.
         */
        fun newInstance(mode: CustomMode): AddEditModeDialog {
            return AddEditModeDialog().apply {
                arguments = Bundle().apply {
                    putLong(ARG_MODE_ID, mode.id)
                    putString(ARG_MODE_NAME, mode.name)
                    putString(ARG_MODE_TEXT, mode.text)
                }
            }
        }
    }

    /** Mod kaydedildiğinde çağrılacak callback */
    var onSave: ((id: Long?, name: String, text: String) -> Unit)? = null

    private var _binding: DialogAddEditModeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddEditModeBinding.inflate(LayoutInflater.from(requireContext()))

        val modeId = arguments?.getLong(ARG_MODE_ID, -1L) ?: -1L
        val isEditing = modeId != -1L

        // Düzenleme modunda var olan değerleri doldur
        if (isEditing) {
            binding.etModeName.setText(arguments?.getString(ARG_MODE_NAME, ""))
            binding.etModeText.setText(arguments?.getString(ARG_MODE_TEXT, ""))
        }

        val title = if (isEditing) {
            getString(R.string.dialog_edit_mode_title)
        } else {
            getString(R.string.dialog_add_mode_title)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = binding.etModeName.text.toString().trim()
                val text = binding.etModeText.text.toString().trim()

                if (name.isBlank() || text.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_empty_fields),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                onSave?.invoke(if (isEditing) modeId else null, name, text)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
