package com.example.myapplication.data.repository

import com.example.myapplication.data.datastore.PinDataStore
import com.example.myapplication.util.PinHasher

class PinRepository(private val pinDataStore: PinDataStore) {

    fun isPinSet(): Boolean = pinDataStore.isPinSetSync()

    fun setPin(pin: String) {
        pinDataStore.setPinSync(PinHasher.hash(pin))
    }

    fun verifyPin(pin: String): Boolean = pinDataStore.verifyPinSync(pin)
}
