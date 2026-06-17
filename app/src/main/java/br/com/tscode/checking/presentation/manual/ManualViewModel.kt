package br.com.tscode.checking.presentation.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.i18n.DEFAULT_LANGUAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ManualViewModel @Inject constructor(
    prefs: AppPreferencesDataSource,
) : ViewModel() {

    val languageFlow = prefs.language
        .map { it.ifBlank { DEFAULT_LANGUAGE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_LANGUAGE)
}
