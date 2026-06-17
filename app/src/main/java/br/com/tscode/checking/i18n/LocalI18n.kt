package br.com.tscode.checking.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

typealias TranslateFunction = (keyPath: String, values: Map<String, String>?) -> String

@Composable
fun rememberT(langFlow: StateFlow<String>): TranslateFunction {
    val lang: State<String> = langFlow.collectAsState()
    return { keyPath, values -> t(keyPath, values, lang.value) }
}

@Composable
fun tr(langFlow: StateFlow<String>, keyPath: String, values: Map<String, String>? = null): String {
    val lang: State<String> = langFlow.collectAsState()
    return t(keyPath, values, lang.value)
}
