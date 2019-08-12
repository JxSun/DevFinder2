package com.jxsun.devfinder.util

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat

class NetworkChecker(
        private val context: Context
) {

    fun isNetworkConnected(): Boolean {
        val networkInfo = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
                ?.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}