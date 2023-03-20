package com.givebox.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.givebox.R
import com.givebox.databinding.LoadingLayoutBinding

/**
 *Created by Fasil on 7/16/2022
 */
class LoadingDialogFragment : DialogFragment() {
    private var _binding: LoadingLayoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.round_shape)
        _binding = LoadingLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun getInstance() = LoadingDialogFragment()
    }
}