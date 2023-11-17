package dev.brianchuquiruna.clonaciondevoces.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import dev.brianchuquiruna.clonaciondevoces.databinding.DialogAddAudioBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddAudioDialog (
    private val onSubmitClickListener: (title:String, date:Date) -> Unit,
    private val onCancelClickListener: () -> Unit
): DialogFragment() {
    private lateinit var binding : DialogAddAudioBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddAudioBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        initListener()
        val dialog = builder.create()
        return dialog
    }

    private fun changeDateFormat(date: Date) : String = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date)

    private fun getDate() : Date {
        val fecha = Date()
        fecha.year + 1900
        fecha.month + 1 // Los meses se cuentan desde 0 (enero) a 11 (diciembre)
        fecha.date
        return fecha
    }

    private fun initListener() {
        with(binding){
            btnAceptar.setOnClickListener{
                onSubmitClickListener.invoke(etTitle.text.toString(), getDate())
                dismiss()
            }
            btnCancelar.setOnClickListener{
                onCancelClickListener.invoke()
                dismiss()
            }
        }
    }

}