package com.example.myapplication.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(private val pinRepo: PinRepository) : ViewModel() {

    enum class Step { CURRENT, NEW, CONFIRM }

    data class UiState(
        val step: Step = Step.CURRENT,
        val currentPin: String = "",
        val newPin: String = "",
        val confirmPin: String = "",
        val error: String? = null,
        val success: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun onDigit(d: Int) {
        val s = _state.value
        val updated = when (s.step) {
            Step.CURRENT -> s.copy(currentPin = s.currentPin + d)
            Step.NEW -> s.copy(newPin = s.newPin + d)
            Step.CONFIRM -> s.copy(confirmPin = s.confirmPin + d)
        }
        _state.value = updated.copy(error = null)
        checkComplete()
    }

    fun onDelete() {
        _state.update {
            when (it.step) {
                Step.CURRENT -> it.copy(currentPin = it.currentPin.dropLast(1))
                Step.NEW -> it.copy(newPin = it.newPin.dropLast(1))
                Step.CONFIRM -> it.copy(confirmPin = it.confirmPin.dropLast(1))
            }.copy(error = null)
        }
    }

    private fun checkComplete() {
        val s = _state.value
        when (s.step) {
            Step.CURRENT -> {
                if (s.currentPin.length == 4) {
                    if (pinRepo.verifyPin(s.currentPin)) {
                        _state.update { it.copy(step = Step.NEW, currentPin = "", error = null) }
                    } else {
                        _state.update { it.copy(currentPin = "", error = "Incorrect PIN") }
                    }
                }
            }
            Step.NEW -> {
                if (s.newPin.length == 4) _state.update { it.copy(step = Step.CONFIRM, error = null) }
            }
            Step.CONFIRM -> {
                if (s.confirmPin.length == 4) {
                    if (s.confirmPin == s.newPin) {
                        pinRepo.setPin(s.confirmPin)
                        _state.update { UiState(success = true) }
                    } else {
                        _state.update { it.copy(newPin = "", confirmPin = "", step = Step.NEW, error = "PINs do not match") }
                    }
                }
            }
        }
    }

    class Factory(private val pinRepo: PinRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(pinRepo) as T
    }
}
