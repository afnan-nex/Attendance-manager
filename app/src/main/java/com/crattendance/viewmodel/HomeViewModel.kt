package com.crattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crattendance.data.model.ClassEntity
import com.crattendance.data.repository.IClassRepository
import com.crattendance.data.repository.ISettingsRepository
import com.crattendance.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDays: List<LocalDate> = emptyList(),
    val todayClasses: List<ClassEntity> = emptyList()
)

class HomeViewModel(
    private val classRepository: IClassRepository,
    settingsRepository: ISettingsRepository
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val windowStart = MutableStateFlow(DateUtils.weekStart(LocalDate.now()))
    @Suppress("UNUSED_VARIABLE") // settings subscription keeps this screen warm to section changes
    private val settings = settingsRepository.settings

    val uiState = combine(
        windowStart,
        selectedDate,
        classRepository.visibleClasses
    ) { start, date, classes ->
        HomeUiState(
            selectedDate = date,
            weekDays = DateUtils.weekDays(start),
            todayClasses = classes
                .filter { it.dayOfWeek == date.dayOfWeek.value }
                .sortedWith(compareBy<ClassEntity> { it.dayOfWeek }.thenBy { it.startTime })
        )
    }.stateIn(
        scope = viewModelScope,
        // Eagerly: the ViewModel now lives for the whole activity, so collect
        // once at creation and keep the data warm across tab switches.
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState()
    )

    /** Selects a day within the visible window, or slides the window to its week. */
    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        if (date !in DateUtils.weekDays(windowStart.value)) {
            windowStart.value = DateUtils.weekStart(date)
        }
    }
}
