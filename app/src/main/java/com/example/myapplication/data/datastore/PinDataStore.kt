package com.example.myapplication.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PinDataStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pin_prefs", Context.MODE_PRIVATE)

    fun setPinSync(hash: String) {
        prefs.edit {
            putString(KEY_PIN_HASH, hash)
            putBoolean(KEY_IS_PIN_SET, true)
        }
    }

    fun clearPinSync() {
        prefs.edit { clear() }
    }

    fun verifyPinSync(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return com.example.myapplication.util.PinHasher.verify(pin, storedHash)
    }

    fun isPinSetSync(): Boolean = prefs.getBoolean(KEY_IS_PIN_SET, false)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_IS_PIN_SET = "is_pin_set"
    }
}
