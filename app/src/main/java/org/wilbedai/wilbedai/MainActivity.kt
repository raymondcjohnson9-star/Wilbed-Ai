package org.wilbedai.wilbedai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

private val Night = Color(0xFF050914)
private val Panel = Color(0xFF101A2D)
private val Cyan = Color(0xFF42E8FF)
private val Violet = Color(0xFF9B75FF)
private val SoftWhite = Color(0xFFEAF4FF)
private val Muted = Color(0xFF91A4C2)
private val Green = Color(0xFF54E6B0)

class MainActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechCallback: ((String) -> Unit)? = null
    private var textToSpeech: TextToSpeech? = null
    var isListening by mutableStateOf(false)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) textToSpeech?.language = Locale.US
        }
        setContent { AtlasTheme { AtlasDashboard(this) } }
    }

    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "atlas_reply")
    }

    fun stopSpeaking() { textToSpeech?.stop() }

    fun stopListening() {
        speechRecognizer?.cancel()
        isListening = false
    }

    fun startVoiceInput(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            isListening = false
            return
        }
        speechCallback = onResult
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).also { recognizer ->
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() { isListening = true }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) { isListening = false }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!spoken.isNullOrBlank()) speechCallback?.invoke(spoken)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.US.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to ATLAS")
            }
            recognizer.startListening(intent)
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }
}

@Composable
private fun AtlasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Cyan,
            secondary = Violet,
            background = Night,
            surface = Panel,
            onBackground = SoftWhite,
            onSurface = SoftWhite
        ),
        content = content
    )
}

@Composable
private fun AtlasDashboard(activity: MainActivity) {
    var command by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("READY") }
    var result by rememberSaveable { mutableStateOf("ATLAS is ready. Tap the central control and speak a command.") }
    val listening by remember { derivedStateOf { activity.isListening } }
    val atlas = remember { WilbedAI(activity) }

    fun execute(raw: String) {
        val clean = raw.trim()
        if (clean.isBlank()) return
        status = "PROCESSING"
        val answer = atlas.respond(clean)
        result = answer
        status = "READY"
        command = ""
        activity.speak(answer)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) activity.startVoiceInput { execute(it) }
        else status = "MIC PERMISSION NEEDED"
    }

    fun activateMicrophone() {
        if (listening) {
            activity.stopListening()
            status = "READY"
        } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            status = "LISTENING"
            activity.startVoiceInput { execute(it) }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val pulse = androidx.compose.animation.core.rememberInfiniteTransition(label = "atlas-pulse")
    val glow by pulse.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1250),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "atlas-glow"
    )

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF071A31), Night, Color(0xFF090B1C)))
        )
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            // Compact header: settings are available, but there is no bottom navigation.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(43.dp).clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Cyan, Violet))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("ATLAS", color = SoftWhite, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("PERSONAL INTELLIGENCE SYSTEM", color = Muted, fontSize = 8.sp, letterSpacing = 1.2.sp)
                }
                IconButton(onClick = {
                    result = "Settings and permissions will be managed here. Microphone access remains under your control."
                    status = "SYSTEM SETTINGS"
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Cyan)
                }
            }

            // One-line system state.
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp))
                    .background(Color(0xCC10233B))
                    .border(1.dp, Color(0x6642E8FF), RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(if (listening) Violet else Green))
                Spacer(Modifier.width(9.dp))
                Text(
                    when { listening -> "LISTENING"; status == "PROCESSING" -> "THINKING"; else -> status },
                    color = if (listening) Violet else Green,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.weight(1f))
                Text("LOCAL-FIRST", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
            }

            // Animated assistant identity. The pulsing rings are lightweight Compose shapes.
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF102B4B), Color(0xFF080F20))))
                    .border(1.dp, Color(0xFF176B9C), RoundedCornerShape(28.dp))
                    .padding(vertical = 22.dp, horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(218.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(206.dp * glow).border(1.dp, Cyan.copy(alpha = 0.30f), CircleShape)
                    )
                    Box(
                        Modifier.size(174.dp).border(2.dp, Violet.copy(alpha = 0.55f), CircleShape)
                    )
                    Box(
                        Modifier.size(145.dp).clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF236EAA), Color(0xFF101D3D), Night)))
                            .border(2.dp, Cyan.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "ATLAS animated assistant",
                            tint = Cyan,
                            modifier = Modifier.size(66.dp)
                        )
                    }
                }
                Text(
                    when { listening -> "I'M LISTENING"; status == "PROCESSING" -> "THINKING"; else -> "ATLAS IS READY" },
                    color = SoftWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp
                )
                Spacer(Modifier.height(5.dp))
                Text("Your voice-controlled assistant", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(19.dp))
                Button(
                    onClick = { activateMicrophone() },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (listening) Color(0xFF633C9C) else Color(0xFF087FA4),
                        contentColor = Color.White
                    )
                ) {
                    Icon(if (listening) Icons.Default.StopCircle else Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(if (listening) "STOP LISTENING" else "TAP TO SPEAK", fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("Microphone starts only when you activate it.", color = Muted, fontSize = 9.sp)
            }

            // Task console replaces chat bubbles.
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Panel)
                    .border(1.dp, Color(0xFF245071), RoundedCornerShape(20.dp))
                    .padding(15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Cyan, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("TASK REPORT", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.2.sp)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { activity.speak(result) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Read task report aloud", tint = Cyan)
                    }
                }
                Text(result, color = SoftWhite, fontSize = 13.sp, lineHeight = 19.sp)
            }

            // Optional typed command field, visually secondary to voice control.
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Or type a command…", color = Muted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftWhite,
                    unfocusedTextColor = SoftWhite,
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Color(0xFF344C6B),
                    cursorColor = Cyan
                ),
                trailingIcon = {
                    IconButton(onClick = { execute(command) }, enabled = command.isNotBlank()) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Run command", tint = Cyan)
                    }
                }
            )

            Text("QUICK SYSTEMS", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolTile("Calculator", "Local maths", Icons.Default.Calculate, Modifier.weight(1f)) {
                    execute("Calculate 125 * 48")
                }
                ToolTile("Planning", "Task support", Icons.Default.TaskAlt, Modifier.weight(1f)) {
                    result = "Planning tools are being developed. ATLAS will show steps and ask before consequential actions."
                }
                ToolTile("Knowledge", "Smart answers", Icons.Default.Lightbulb, Modifier.weight(1f)) {
                    result = "ATLAS can currently answer supported offline requests. Broader knowledge features are planned."
                }
            }

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF0B1425)).padding(13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("VOICE", color = Muted, fontSize = 9.sp)
                    Text("READY", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("MATH ENGINE", color = Muted, fontSize = 9.sp)
                    Text("AVAILABLE", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("MEMORY", color = Muted, fontSize = 9.sp)
                    Text("LOCAL", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "ATLAS  •  USER CONTROLLED  •  BUILDING INTELLIGENCE",
                color = Muted,
                fontSize = 8.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
        Text(subtitle, color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun ToolTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(Panel)
            .border(1.dp, Color(0xFF263D5A), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(13.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Icon(icon, contentDescription = null, tint = Cyan, modifier = Modifier.size(25.dp))
        Spacer(Modifier.height(9.dp))
        Text(title, color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(subtitle, color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun EngineStatus(name: String, detail: String, ready: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (ready) Green else Violet))
        Spacer(Modifier.width(9.dp))
        Text(name, color = SoftWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(detail, color = if (ready) Green else Muted, fontSize = 9.sp)
    }
}
