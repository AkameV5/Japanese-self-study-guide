package com.example.japanese_self_study_guide.main_profile

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.japanese_self_study_guide.R
import com.example.japanese_self_study_guide.audio.AudioActivity
import com.example.japanese_self_study_guide.grammar.GrammarActivity
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaActivity
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaActivity
import com.example.japanese_self_study_guide.kanji.KanjiActivity
import com.example.japanese_self_study_guide.texts_and_translation.TextsActivity
import com.example.japanese_self_study_guide.ui.theme.JapaneseSelfStudyGuideTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yalantis.ucrop.UCrop
import java.io.File

class ProfileActivity : ComponentActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    private val db    = FirebaseFirestore.getInstance()
    private val croppedUriState = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = mAuth.currentUser?.uid ?: run { finish(); return }

        val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val lang = prefs.getString("My_Lang", "ru") ?: "ru"

        setContent {
            JapaneseSelfStudyGuideTheme {
                ProfileScreen(
                    uid        = uid,
                    lang       = lang,
                    croppedUri = croppedUriState.value,
                    onPickImage = { launchImagePicker() },
                    onSave      = { username, uri -> saveProfile(uid, username, uri) },
                    onRingClick = { index -> navigateFromRing(index) },
                    onBack      = { finish() }
                )
            }
        }
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        @Suppress("DEPRECATION")
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), 1)
    }

    @Deprecated("Needed for UCrop")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data?.data != null) {
            val source = data.data!!
            val dest   = Uri.fromFile(File(cacheDir, "cropped_avatar.jpg"))
            UCrop.of(source, dest)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(512, 512)
                .withOptions(UCrop.Options().apply {
                    setCircleDimmedLayer(true)
                    setShowCropGrid(false)
                    setShowCropFrame(false)
                    setActiveControlsWidgetColor(resources.getColor(R.color.accentPink, theme))
                })
                .start(this)
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            croppedUriState.value = UCrop.getOutput(data!!)
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, getString(R.string.profile_crop_error, UCrop.getError(data!!)), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfile(uid: String, username: String, imageUri: Uri?) {
        val userRef = db.collection("Users").document(uid)
        fun saveUsername() {
            userRef.update("username", username)
                .addOnSuccessListener {
                    getSharedPreferences("LocalUser", MODE_PRIVATE).edit()
                        .putString("username", username).apply()
                    Toast.makeText(this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.profile_save_error, it.message), Toast.LENGTH_SHORT).show()
                }
        }
        if (imageUri != null) {
            FirestoreProfilePhoto.savePhoto(uid, imageUri, this)
                .addOnSuccessListener { saveUsername() }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.profile_photo_error, it.message), Toast.LENGTH_SHORT).show()
                }
        } else {
            saveUsername()
        }
    }

    private fun navigateFromRing(index: Int) {
        val intent = when (index) {
            0 -> Intent(this, HiraganaActivity::class.java)
            1 -> Intent(this, KatakanaActivity::class.java)
            2 -> Intent(this, KanjiActivity::class.java)
            3 -> Intent(this, GrammarActivity::class.java)
            4 -> Intent(this, TextsActivity::class.java)
            5 -> Intent(this, AudioActivity::class.java)
            else -> return
        }
        startActivity(intent)
    }
}

private val ringColors = listOf(
    Color(0xFFFF80AB), Color(0xFFFFB74D), Color(0xFF4FC3F7),
    Color(0xFF81C784), Color(0xFFBA68C8), Color(0xFFFF8A65)
)

private val barColors = listOf(
    Color(0xFFFF80AB), Color(0xFFFFB74D), Color(0xFF4FC3F7),
    Color(0xFF81C784), Color(0xFFBA68C8), Color(0xFFFF8A65)
)

private fun Modifier.safeClickable(onClick: () -> Unit): Modifier =
    this.pointerInput(onClick) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull()
                if (change != null && !change.pressed && change.previousPressed) onClick()
            }
        }
    }

@Composable
private fun ProgressRings(progress: FloatArray, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sz = minOf(size.width, size.height)
        val center = Offset(sz / 2f, sz / 2f)
        val strokeWidth = sz / 26f
        val ringStep = strokeWidth * 1.55f
        var radius = sz / 2f - strokeWidth
        for (i in 0 until 6) {
            if (radius < strokeWidth * 2.2f) break
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)
            val color = ringColors[i]
            drawArc(color.copy(alpha = 0.18f), -90f, 360f, false,
                topLeft, arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            val sweep = 360f * (progress.getOrElse(i) { 0f } / 100f)
            if (sweep > 0f)
                drawArc(color, -90f, sweep, false,
                    topLeft, arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            radius -= ringStep
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    uid: String,
    lang: String,
    croppedUri: Uri?,
    onPickImage: () -> Unit,
    onSave: (username: String, uri: Uri?) -> Unit,
    onRingClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var username     by remember { mutableStateOf("") }
    var avatarBase64 by remember { mutableStateOf<String?>(null) }
    var progress     by remember { mutableStateOf(FloatArray(6)) }
    var totals       by remember { mutableStateOf(IntArray(6) { 1 }) }
    var isSaving     by remember { mutableStateOf(false) }

    val ringLabels = if (lang == "ja") listOf("ひらがな", "カタカナ", "漢字", "文法", "テキスト", "音声")
    else              listOf(
        context.getString(R.string.hiragana_title),
        context.getString(R.string.katakana_title),
        context.getString(R.string.kanji_title),
        context.getString(R.string.grammar_title),
        context.getString(R.string.texts_title),
        context.getString(R.string.audio_title)
    )

    val progressKeys = listOf("hiraganaDone","katakanaDone","kanjiDone","grammarDone","textsDone","audioDone")
    val totalKeys    = listOf("hiraganaTotal","katakanaTotal","kanjiTotal","grammarTotal","textsTotal","audioTotal")

    LaunchedEffect(uid) {
        db.collection("Users").document(uid).get().addOnSuccessListener { doc ->
            username     = doc.getString("username") ?: ""
            avatarBase64 = doc.getString("profilePhoto")
        }
        ProgressManager.getProgressDoc(uid).addOnSuccessListener { progDoc ->
            TotalManager.loadTotals(context) { tot ->
                val p = FloatArray(6)
                val t = IntArray(6)
                for (i in 0..5) {
                    val done  = progDoc.getLong(progressKeys[i])?.toInt() ?: 0
                    val total = tot[totalKeys[i]] ?: 1
                    t[i] = total
                    p[i] = if (total > 0) done * 100f / total else 0f
                }
                progress = p
                totals   = t
            }
        }
    }

    val avatarBitmap = remember(croppedUri, avatarBase64) {
        croppedUri?.let {
            runCatching {
                context.contentResolver.openInputStream(it)?.use { s ->
                    BitmapFactory.decodeStream(s)
                }?.asImageBitmap()
            }.getOrNull()
        } ?: avatarBase64?.let {
            runCatching {
                val b = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
            }.getOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(pad)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .safeClickable { onPickImage() }
            ) {
                if (avatarBitmap != null) {
                    Image(bitmap = avatarBitmap, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Image(painter = painterResource(R.drawable.profile_user_def),
                        contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize())
                }
            }

            Text(
                text = stringResource(R.string.profile_pick_photo),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.username)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (username.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.profile_username_empty), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    onSave(username.trim(), croppedUri)
                },
                enabled  = !isSaving,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.profile_save))
                }
            }

            Spacer(Modifier.height(24.dp))

            ProgressRings(progress = progress, modifier = Modifier.size(280.dp))

            Spacer(Modifier.height(16.dp))

            ringLabels.forEachIndexed { i, label ->
                val done = if (totals[i] > 0) (progress[i] * totals[i] / 100).toInt() else 0
                Text(
                    text = "$label  $done / ${totals[i]}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .safeClickable { onRingClick(i) }
                )
                LinearProgressIndicator(
                    progress = { progress[i] / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(top = 4.dp)
                        .safeClickable { onRingClick(i) },
                    color      = barColors[i],
                    trackColor = barColors[i].copy(alpha = 0.2f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}