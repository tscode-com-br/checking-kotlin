package br.com.tscode.checking.platform.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

// Content-type sent to the server for MP4 output.
const val VIDEO_CONTENT_TYPE = "video/mp4"

@Singleton
class VideoRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var activeRecording: Recording? = null

    // Binds a camera preview to the given PreviewView and returns the VideoCapture use-case.
    // The caller owns the lifecycle; call this from a @Composable DisposableEffect or LaunchedEffect.
    suspend fun bindPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ): VideoCapture<Recorder> = suspendCancellableCoroutine { cont ->
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = try {
                providerFuture.get()
            } catch (e: Exception) {
                cont.resumeWithException(e)
                return@addListener
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            val videoCapture = VideoCapture.withOutput(recorder)

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    videoCapture,
                )
                cont.resume(videoCapture)
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))

        cont.invokeOnCancellation {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
        }
    }

    // Starts recording to a temp MP4 file. Returns the output File immediately;
    // recording continues until stopRecording() is called.
    fun startRecording(videoCapture: VideoCapture<Recorder>, outputFile: File): File {
        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        activeRecording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                // Events handled by the caller via the returned File; we only need to track the
                // finalize event to clean up internal state.
                if (event is VideoRecordEvent.Finalize) {
                    activeRecording = null
                }
            }
        return outputFile
    }

    // Stops the active recording. The output file is ready for upload after this returns.
    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    // Creates a fresh temp file in the app's cache dir for a new recording.
    fun createTempFile(): File =
        File.createTempFile("accident_video_", ".mp4", context.cacheDir)

    fun isRecording(): Boolean = activeRecording != null
}
