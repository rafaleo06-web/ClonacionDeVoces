package dev.brianchuquiruna.clonaciondevoces.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import dev.brianchuquiruna.clonaciondevoces.databinding.DialogAlertBinding

class AlertDialog (
    private val message:String,
    private val onSubmitClickListener: () -> Unit,
    private val onCancelClickListener: () -> Unit
): DialogFragment() {

    private lateinit var binding : DialogAlertBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAlertBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        initUI()
        initListener()
        return builder.create()
    }

    private fun initUI() {
        binding.tvDescription.text = message
    }

    private fun initListener() {
        with(binding){
            btnConfirm.setOnClickListener{
                onSubmitClickListener.invoke()
                dismiss()
            }
            btnCancel.setOnClickListener{
                onCancelClickListener.invoke()
                dismiss()
            }
        }
    }

}