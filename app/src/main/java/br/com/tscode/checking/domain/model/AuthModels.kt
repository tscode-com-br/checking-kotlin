package br.com.tscode.checking.domain.model

data class AuthStatus(
    val found: Boolean,
    val chave: String,
    val hasPassword: Boolean,
    val authenticated: Boolean,
    val message: String,
)
