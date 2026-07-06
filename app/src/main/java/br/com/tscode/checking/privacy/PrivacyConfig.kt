package br.com.tscode.checking.privacy

/**
 * LGPD (Lei 13.709/2018) — single source of truth for the legal/business facts the privacy surfaces
 * need. Everything else (the Privacy screen, the prominent disclosure, the rights channel) reads from
 * here, so the whole app stays compliant by editing this one file.
 *
 * Controlador é PESSOA FÍSICA, uso não comercial (gratuito), lançamento apenas no Brasil, hospedagem em
 * Singapura (transferência internacional confirmada). Como agente de tratamento de PEQUENO PORTE
 * (Resolução CD/ANPD nº 2/2022), não há Encarregado (DPO) formalmente indicado — o canal de comunicação
 * para direitos e privacidade é o e-mail em [privacyRequestsEmail].
 *
 * ⚠️ Confirme [controllerLegalName] (nome legal do controlador) antes de publicar. Pessoa física não tem
 * CNPJ e o CPF NÃO deve ser publicado; a identificação do controlador é feita pelo nome + e-mail de contato.
 */
object PrivacyConfig {

    // ── Controlador (art. 9º III/IV) — pessoa natural, sem CNPJ ──────────────────
    const val controllerLegalName = "Tamer Salmem" // CONFIRME o nome legal do controlador
    const val isNaturalPerson = true

    // ── Canal de privacidade / direitos (art. 18); agente de pequeno porte (Res. CD/ANPD 2/2022) ──
    const val privacyRequestsEmail = "tscode.com.br@gmail.com"

    // ── Política de Privacidade pública (exigida pelo Google Play) ───────────────
    // Servida pelo monólito via /checking/ (que remove o prefixo e chega em GET /privacidade no app).
    // A URL "bare" /privacidade exigiria uma rota extra no nginx público do droplet; esta já funciona (200).
    const val privacyPolicyUrl = "https://www.tscode.com.br/checking/privacidade"

    // ── Transferência internacional (art. 33) — confirmado FORA do Brasil ────────
    const val internationalTransfer = true
    const val hostingCountry = "Singapura"

    // ── Retenção (art. 15/16) — descritiva; ajuste se houver prazos fixos ────────
    const val retentionCheckHistory = "enquanto durar o uso e pelo prazo de obrigações legais"
    const val retentionAccidentVideo = "pelo prazo necessário à apuração e a obrigações legais"
    const val retentionLocalLogDays = 30 // já implementado no app (log local de Atividades)

    // ── Idade mínima (art. 14) ───────────────────────────────────────────────────
    const val minimumAge = 18

    private const val PLACEHOLDER_MARK = "PREENCHER:"

    /** True when no legally-required field still holds a placeholder. */
    val isConfigured: Boolean
        get() = listOf(
            controllerLegalName, privacyRequestsEmail, retentionCheckHistory, retentionAccidentVideo,
        ).none { it.contains(PLACEHOLDER_MARK) }
}
