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
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            JapaneseSelfStudyGuideTheme {
                RegistrationScreen(
                    onGoToVerify = { email ->
                        val intent = Intent(this, VerifyEmailActivity::class.java)
                        intent.putExtra("USER_EMAIL", email)
                        startActivity(intent)
                        finish()
                    },
                    onGoToLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    onToggleLanguage = {
                        val current = resources.configuration.locale.language
                        setLocale(if (current == "ru") "ja" else "ru")
                    }
                )
            }
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
private fun RegistrationScreen(
    onGoToVerify: (email: String) -> Unit,
    onGoToLogin: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    val context = LocalContext.current
    val mAuth = remember { FirebaseAuth.getInstance() }
    val db    = remember { FirebaseFirestore.getInstance() }

    var username  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
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
                    text       = stringResource(R.string.register),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value         = username,
                    onValueChange = { username = it },
                    label         = { Text(stringResource(R.string.username)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it },
                    label           = { Text(stringResource(R.string.email)) },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier        = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it },
                    label                = { Text(stringResource(R.string.password)) },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier             = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                Button(
                    onClick = {
                        if (username.isBlank() || email.isBlank() || password.isBlank()) {
                            android.widget.Toast.makeText(context,
                                context.getString(R.string.fill_all_fields),
                                android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val user = mAuth.currentUser
                                    if (user != null) {
                                        saveUserToFirestore(db, user, username.trim(), password.trim())
                                        user.sendEmailVerification()
                                            .addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    android.widget.Toast.makeText(context,
                                                        context.getString(R.string.verification_sent),
                                                        android.widget.Toast.LENGTH_LONG).show()
                                                    onGoToVerify(email.trim())
                                                } else {
                                                    android.widget.Toast.makeText(context,
                                                        context.getString(R.string.send_email_error),
                                                        android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                } else {
                                    android.widget.Toast.makeText(context,
                                        context.getString(R.string.register_error, task.exception?.message),
                                        android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    enabled  = !isLoading,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.register))
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                TextButton(onClick = onGoToLogin) {
                    Text(
                        text      = stringResource(R.string.have_account),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun saveUserToFirestore(
    db: FirebaseFirestore,
    user: FirebaseUser,
    username: String,
    password: String
) {
    val registrationDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val userData = mapOf(
        "username"         to username,
        "email"            to user.email,
        "registrationDate" to registrationDate,
        "password"         to hashPassword(password)
    )
    db.collection("Users").document(user.uid).set(userData)
}

private fun hashPassword(password: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash   = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        hash.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        "HASH_ERROR"
    }
}