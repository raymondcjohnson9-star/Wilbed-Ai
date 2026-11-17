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
            if (status == TextToSpeech.SUCCESS) textToSpeech?.language = Locale.getDefault()
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
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
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
    var status by rememberSaveable { mutableStateOf("SYSTEM ONLINE") }
    var result by rememberSaveable { mutableStateOf("ATLAS is ready. Tap the microphone or enter a command.") }
    var activeSection by rememberSaveable { mutableStateOf("HOME") }
    val listening by remember { derivedStateOf { activity.isListening } }
    val atlas = remember { WilbedAI(activity) }

    fun execute(raw: String) {
        val clean = raw.trim()
        if (clean.isBlank()) return
        status = "PROCESSING REQUEST"
        val answer = atlas.respond(clean)
        result = answer
        status = "SYSTEM ONLINE"
        command = ""
        activity.speak(answer)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) activity.startVoiceInput { execute(it) }
        else status = "MICROPHONE PERMISSION DENIED"
    }

    fun activateMicrophone() {
        if (listening) {
            activity.stopListening()
        } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            activity.startVoiceInput { execute(it) }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF101D36), Night, Color(0xFF080D1B)))
        )
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Cyan, Violet))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "ATLAS", tint = Color.White, modifier = Modifier.size(27.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("ATLAS", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 2.5.sp)
                    Text("INTELLIGENT COMMAND CENTRE", color = Muted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                }
                IconButton(onClick = { activeSection = "SETTINGS" }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Cyan)
                }
            }

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                    .background(Color(0xAA101A2D))
                    .border(1.dp, Color(0x663D6685), RoundedCornerShape(15.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(if (listening) Violet else Green))
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (listening) "VOICE INPUT ACTIVE" else status, color = if (listening) Violet else Green, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(if (listening) "ATLAS is listening for your command" else "Local assistant interface ready", color = Muted, fontSize = 10.sp)
                }
                Text("ONLINE", color = Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            // Central assistant identity: a command hub, not a conversation screen.
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF102B4B), Color(0xFF081426))))
                    .border(1.dp, Color(0xFF176B9C), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(156.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF1D79B9), Color(0xFF101D3D), Night)))
                        .border(2.dp, Cyan.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(124.dp).border(1.dp, Violet.copy(alpha = 0.7f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "ATLAS assistant", tint = Cyan, modifier = Modifier.size(66.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("YOUR INTELLIGENT ASSISTANT", color = SoftWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                Text("Think  •  Plan  •  Analyse  •  Execute", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = { activateMicrophone() },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (listening) Violet else Color(0xFF0D537B), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(if (listening) Icons.Default.StopCircle else Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.width(9.dp))
                    Text(if (listening) "STOP LISTENING" else "ACTIVATE VOICE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("Voice activation is user-controlled in this version.", color = Muted, fontSize = 10.sp)
            }

            SectionTitle("COMMAND CONSOLE", "Issue a task without opening a chat.")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Enter a command...", color = Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite,
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0xFF344C6B),
                        cursorColor = Cyan
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { execute(command) }, enabled = command.isNotBlank()) {
                    Text("RUN")
                }
            }

            SectionTitle("SMART TOOLS", "Specialised systems available in the ATLAS workspace.")
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ToolTile("Calculations", "Arithmetic engine", Icons.Default.Calculate, Modifier.weight(1f)) { command = "Calculate 125 * 48" }
                    ToolTile("Documents", "Reading workspace", Icons.Default.Description, Modifier.weight(1f)) { activeSection = "DOCUMENTS"; result = "Document reading tools are planned for the next development stage." }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ToolTile("Research", "Knowledge engine", Icons.Default.Search, Modifier.weight(1f)) { activeSection = "RESEARCH"; result = "Research integration is planned. ATLAS will distinguish verified sources from assumptions." }
                    ToolTile("Planning", "Tasks and decisions", Icons.Default.TaskAlt, Modifier.weight(1f)) { activeSection = "PLANNING"; result = "Planning and multi-step task execution are being developed." }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ToolTile("Files", "Organisation", Icons.Default.FolderOpen, Modifier.weight(1f)) { activeSection = "FILES"; result = "File organisation will require your permission before changes are made." }
                    ToolTile("Recommendations", "Decision support", Icons.Default.Lightbulb, Modifier.weight(1f)) { activeSection = "RECOMMENDATIONS"; result = "Decision support will compare options, explain trade-offs and state uncertainty." }
                }
            }

            SectionTitle("INTELLIGENCE SYSTEMS", "Modular engines, developed and tested independently.")
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Panel).border(1.dp, Color(0xFF263D5A), RoundedCornerShape(18.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                EngineStatus("Reasoning engine", "Foundation planned", false)
                EngineStatus("Mathematics engine", "Local arithmetic available", true)
                EngineStatus("Language model", "Independent training planned", false)
                EngineStatus("Memory engine", "Local memory foundation", true)
                EngineStatus("Voice engine", "Tap-to-activate voice", true)
                EngineStatus("Phone operations", "Permission integrations planned", false)
            }

            SectionTitle("LATEST RESULT", "ATLAS task output")
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Panel).border(1.dp, Color(0xFF263D5A), RoundedCornerShape(18.dp)).padding(15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("TASK REPORT", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { activity.speak(result) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Read result aloud", tint = Cyan)
                    }
                }
                Text(result, color = SoftWhite, fontSize = 13.sp, lineHeight = 19.sp)
            }

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF0B1425))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("HOME", "TOOLS", "KNOWLEDGE", "SETTINGS").forEach { section ->
                    Text(
                        section,
                        color = if (activeSection == section) Cyan else Muted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { activeSection = section }.padding(5.dp)
                    )
                }
            }
            Text("ATLAS  •  LOCAL-FIRST INTELLIGENCE  •  USER CONTROLLED", color = Muted, fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
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
