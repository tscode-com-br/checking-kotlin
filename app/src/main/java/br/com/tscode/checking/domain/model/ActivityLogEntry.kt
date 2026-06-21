package br.com.tscode.checking.domain.model

import java.time.Instant

// plan004 §3.1/§3.3 — the Activities debug log model. The table is ENGLISH-ONLY; `description` is built
// (in English) at log time. `kind` is the "Activity" column text; `severity` drives the row color only.

enum class ActivityActor { USER, SYS }

enum class ActivityKind {
    // Required (plan004 §3.1):
    CHECK_IN, CHECK_OUT, ACTIVE, INACTIVE, ERROR,
    // Background suite (plan004 §3.2b/§3.4):
    TRIGGER, LOCATION, SYNC, AUTH, SYSTEM,
}

enum class ActivitySeverity { SUCCESS, FAILURE, WARNING, INFO }

data class ActivityLogEntry(
    val at: Instant,
    val actor: ActivityActor,
    val kind: ActivityKind,
    val severity: ActivitySeverity,
    val description: String,
    val location: String? = null,
)
