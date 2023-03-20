package com.givebox.ui.login

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.givebox.common.hideLoading
import com.givebox.common.setNavigationResult
import com.givebox.common.showDialog
import com.givebox.common.showLoading
import com.givebox.databinding.FragmentOtpBinding
import com.givebox.ui.BaseFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class OtpFragment : BaseFragment<FragmentOtpBinding>() {
    private val navArgs by navArgs<OtpFragmentArgs>()
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private var verificationID = ""
    override fun getViewBinding(): FragmentOtpBinding {
        return FragmentOtpBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()
    }

    override fun initViews() {

        val phone = when (navArgs.phone.toString().length) {
            12 -> {
                "+" + navArgs.phone.toString()
            }
            13 -> {
                navArgs.phone.toString()
            }
            else -> {
                "+91" + navArgs.phone.toString()
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        binding.btnVerify.setOnClickListener {
            if (binding.edtOtp.text.isNullOrEmpty()) {
                showDialog(message = "Invalid otp")
            } else {
                showLoading()
                firebaseAuth.signInWithCredential(
                    PhoneAuthProvider.getCredential(
                        verificationID,
                        binding.edtOtp.text.toString()
                    )
                )
                    .addOnSuccessListener {
                        hideLoading()
                        this@OtpFragment.setNavigationResult("isVerified", true)
                        this@OtpFragment.findNavController().popBackStack()
                    }.addOnFailureListener {
                        hideLoading()
                        showDialog(message = it.message.toString())
                    }
            }
        }


        binding.imgBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun observe() {
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
                hideLoading()
                this@OtpFragment.setNavigationResult("isVerified", true)
                this@OtpFragment.findNavController().popBackStack()
            }.addOnFailureListener {
                hideLoading()
                showDialog(message = it.message.toString())
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            hideLoading()
            showDialog(message = e.message.toString())
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            this@OtpFragment.verificationID = verificationId
        }
    }

}