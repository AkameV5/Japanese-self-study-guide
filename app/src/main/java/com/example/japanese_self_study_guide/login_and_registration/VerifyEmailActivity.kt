package com.example.japanese_self_study_guide.login_and_registration

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.main_profile.MainActivity
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme
import com.google.firebase.auth.FirebaseAuth

class VerifyEmailActivity : ComponentActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var checkRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        setContent {
            JapaneseSelfStudyGuideTheme {
                VerifyEmailScreen(
                    userEmail     = userEmail,
                    onResend      = { resendVerificationEmail() },
                    onBackToLogin = {
                        mAuth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }

        startPolling()
    }

    private fun startPolling() {
        checkRunnable = object : Runnable {
            override fun run() {
                val user = mAuth.currentUser ?: return
                user.reload().addOnCompleteListener {
                    if (user.isEmailVerified) {
                        startActivity(Intent(this@VerifyEmailActivity, MainActivity::class.java))
                        finish()
                    } else {
                        handler.postDelayed(this, 3000)
                    }
                }
            }
        }
        handler.post(checkRunnable!!)
    }

    private fun resendVerificationEmail() {
        mAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            val msgRes = if (task.isSuccessful) R.string.resend_success else R.string.resend_error
            android.widget.Toast.makeText(this, getString(msgRes), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        checkRunnable?.let { handler.removeCallbacks(it) }
    }
}

@Composable
private fun VerifyEmailScreen(
    userEmail: String,
    onResend: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector     = Icons.Default.Email,
                    contentDescription = null,
                    tint            = MaterialTheme.colorScheme.primary,
                    modifier        = Modifier.size(80.dp).padding(bottom = 16.dp)
                )

                Text(
                    text       = stringResource(R.string.verify_title),
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = if (userEmail.isNotEmpty())
                        stringResource(R.string.verify_email_message, userEmail)
                    else
                        stringResource(R.string.verify_message),
                    fontSize   = 16.sp,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    modifier   = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick  = onResend,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.resend_email))
                }

                TextButton(onClick = onBackToLogin) {
                    Text(
                        text      = stringResource(R.string.back_to_login),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}