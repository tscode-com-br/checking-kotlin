package br.com.tscode.checking

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

// Custom instrumentation runner that swaps the real Application for HiltTestApplication,
// allowing @HiltAndroidTest instrumented tests to inject dependencies.
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
