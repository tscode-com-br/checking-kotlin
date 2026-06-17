package br.com.tscode.checking.presentation.accident

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import br.com.tscode.checking.i18n.TranslateFunction
import br.com.tscode.checking.platform.camera.VIDEO_CONTENT_TYPE
import br.com.tscode.checking.platform.camera.VideoRecorder
import br.com.tscode.checking.presentation.components.PrimaryButton
import br.com.tscode.checking.presentation.theme.CheckingError
import br.com.tscode.checking.presentation.theme.CheckingOnPrimary
import br.com.tscode.checking.presentation.theme.Tokens
import java.io.File
import kotlinx.coroutines.launch

enum class VideoRecordPhase { RECORDING, UPLOADING, DONE, ERROR }

data class VideoRecordState(
    val phase: VideoRecordPhase = VideoRecordPhase.RECORDING,
    val uploadProgress: Float = 0f,
    val statusMessage: String = "",
)

@Composable
fun VideoRecordScreen(
    recorder: VideoRecorder,
    // Receives the completed MP4 file + content-type + progress callback.
    onUpload: suspend (file: File, contentType: String, onProgress: (Float) -> Unit) -> Unit,
    onDone: () -> Unit,
    t: TranslateFunction,
) {
    BackHandler { onDone() }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Camera + microphone are requested HERE, at the moment of recording (not pre-granted in
    // Settings). The preview/recording only binds once both are granted.
    fun hasCameraAndMic(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    var permissionsGranted by remember { mutableStateOf(hasCameraAndMic()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        permissionsGranted = result[Manifest.permission.CAMERA] == true &&
            result[Manifest.permission.RECORD_AUDIO] == true
    }
    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            )
        }
    }

    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var state by remember { mutableStateOf(VideoRecordState()) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            if (recorder.isRecording()) recorder.stopRecording()
        }
    }

    LaunchedEffect(previewView, permissionsGranted) {
        if (!permissionsGranted) return@LaunchedEffect
        val pv = previewView ?: return@LaunchedEffect
        val vc = runCatching { recorder.bindPreview(lifecycleOwner, pv) }.getOrElse { return@LaunchedEffect }
        videoCapture = vc
        val tempFile = recorder.createTempFile()
        recordedFile = recorder.startRecording(vc, tempFile)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding(),
    ) {
        // Camera UI binds only after camera+mic are granted; otherwise show the rationale.
        if (permissionsGranted) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { pv ->
                    pv.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewView = pv
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(Tokens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.phase) {
                VideoRecordPhase.RECORDING -> {
                    Text(
                        text = "⏺ Gravando…",
                        style = MaterialTheme.typography.labelLarge,
                        color = CheckingError,
                    )
                    PrimaryButton(
                        text = t("accident.actions.back", null),
                        onClick = {
                            recorder.stopRecording()
                            val file = recordedFile
                            if (file == null) {
                                state = state.copy(
                                    phase = VideoRecordPhase.ERROR,
                                    statusMessage = t("accident.video.error", null),
                                )
                                return@PrimaryButton
                            }
                            state = state.copy(
                                phase = VideoRecordPhase.UPLOADING,
                                statusMessage = t("accident.video.sending", null),
                            )
                            scope.launch {
                                runCatching {
                                    onUpload(file, VIDEO_CONTENT_TYPE) { progress ->
                                        state = state.copy(uploadProgress = progress)
                                    }
                                    state = state.copy(
                                        phase = VideoRecordPhase.DONE,
                                        statusMessage = t("accident.video.sent", null),
                                    )
                                }.onFailure {
                                    state = state.copy(
                                        phase = VideoRecordPhase.ERROR,
                                        statusMessage = t("accident.video.error", null),
                                    )
                                }
                            }
                        },
                        enabled = true,
                    )
                }

                VideoRecordPhase.UPLOADING -> {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CheckingOnPrimary,
                    )
                    LinearProgressIndicator(
                        progress = { state.uploadProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                VideoRecordPhase.DONE -> {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CheckingOnPrimary,
                    )
                    PrimaryButton(
                        text = t("accident.actions.back", null),
                        onClick = onDone,
                        enabled = true,
                    )
                }

                VideoRecordPhase.ERROR -> {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CheckingError,
                    )
                    PrimaryButton(
                        text = t("accident.actions.back", null),
                        onClick = onDone,
                        enabled = true,
                    )
                }
            }
        }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(Tokens.sectionGap),
                verticalArrangement = Arrangement.spacedBy(Tokens.itemGap),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = t("accident.video.permissionRequired", null),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CheckingOnPrimary,
                )
                PrimaryButton(text = t("accident.actions.back", null), onClick = onDone)
            }
        }
    }
}
