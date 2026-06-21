package br.com.tscode.checking.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tscode.checking.BuildConfig
import br.com.tscode.checking.presentation.theme.ArimoFamily
import br.com.tscode.checking.presentation.theme.CheckingHeaderBg
import kotlinx.coroutines.delay
import kotlin.math.min

// Splash: the Checking logo on a teal background, with a single animation —
// the checkmark "V" drawn progressively as if by hand. Nothing else animates.
// Coordinates mirror the admin logo SVG (viewBox 0 0 220 170): an outer
// translate(18,16) and an inner rotate(-12) about pivot (74,70).
@Composable
fun AppSplashScreen(onFinished: () -> Unit) {
    // Logo geometry (parsed once — fixed viewport coordinates).
    val card = remember {
        PathParser().parsePathString(
            "M30,8 H114 A18,18 0 0,1 132,26 V110 A18,18 0 0,1 114,128 H30 A18,18 0 0,1 12,110 V26 A18,18 0 0,1 30,8 Z",
        ).toPath()
    }
    val photo = remember {
        PathParser().parsePathString(
            "M32,30 H58 A4,4 0 0,1 62,34 V54 A4,4 0 0,1 58,58 H32 A4,4 0 0,1 28,54 V34 A4,4 0 0,1 32,30 Z",
        ).toPath()
    }
    val nameLine = remember { PathParser().parsePathString("M28,74 H64").toPath() }
    val roleLine = remember { PathParser().parsePathString("M28,96 H56").toPath() }
    val arc1 = remember { PathParser().parsePathString("M154,30 C167,34 176,43 180,56").toPath() }
    val arc2 = remember { PathParser().parsePathString("M166,18 C184,24 197,38 202,56").toPath() }

    // Checkmark — the only animated stroke. Drawn from (92,78) down to the
    // vertex (118,104) and up to (162,44), exactly how a hand writes a check.
    val checkmark = remember { PathParser().parsePathString("M92,78 L118,104 L162,44").toPath() }
    val checkMeasure = remember { PathMeasure().apply { setPath(checkmark, false) } }
    val checkLength = remember { checkMeasure.length }
    val drawnCheck = remember { Path() }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing))
        delay(450)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CheckingHeaderBg)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(
                modifier = Modifier.size(width = 300.dp, height = 232.dp),
            ) {
            val s = min(size.width / 220f, size.height / 170f)
            val offsetX = (size.width - 220f * s) / 2f
            val offsetY = (size.height - 170f * s) / 2f
            val white = Color.White

            translate(left = offsetX, top = offsetY) {
                scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
                    translate(left = 18f, top = 16f) {
                        rotate(degrees = -12f, pivot = Offset(74f, 70f)) {
                            // Static logo elements
                            drawPath(card, white, style = Stroke(width = 10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawPath(photo, white, style = Fill)
                            drawPath(nameLine, white, style = Stroke(width = 10f, cap = StrokeCap.Round))
                            drawPath(roleLine, white, style = Stroke(width = 10f, cap = StrokeCap.Round))
                            drawPath(arc1, white, style = Stroke(width = 8f, cap = StrokeCap.Round))
                            drawPath(arc2, white, style = Stroke(width = 8f, cap = StrokeCap.Round))

                            // Hand-drawn checkmark: reveal the stroke up to the animated length.
                            drawnCheck.reset()
                            checkMeasure.getSegment(0f, checkLength * progress.value, drawnCheck, true)
                            drawPath(drawnCheck, white, style = Stroke(width = 20f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
            }
            }
            // V1.6.2 — app version shown right below the animated logo; tracks BuildConfig.VERSION_NAME.
            Text(
                text = BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = ArimoFamily,
                    letterSpacing = 1.sp,
                ),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        val footerStyle = MaterialTheme.typography.labelSmall.copy(
            fontFamily = ArimoFamily,
            fontSize = 13.75.sp, // labelSmall 11sp + 25%
            letterSpacing = 1.5.sp,
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = "Dilnei Schmidt", style = footerStyle, color = Color.White, textAlign = TextAlign.Center)
            Text(text = "Tamer Salmem", style = footerStyle, color = Color.White, textAlign = TextAlign.Center)
            Text(text = "Thiago Soares do Nascimento", style = footerStyle, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}
