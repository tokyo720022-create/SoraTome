package io.github.tokyo720022create.soratome.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tokyo720022create.soratome.R
import io.github.tokyo720022create.soratome.ui.findActivity

private const val APP_NAME = "SoraTome"
private const val TAGLINE = "Your manga, remembered."
private const val CAPTION = "Library, reader and tracking"

private val GradientTop = Color(0xFF2B1B47)
private val GradientMiddle = Color(0xFF120B22)
private val GradientBottom = Color(0xFF07050C)
private val Glow = Color(0x668C7BFF)

/**
 * The screen shown while the app starts: dark gradient, the app icon with a soft glow,
 * the name, a short tagline and a thin progress bar.
 */
@Composable
fun SplashScreen() {
    // Colour the status bar to match while the splash is showing.
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val window = activity?.window
        val previous = window?.statusBarColor
        window?.statusBarColor = GradientTop.toArgb()
        onDispose {
            if (window != null && previous != null) window.statusBarColor = previous
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientTop, GradientMiddle, GradientBottom))),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        val radius = size.minDimension * 0.95f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Glow, Color.Transparent),
                                center = center,
                                radius = radius,
                            ),
                            radius = radius,
                            center = center,
                        )
                    },
            ) {
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = APP_NAME,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = TAGLINE,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(32.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(180.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.12f),
            )
        }
        Text(
            text = CAPTION,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
        )
    }
}
