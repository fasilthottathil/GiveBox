package com.givebox.common

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.givebox.ui.custom.ErrorDialogFragment
import com.givebox.ui.custom.LoadingDialogFragment

/**
 * Created by Fasil on 20/11/22.
 */
fun <T> Fragment.setNavigationResult(key: String, value: T) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(
        key,
        value
    )
}

fun <T> Fragment.getNavigationResult(@IdRes id: Int, key: String, onResult: (result: T) -> Unit) {
    val navBackStackEntry = findNavController().getBackStackEntry(id)

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && navBackStackEntry.savedStateHandle.contains(key)) {
            val result = navBackStackEntry.savedStateHandle.get<T>(key)
            result?.let(onResult)
            navBackStackEntry.savedStateHandle.remove<T>(key)
        }
    }
    navBackStackEntry.lifecycle.addObserver(observer)

    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            navBackStackEntry.lifecycle.removeObserver(observer)
        }
    })
}

fun NavController.navigateSafely(srcId: Int, actionId: Int, bundle: Bundle? = null) {
    if (currentDestination?.id == srcId) {
        navigate(actionId, bundle)
    }
}

fun NavController.navigateSafely(srcId: Int, navDirections: NavDirections) {
    if (currentDestination?.id == srcId) {
        navigate(navDirections)
    }
}

private var loadingDialog: LoadingDialogFragment? = null
fun Fragment.showLoading() {
    loadingDialog = LoadingDialogFragment.getInstance()
    loadingDialog?.isCancelable = false
    loadingDialog?.show(childFragmentManager, this.tag)
}

fun hideLoading() = loadingDialog?.dismiss()

private var errorDialogFragment: ErrorDialogFragment? = null
fun Fragment.showDialog(title: String = "Error", message: String) {
    errorDialogFragment = ErrorDialogFragment.newInstance(title, message)
    errorDialogFragment?.show(childFragmentManager, this.tag)
}

private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
fun Fragment.registerForActivityResults(onActivityResult: (Intent?) -> Unit) {
    activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            onActivityResult(it.data)
        }
    }
}

fun launchActivityResult(intent: Intent) {
    activityResultLauncher?.launch(intent)
}

fun DialogFragment.setWidthPercent(percentage: Int, height: Int?) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    if (height != null) {
        val percentHeight = rect.height() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
    } else {
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

fun Fragment.isLargeScreen(): Boolean {
    val screenSize: Int = resources.configuration.screenLayout and
            Configuration.SCREENLAYOUT_SIZE_MASK
    return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE
}

fun DialogFragment.setScreenSizeAwareWidth(defaultWidth: Int = 70, height: Int? = null) {
    if (isLargeScreen()) {
        setWidthPercent(defaultWidth,height)
    } else {
        setFullScreen()
    }
}

fun DialogFragment.setFullScreen() {
    dialog?.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}