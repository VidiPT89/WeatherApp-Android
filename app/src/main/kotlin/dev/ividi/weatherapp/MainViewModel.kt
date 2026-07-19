package dev.ividi.weatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ividi.weatherapp.data.repository.AppPreferencesRepository
import dev.ividi.weatherapp.data.repository.ThemeMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Exposes the persisted theme override to [MainActivity], read once at the composition root. */
@HiltViewModel
class MainViewModel @Inject constructor(
    appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = appPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
}
