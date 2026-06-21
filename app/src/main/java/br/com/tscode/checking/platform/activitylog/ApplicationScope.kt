package br.com.tscode.checking.platform.activitylog

import javax.inject.Qualifier

/**
 * plan004 §3.3 — qualifier for the application-lifetime [kotlinx.coroutines.CoroutineScope] used by
 * [ActivityLogger] to persist log entries off the caller's thread (so a check-in / FGS / receiver is
 * never blocked or broken by logging). Provided as a singleton (SupervisorJob + Dispatchers.IO).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
