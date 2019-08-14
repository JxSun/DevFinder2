package com.jxsun.devfinder.data.source.local

import android.content.Context
import com.jxsun.devfinder.util.extension.edit

private const val PREFS_NAME = "com.jxsun.devfinder.prefs"
private const val KEY_NEXT_USER_INDEX = "KEY_NEXT_USER_INDEX"

class AppPreferences(
    private val context: Context
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var nextUserIndex: Int
        get() = preferences.getInt(KEY_NEXT_USER_INDEX, 0)
        set(value) {
            preferences.edit {
                it.putInt(KEY_NEXT_USER_INDEX, value)
            }
        }
}
