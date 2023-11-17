package dev.brianchuquiruna.clonaciondevoces

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentChooseOptionBinding
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentStudyHallBinding

class ChooseOptionFragment : Fragment() {

    private lateinit var binding: FragmentChooseOptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChooseOptionBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.btnLearn.setOnClickListener {
            findNavController().navigate(R.id.action_chooseOptionFragment_to_allLearningSesionsFragment)
        }

        binding.btnActivitysLearn.setOnClickListener {
            findNavController().navigate(R.id.action_chooseOptionFragment_to_learningFragment)
        }
    }

}