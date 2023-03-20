package com.givebox.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.givebox.R
import com.givebox.databinding.FragmentErrorDialogBinding


class ErrorDialogFragment : DialogFragment() {
    private var _binding: FragmentErrorDialogBinding? = null
    private val binding get() = _binding!!
    private var errorTitle: String? = null
    private var errorMessage: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        dialog?.window?.setBackgroundDrawableResource(R.drawable.error_shape)
        arguments?.let {
            errorTitle = it.getString("title")
            errorMessage = it.getString("message")
        }
        _binding = FragmentErrorDialogBinding.inflate(layoutInflater)

        initViews()

        return binding.root
    }

    private fun initViews() {
        binding.txtErrorTitle.text = errorTitle
        binding.txtErrorMessage.text = errorMessage
        binding.txtClose.setOnClickListener { dialog?.dismiss() }
    }

    companion object {
        fun newInstance(title: String?, message: String): ErrorDialogFragment {
            return ErrorDialogFragment().apply {
                this.arguments = Bundle().apply {
                    putString("title", title)
                    putString("message", message)
                }
            }
        }
    }
}