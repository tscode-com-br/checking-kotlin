package br.com.tscode.checking.platform.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// Receives ENTER/EXIT geofence transitions from Play Services via the PendingIntent registered
// in GeofenceManager (§23.3-2, T3B.9).  Wakes the orchestrator so it can run the 7-step
// check engine without waiting for the next 15-min FGS tick.
@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var orchestrator: BackgroundCheckOrchestrator
    @Inject lateinit var activityLogger: ActivityLogger

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val transition = event.geofenceTransition
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            transition != Geofence.GEOFENCE_TRANSITION_EXIT) return

        // plan004 — geofence crossing woke the engine. Logged best-effort off the application scope (same
        // fire-and-forget semantics as every ActivityLogger call); location=null since matching is server-side.
        activityLogger.logLocation(
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) "Entered geofence." else "Exited geofence.",
        )

        // Ensure the FGS is alive so subsequent timer ticks keep running.
        if (!AutoActivityForegroundService.isRunning) {
            AutoActivityController.start(context)
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                orchestrator.runOnce(OrchestratorTrigger.GEOFENCE)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
