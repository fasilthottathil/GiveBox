package com.givebox.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.givebox.R
import com.givebox.common.showQuestionDialog
import com.givebox.databinding.FragmentMainBinding
import com.givebox.ui.chat.ChatFragment
import com.givebox.ui.custom.ErrorDialogFragment
import com.givebox.ui.custom.LoadingDialogFragment
import com.givebox.ui.favourite.FavouriteFragment
import com.givebox.ui.home.HomeFragment
import com.givebox.ui.post.AddPostFragment
import com.givebox.ui.post.FilterPostFragment
import com.givebox.ui.profile.ProfileFragment
import kotlin.system.exitProcess


class MainFragment : BaseFragment<FragmentMainBinding>() {
    override fun getViewBinding(): FragmentMainBinding {
        return FragmentMainBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observe()

    }

    override fun initViews() {
        binding.bottomNav.setupWithNavController( Navigation.findNavController(binding.navHostFragmentMain))
        binding.bottomNav.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(
                item,
                Navigation.findNavController(binding.navHostFragmentMain)
            )
            return@setOnItemSelectedListener true
        }

        childFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                when(f) {
                    is HomeFragment -> binding.bottomNav.visibility = View.VISIBLE
                    is NavHostFragment -> binding.bottomNav.visibility = View.VISIBLE
                    is MainFragment -> binding.bottomNav.visibility = View.VISIBLE
                    is ChatFragment -> binding.bottomNav.visibility = View.VISIBLE
                    is ProfileFragment -> binding.bottomNav.visibility = View.VISIBLE
                    is AddPostFragment -> binding.bottomNav.visibility = View.VISIBLE
                    is FavouriteFragment -> binding.bottomNav.visibility = View.VISIBLE
                    else -> {
                        if (f !is ErrorDialogFragment && f !is LoadingDialogFragment && f !is FilterPostFragment) {
                            binding.bottomNav.visibility = View.GONE
                        }
                    }
                }
            }
        }, true)

    }

    override fun observe() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentDestId = Navigation.findNavController(binding.navHostFragmentMain).currentDestination?.id
                    if (currentDestId != R.id.mainFragment && currentDestId != R.id.homeFragment && currentDestId != R.id.chatFragment && currentDestId != R.id.favouriteFragment && currentDestId != R.id.addPostFragment && currentDestId != R.id.profileFragment) {
                        Navigation.findNavController(binding.navHostFragmentMain).popBackStack()
                    } else {
                        context.showQuestionDialog("Do you want to exit?","Yes","No"){
                            if (it) {
                                activity?.finishAffinity()
                                exitProcess(0)
                            }
                        }
                    }
                }
            })
    }

}
