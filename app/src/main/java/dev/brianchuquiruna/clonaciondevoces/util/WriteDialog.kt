package dev.brianchuquiruna.clonaciondevoces.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import dev.brianchuquiruna.clonaciondevoces.databinding.DialogLearningBinding
import dev.brianchuquiruna.clonaciondevoces.databinding.DialogLearningWriteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WriteDialog (
    private val onSubmitClickListener: (title:String, description:String, date:Date,qualification:String) -> Unit,
    private val onCancelClickListener: () -> Unit
): DialogFragment() {
        private lateinit var binding : DialogLearningWriteBinding

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            binding = DialogLearningWriteBinding.inflate(LayoutInflater.from(context))
            val builder = AlertDialog.Builder(requireActivity())
            builder.setView(binding.root)
            initUI()
            initListener()
            val dialog = builder.create()
            return dialog
        }

        private fun initUI() {
            with(binding){
                tvDate.text = changeDateFormat(getDate())
            }
        }

        private fun changeDateFormat(date:Date) : String = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date)

        private fun getDate() : Date{
            val fecha = Date()
            fecha.year + 1900
            fecha.month + 1 // Los meses se cuentan desde 0 (enero) a 11 (diciembre)
            fecha.date
            return fecha
        }

        private fun initListener() {
            with(binding){
                btnAceptar.setOnClickListener{
                    onSubmitClickListener.invoke(etTitle.text.toString(),etDescription.text.toString(), getDate(),etNota.text.toString())
                    dismiss()
                }
                btnCancelar.setOnClickListener{
                    onCancelClickListener.invoke()
                    dismiss()
                }
            }
        }

}