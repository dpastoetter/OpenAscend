package com.openascend.app.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.Habit
import com.openascend.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HabitEditUi {
    data object Loading : HabitEditUi
    data object NotFound : HabitEditUi
    data class Ready(val habit: Habit) : HabitEditUi
}

@HiltViewModel
class HabitEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: -1L

    private val _ui = MutableStateFlow<HabitEditUi>(HabitEditUi.Loading)
    val uiState = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            if (habitId < 0L) {
                _ui.value = HabitEditUi.NotFound
                return@launch
            }
            val h = habitRepository.getHabit(habitId)
            _ui.value = if (h == null) HabitEditUi.NotFound else HabitEditUi.Ready(h)
        }
    }

    fun save(name: String, frequencyPerWeek: Int, difficulty: Int, linkedStat: CoreStat, isRestDay: Boolean) {
        val current = (_ui.value as? HabitEditUi.Ready)?.habit ?: return
        viewModelScope.launch {
            habitRepository.updateHabit(
                current.copy(
                    name = name.trim(),
                    frequencyPerWeek = frequencyPerWeek.coerceIn(1, 7),
                    difficulty = difficulty.coerceIn(1, 5),
                    linkedStat = linkedStat,
                    isRestDay = isRestDay,
                ),
            )
            val refreshed = habitRepository.getHabit(habitId)
            _ui.value = if (refreshed == null) HabitEditUi.NotFound else HabitEditUi.Ready(refreshed)
        }
    }
}
