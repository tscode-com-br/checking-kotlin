package br.com.tscode.checking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebPasswordStatusResponse(
    val found: Boolean,
    val chave: String,
    @SerialName("has_password") val hasPassword: Boolean,
    val authenticated: Boolean,
    val message: String,
)

@Serializable
data class WebPasswordRegisterRequest(
    val chave: String,
    val projeto: String? = null,
    val senha: String,
)

@Serializable
data class WebUserSelfRegistrationRequest(
    val chave: String,
    val nome: String,
    val projetos: List<String>,
    val email: String? = null,
    val senha: String,
    @SerialName("confirmar_senha") val confirmarSenha: String,
)

@Serializable
data class WebPasswordLoginRequest(
    val chave: String,
    val senha: String,
)

@Serializable
data class WebPasswordChangeRequest(
    val chave: String,
    @SerialName("senha_antiga") val senhaAntiga: String,
    @SerialName("nova_senha") val novaSenha: String,
)

@Serializable
data class WebPasswordActionResponse(
    val ok: Boolean,
    val authenticated: Boolean,
    @SerialName("has_password") val hasPassword: Boolean,
    val message: String,
)

@Serializable
data class WebUserSelfRegistrationResponse(
    val ok: Boolean,
    val authenticated: Boolean,
    @SerialName("has_password") val hasPassword: Boolean,
    val message: String,
    val projects: List<String>,
    @SerialName("active_project") val activeProject: String,
)
