package com.example.japanese_self_study_guide.login_and_registration

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.main_profile.MainActivity
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class LoginActivity : ComponentActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            JapaneseSelfStudyGuideTheme {
                LoginScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onGoToRegister = {
                        startActivity(Intent(this, RegistrationActivity::class.java))
                        finish()
                    },
                    onGoToVerify = { email ->
                        val intent = Intent(this, VerifyEmailActivity::class.java)
                        intent.putExtra("USER_EMAIL", email)
                        startActivity(intent)
                    },
                    onToggleLanguage = {
                        val current = resources.configuration.locale.language
                        setLocale(if (current == "ru") "ja" else "ru")
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        if (user != null && user.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        getSharedPreferences("Settings", MODE_PRIVATE).edit()
            .putString("My_Lang", langCode).apply()
        recreate()
    }

    private fun loadLocale() {
        val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "ru") ?: "ru"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

@Composable
private fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToVerify: (email: String) -> Unit,
    onToggleLanguage: () -> Unit
) {
    val context = LocalContext.current
    val mAuth = remember { FirebaseAuth.getInstance() }

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        TextButton(
            onClick  = onToggleLanguage,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Text(stringResource(R.string.lang_button), color = MaterialTheme.colorScheme.primary)
        }

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
                Text(
                    text       = stringResource(R.string.login),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text(stringResource(R.string.email)) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier      = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value               = password,
                    onValueChange       = { password = it },
                    label               = { Text(stringResource(R.string.password)) },
                    singleLine          = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions     = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier            = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            android.widget.Toast.makeText(context,
                                context.getString(R.string.fill_all_fields),
                                android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        mAuth.signInWithEmailAndPassword(email.trim(), password.trim())
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val user = mAuth.currentUser
                                    if (user != null) {
                                        if (user.isEmailVerified) {
                                            onLoginSuccess()
                                        } else {
                                            mAuth.signOut()
                                            android.widget.Toast.makeText(context,
                                                context.getString(R.string.email_verification),
                                                android.widget.Toast.LENGTH_LONG).show()
                                            onGoToVerify(email.trim())
                                        }
                                    }
                                } else {
                                    android.widget.Toast.makeText(context,
                                        context.getString(R.string.login_error, task.exception?.message),
                                        android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    enabled  = !isLoading,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.login))
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                TextButton(onClick = onGoToRegister) {
                    Text(
                        text       = stringResource(R.string.no_account),
                        fontStyle  = FontStyle.Italic,
                        textAlign  = TextAlign.Center,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}