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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF050914)
private val Panel = Color(0xFF101A2D)
private val PanelLight = Color(0xFF172640)
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
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            }
        }
        setContent { WilbedTheme { WilbedApp(this) } }
    }

    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wilbed_reply")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

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
                    val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) speechCallback?.invoke(spokenText)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Wilbed")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
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
private fun WilbedTheme(content: @Composable () -> Unit) {
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

private data class Message(val sender: String, val text: String, val time: String)

private fun timestamp(): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

@Composable
private fun WilbedApp(activity: MainActivity) {
    var draft by rememberSaveable { mutableStateOf("") }
    var showTools by rememberSaveable { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf("SYSTEM ONLINE") }
    val brain = remember { WilbedBrain() }
    val messages = remember {
        mutableStateListOf(
            Message(
                "WILBED",
                "Welcome to Wilbed AI.\n\nI'm ready to help you learn, build and explore. Ask me to teach you a programming language, explain a concept, review code or give you a challenge.",
                timestamp()
            )
        )
    }
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val listening by remember { derivedStateOf { activity.isListening } }

    fun sendMessage(messageText: String) {
        val cleanText = messageText.trim()
        if (cleanText.isNotEmpty()) {
            messages.add(Message("YOU", cleanText, timestamp()))
            status = "PROCESSING REQUEST"
            val reply = brain.respond(cleanText)
            messages.add(Message("WILBED", reply, timestamp()))
            activity.speak(reply)
            status = "SYSTEM ONLINE"
            draft = ""
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            activity.startVoiceInput { spoken -> sendMessage(spoken) }
        } else {
            status = "MICROPHONE PERMISSION DENIED"
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF101D36), Night, Color(0xFF080D1B)))
        )
    ) {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                // Futuristic identity header and conversation controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(
                            Brush.linearGradient(listOf(Cyan, Violet))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Wilbed AI", tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("WILBED", color = SoftWhite, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 2.4.sp)
                        Text("PERSONAL AI  /  NEURAL INTERFACE", color = Muted, fontSize = 8.sp, letterSpacing = 1.sp)
                    }
                    IconButton(onClick = {
                        activity.stopSpeaking()
                        activity.stopListening()
                        messages.clear()
                        messages.add(Message("WILBED", "New conversation started. What shall we work on?", timestamp()))
                        status = "SYSTEM ONLINE"
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "New conversation", tint = Cyan)
                    }
                }

                Spacer(Modifier.height(13.dp))
                Surface(
                    color = Color(0xAA101A2D),
                    shape = RoundedCornerShape(15.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x663D6685)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(if (listening) Violet else Green))
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (listening) "LISTENING TO YOUR VOICE" else status, color = if (listening) Violet else Green, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(if (listening) "Speak clearly. Wilbed is listening." else "Chat ready  •  Voice input available", color = Muted, fontSize = 9.sp)
                        }
                        Icon(Icons.Default.GraphicEq, contentDescription = "Audio system", tint = Cyan, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(13.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(13.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp)
                ) {
                    items(messages) { message ->
                        val mine = message.sender == "YOU"
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = if (mine) Alignment.End else Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!mine) {
                                    Box(
                                        Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                    Spacer(Modifier.width(7.dp))
                                }
                                Text(if (mine) "YOU" else "WILBED AI", color = if (mine) Violet else Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                                Spacer(Modifier.width(7.dp))
                                Text(message.time, color = Muted, fontSize = 8.sp)
                            }
                            Spacer(Modifier.height(5.dp))
                            Surface(
                                color = if (mine) Color(0xFF282449) else Panel,
                                shape = RoundedCornerShape(
                                    topStart = 17.dp, topEnd = 17.dp,
                                    bottomEnd = if (mine) 5.dp else 17.dp,
                                    bottomStart = if (mine) 17.dp else 5.dp
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (mine) Color(0xFF6550A0) else Color(0xFF263D5A)),
                                modifier = Modifier.widthIn(max = 350.dp)
                            ) {
                                Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                                    Text(
                                        message.text,
                                        color = SoftWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        fontFamily = if (!mine && (message.text.contains("fun main") || message.text.contains("print(") || message.text.contains("console.log"))) FontFamily.Monospace else FontFamily.Default
                                    )
                                    if (!mine) {
                                        Spacer(Modifier.height(7.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            TextButton(
                                                onClick = { clipboard.setText(AnnotatedString(message.text)) },
                                                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Muted, modifier = Modifier.size(13.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("COPY", color = Muted, fontSize = 8.sp)
                                            }
                                            TextButton(
                                                onClick = { activity.speak(message.text) },
                                                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Cyan, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("PLAY", color = Cyan, fontSize = 8.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (messages.size == 1) {
                    Text("START WITH A COMMAND", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(7.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        QuickAction("Teach me Python") { sendMessage("Teach me Python from the beginning") }
                        QuickAction("Explain loops") { sendMessage("Explain loops with examples") }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Surface(
                    color = Color(0xFF111C30),
                    shape = RoundedCornerShape(23.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (listening) Violet else Color(0xFF344C6B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(start = 7.dp, end = 7.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (listening) {
                                    activity.stopListening()
                                } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    activity.startVoiceInput { spoken -> sendMessage(spoken) }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.size(43.dp).background(if (listening) Color(0xFF71345B) else Color(0xFF18394B), CircleShape)
                        ) {
                            Icon(if (listening) Icons.Default.StopCircle else Icons.Default.Mic, contentDescription = if (listening) "Stop listening" else "Speak to Wilbed", tint = if (listening) Color.White else Cyan)
                        }
                        Spacer(Modifier.width(5.dp))
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask Wilbed anything...", color = Muted, fontSize = 12.sp) },
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Spacer(Modifier.width(3.dp))
                        IconButton(
                            onClick = { sendMessage(draft) },
                            enabled = draft.isNotBlank(),
                            modifier = Modifier.size(44.dp).background(
                                if (draft.isNotBlank()) Brush.linearGradient(listOf(Cyan, Violet)) else Brush.linearGradient(listOf(Color(0xFF344057), Color(0xFF344057))),
                                CircleShape
                            )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send message", tint = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "WILBED AI  •  TEXT + VOICE  •  YOUR LEARNING COMPANION",
                    color = Color(0xFF62738F), fontSize = 7.sp, letterSpacing = 0.8.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun QuickAction(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF14243A),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF36516F))
    ) {
        Text(label, color = Cyan, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp))
    }
}
