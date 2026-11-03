package org.wilbedai.wilbedai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF080D1B)
private val Panel = Color(0xFF121B30)
private val Cyan = Color(0xFF42DDF5)
private val Violet = Color(0xFF9B78FF)
private val SoftWhite = Color(0xFFEAF1FF)
private val Muted = Color(0xFF9AA9C3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WilbedTheme { WilbedApp() } }
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

private data class Message(val sender: String, val text: String)

@Composable
private fun WilbedApp() {
    var draft by rememberSaveable { mutableStateOf("") }
    val brain = remember { WilbedBrain() }
    val messages = remember {
        mutableStateListOf(
            Message(
                "WILBED",
                "Hello! I'm Wilbed, your AI assistant and programming tutor.\n\nAsk me to teach you any programming language, explain a topic, review your code, or give you a practice task."
            )
        )
    }
    val listState = rememberLazyListState()

    fun sendMessage(messageText: String) {
        val cleanText = messageText.trim()
        if (cleanText.isNotEmpty()) {
            messages.add(Message("YOU", cleanText))
            messages.add(Message("WILBED", brain.respond(cleanText)))
            draft = ""
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(containerColor = Night) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(
                        Brush.linearGradient(listOf(Cyan, Violet))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Wilbed AI", tint = Color.White)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("WILBED AI", color = SoftWhite, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    Text("Your personal learning assistant", color = Muted, fontSize = 11.sp)
                }
                Surface(color = Color(0xFF12343E), shape = RoundedCornerShape(30.dp)) {
                    Text(
                        "● READY",
                        color = Cyan,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF26334A))
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(messages) { message ->
                    val mine = message.sender == "YOU"
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = if (mine) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            if (mine) "YOU" else "WILBED",
                            color = if (mine) Violet else Cyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp)
                        )
                        Surface(
                            color = if (mine) Color(0xFF30294F) else Panel,
                            shape = RoundedCornerShape(
                                topStart = 17.dp,
                                topEnd = 17.dp,
                                bottomEnd = if (mine) 5.dp else 17.dp,
                                bottomStart = if (mine) 17.dp else 5.dp
                            ),
                            modifier = Modifier.widthIn(max = 330.dp)
                        ) {
                            Text(
                                message.text,
                                color = SoftWhite,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            if (messages.size == 1) {
                Text("TRY ASKING", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    SuggestionChip("Teach me Python") { sendMessage("Teach me Python from the beginning") }
                    SuggestionChip("Explain loops") { sendMessage("Explain loops with examples") }
                }
                Spacer(Modifier.height(10.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask Wilbed anything...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { sendMessage(draft) },
                    enabled = draft.isNotBlank(),
                    modifier = Modifier.size(50.dp).background(
                        if (draft.isNotBlank()) Violet else Color(0xFF354057),
                        CircleShape
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send message", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF19263D),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF344866))
    ) {
        Text(label, color = Cyan, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp))
    }
}
