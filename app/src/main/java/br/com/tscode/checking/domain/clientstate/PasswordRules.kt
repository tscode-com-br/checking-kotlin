package br.com.tscode.checking.domain.clientstate

fun isPasswordLengthValid(password: String?): Boolean {
    val raw = password ?: ""
    return raw.length in 3..10 && raw.trim().isNotEmpty()
}

fun isPasswordVerificationInputValid(password: String?): Boolean {
    val raw = password ?: ""
    return raw.length in 1..10
}
