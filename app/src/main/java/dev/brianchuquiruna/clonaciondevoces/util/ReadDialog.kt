package dev.brianchuquiruna.clonaciondevoces.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import dev.brianchuquiruna.clonaciondevoces.databinding.DialogLearningBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReadDialog(
    private val title:String,
    private val description:String,
    private val date: Date,
    private val qualification:Int,
    private val onSubmitClickListener: (description:String,qualification:String) -> Unit,
    private val onCancelClickListener: () -> Unit
):DialogFragment() {
    private lateinit var binding :DialogLearningBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogLearningBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        initUI()
        initListener()
        val dialog = builder.create()
        return dialog
    }

    private fun initUI() {
        with(binding){
            tvTitle.text = title
            etDescription.setText(description)
            etNota.setText(qualification.toString())
            tvDate.text= changeDateFormat(date)
        }
    }

    private fun changeDateFormat(date:Date) : String = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date)


    private fun initListener() {
        with(binding){
            btnAceptar.setOnClickListener{
                onSubmitClickListener.invoke(etDescription.text.toString(),etNota.text.toString())
                dismiss()
            }
            btnCancelar.setOnClickListener{
                onCancelClickListener.invoke()
                dismiss()
            }
        }
    }
}