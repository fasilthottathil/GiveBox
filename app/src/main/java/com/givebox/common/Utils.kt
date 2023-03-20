package com.givebox.common

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Fasil on 20/11/22.
 */
inline fun <reified T,reified O> T.mapObject(): O? {
    val type = object : TypeToken<O>(){}.type
    return try {
        Gson().fromJson(Gson().toJson(this), type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

inline fun <reified T> String.toObjectByGson(): T = Gson().fromJson(this, T::class.java)

inline fun <reified T> T.toGsonByObject(): String = Gson().toJson(this)

fun CharSequence?.validatePassword(): Pair<Boolean, String?> {
    return if (this == null) Pair(false, "Password cannot be null") else {
        if (this.isEmpty()) Pair(false, "Password cannot be empty")
        else if (this.isBlank()) Pair(false, "Password cannot be blank")
        else if (this.length < 6) Pair(false, "Password should be at least 6 characters")
        else Pair(true, null)
    }
}

fun CharSequence?.validateInputField(field: String): Pair<Boolean, String?> {
    return if (this == null) Pair(false, "$field cannot be null") else {
        if (this.isEmpty()) Pair(false, "$field cannot be empty")
        else if (this.isBlank()) Pair(false, "$field cannot be blank")
        else Pair(true, null)
    }
}

fun CharSequence?.validateEmail(): Pair<Boolean, String?> {
    return if (this == null) Pair(false, "Email cannot be null") else {
        if (this.isEmpty()) Pair(false, "Email cannot be empty")
        else if (this.isBlank()) Pair(false, "Email cannot be blank")
        else if (!Patterns.EMAIL_ADDRESS.matcher(this).matches()) Pair(false, "Invalid email")
        else Pair(true, null)
    }
}

fun CharSequence?.validatePhone(): Pair<Boolean, String?> {
    return if (this == null) Pair(false, "Phone cannot be null") else {
        if (this.isEmpty()) Pair(false, "Phone cannot be empty")
        else if (this.isBlank()) Pair(false, "Phone cannot be blank")
        else if (this.length < 10 || this.length > 13) Pair(false, "Invalid phone")
        else Pair(true, null)
    }
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun Context?.showListAlertDialog(title: String, list: Array<String>, onSelectItem: ((String)->Unit)) {
    if (this == null) return
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setItems(list) { _, which -> onSelectItem.invoke(list[which]) }.create().show()
}

fun Context?.showQuestionDialog(
    question: String,
    positiveBtn: String,
    negativeBtn: String,
    onSelection: ((Boolean) -> Unit)
) {
    if (this == null) return
    val builder = AlertDialog.Builder(this)
   val dialog =  builder.apply {
        setMessage(question)
        setPositiveButton(positiveBtn) { d, _ ->
            onSelection.invoke(true)
            d.cancel()
        }
        setNegativeButton(negativeBtn) { d, _ ->
            onSelection.invoke(false)
            d.cancel()
        }
    }.create()
    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
    }
    dialog.show()
}

fun Context?.checkPermissions(permissions: Array<String>, granted: (Boolean) -> Unit) {
    if (this == null) {
        granted.invoke(false)
        return
    }
    var isGranted = true
    permissions.forEach {
        if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
            isGranted = false
            return@forEach
        }
    }
    granted.invoke(isGranted)
}