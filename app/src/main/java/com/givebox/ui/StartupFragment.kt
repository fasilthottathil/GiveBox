package com.givebox.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import com.givebox.R
import com.givebox.common.navigateSafely
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.FragmentStartupBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StartupFragment : BaseFragment<FragmentStartupBinding>() {
    @Inject lateinit var appPreferenceManager: AppPreferenceManager
    override fun getViewBinding(): FragmentStartupBinding {
        return FragmentStartupBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        Handler(Looper.myLooper()!!).postDelayed({
            if (appPreferenceManager.isAuthenticated()) {
                findNavController().navigateSafely(
                    R.id.startupFragment,
                    R.id.action_startupFragment_to_mainFragment
                )
            } else {
                findNavController().navigateSafely(
                    R.id.startupFragment,
                    R.id.action_startupFragment_to_loginFragment
                )
            }
        }, 1200)

        observe()
    }

    override fun initViews() {

    }

    override fun observe() {

    }

}