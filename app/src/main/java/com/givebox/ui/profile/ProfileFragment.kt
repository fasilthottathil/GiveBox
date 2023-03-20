package com.givebox.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.givebox.R
import com.givebox.common.*
import com.givebox.data.local.pref.AppPreferenceManager
import com.givebox.databinding.FragmentProfileBinding
import com.givebox.ui.BaseFragment
import com.givebox.ui.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val viewModel by viewModels<ProfileViewModel>()
    @Inject
    lateinit var requestManager: RequestManager
    private val myProductsAdapter by lazy { MyProductsAdapter(requestManager) }
    private var mGoogleSignInClient: GoogleSignInClient? = null
    @Inject lateinit var appPreferenceManager: AppPreferenceManager
    override fun getViewBinding(): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()

        context?.let { mGoogleSignInClient = GoogleSignIn.getClient(it, gso) }

        initViews()
        observe()
    }

    override fun initViews() {
        binding.rvProducts.apply {
            adapter = myProductsAdapter.also {
                it.setOnProductDeleteClickListener { productEntity ->
                    context.showQuestionDialog(
                        "Do you want to delete the product ${productEntity.name}?",
                        "Yes",
                        "No"
                    ) { isDelete ->
                        if (isDelete) {
                            viewModel.deleteProduct(productEntity.id)
                        }
                    }
                }
            }
        }

        binding.imgMore.setOnClickListener {
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.menuInflater.inflate(R.menu.profile_popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId) {
                    R.id.settings -> {
                        findNavController().navigateSafely(
                            R.id.profileFragment,
                            R.id.action_profileFragment_to_settingsFragment
                        )
                    }
                    R.id.logout -> {
                        context.showQuestionDialog("Do you want to logout?", "Yes", "No"){ isLogout ->
                            if (isLogout) {
                                appPreferenceManager.logout()
                                mGoogleSignInClient?.signOut()
                                Thread.sleep(200)
                                activity?.finish()
                                activity?.finishAffinity()
                                startActivity(Intent(context, MainActivity::class.java))
                                exitProcess(0)
                            }
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }

        binding.imgBack.setOnClickListener { activity?.onBackPressed() }
    }

    override fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.loading.collectLatest {
                if (it) showLoading() else hideLoading()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.error.collectLatest {
                it?.let {
                    showDialog(message = it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.user.collectLatest {
                it?.let {
                    binding.txtUsername.text = it.name
                    requestManager.load(it.profileUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .circleCrop().into(binding.imgProfile)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.products.collectLatest {
                myProductsAdapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.deleteProduct.collectLatest {
                it?.let {
                    showDialog("Success", "Product deleted successfully")
                }
            }
        }

    }

}