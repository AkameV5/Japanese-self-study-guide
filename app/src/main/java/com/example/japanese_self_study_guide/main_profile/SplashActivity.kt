package com.example.japanese_self_study_guide.main_profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.login_and_registration.LoginActivity
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JapaneseSelfStudyGuideTheme {
                SplashScreen(
                    onFinished = {
                        val user = FirebaseAuth.getInstance().currentUser
                        val dest = if (user != null && user.isEmailVerified)
                            Intent(this, MainActivity::class.java)
                        else
                            Intent(this, LoginActivity::class.java)
                        dest.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(dest)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        delay(800)
        alpha.animateTo(
            targetValue   = 0f,
            animationSpec = tween(durationMillis = 500)
        )
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE91E63)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter            = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(280.dp)
                .alpha(alpha.value),
            contentScale = ContentScale.Fit
        )
    }
}