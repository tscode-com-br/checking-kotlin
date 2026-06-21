package br.com.tscode.checking.domain.model

data class AuthStatus(
    val found: Boolean,
    val chave: String,
    val hasPassword: Boolean,
    val authenticated: Boolean,
    val message: String,
    // plan003 — server-derived: pendingApproval = self-registration awaiting admin approval (no User yet);
    // queueFull = the pending queue (cap) was full at registration time (transient, only from selfRegister).
    val pendingApproval: Boolean = false,
    val queueFull: Boolean = false,
)
