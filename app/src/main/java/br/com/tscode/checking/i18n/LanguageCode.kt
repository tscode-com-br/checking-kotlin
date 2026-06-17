package br.com.tscode.checking.i18n

data class LanguageEntry(
    val code: String,
    val label: String,
    val nativeLabel: String,
    val locale: String,
)

val SUPPORTED_LANGUAGES: List<LanguageEntry> = listOf(
    LanguageEntry("zh", "Chinese", "中文", "zh-CN"),
    LanguageEntry("en", "English", "English", "en-US"),
    LanguageEntry("id", "Indonesian", "Bahasa Indonesia", "id-ID"),
    LanguageEntry("ms", "Malay", "Bahasa Melayu", "ms-MY"),
    LanguageEntry("pt", "Portuguese", "Português", "pt-BR"),
    LanguageEntry("tl", "Tagalog (Filipino)", "Tagalog (Filipino)", "fil-PH"),
)

val LANGUAGE_ALIAS_MAP: Map<String, String> = mapOf(
    "fil" to "tl",
    "tl" to "tl",
    "pt" to "pt",
    "en" to "en",
    "zh" to "zh",
    "ms" to "ms",
    "id" to "id",
    "in" to "id",
)
