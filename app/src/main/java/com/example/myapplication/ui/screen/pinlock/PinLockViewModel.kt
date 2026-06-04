package com.example.myapplication.ui.screen.pinlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PinLockViewModel(private val pinRepo: PinRepository) : ViewModel() {

    enum class Mode { SET_PIN, CONFIRM_PIN, ENTER_PIN }

    data class UiState(
        val mode: Mode = Mode.ENTER_PIN,
        val pin: String = "",
        val firstPin: String = "",
        val error: String? = null,
        val unlocked: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        if (!pinRepo.isPinSet()) {
            _state.update { it.copy(mode = Mode.SET_PIN) }
        }
    }

    fun onDigit(digit: Int) {
        val current = _state.value
        if (current.error != null) {
            _state.update { it.copy(error = null, pin = "") }
        }
        val newPin = current.pin + digit
        _state.update { it.copy(pin = newPin) }
        if (newPin.length == 4) onPinComplete(newPin)
    }

    fun onDelete() {
        _state.update { it.copy(pin = it.pin.dropLast(1), error = null) }
    }

    private fun onPinComplete(pin: String) {
        when (_state.value.mode) {
            Mode.SET_PIN -> _state.update {
                it.copy(mode = Mode.CONFIRM_PIN, firstPin = pin, pin = "")
            }
            Mode.CONFIRM_PIN -> {
                if (pin == _state.value.firstPin) {
                    pinRepo.setPin(_state.value.firstPin)
                    _state.update { it.copy(unlocked = true) }
                } else {
                    _state.update {
                        it.copy(error = "PINs do not match", pin = "", firstPin = "", mode = Mode.SET_PIN)
                    }
                }
            }
            Mode.ENTER_PIN -> {
                if (pinRepo.verifyPin(pin)) {
                    _state.update { it.copy(unlocked = true) }
                } else {
                    _state.update { it.copy(error = "Incorrect PIN", pin = "") }
                }
            }
        }
    }

    fun resetError() {
        _state.update { it.copy(error = null, pin = "") }
    }

    class Factory(private val pinRepo: PinRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PinLockViewModel(pinRepo) as T
    }
}
