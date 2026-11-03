package org.wilbedai.wilbedai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
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
            primary = Cyan, secondary = Violet, background = Night,
            surface = Panel, onBackground = SoftWhite, onSurface = SoftWhite
        ),
        content = content
    )
}

private data class Message(val sender: String, val text: String)
private enum class Page { HOME, CHAT, ACADEMY, SETTINGS }

@Composable
private fun WilbedApp() {
    var page by rememberSaveable { mutableStateOf(Page.HOME) }
    var draft by rememberSaveable { mutableStateOf("") }
    val brain = remember { WilbedBrain() }
    val messages = remember {
        mutableStateListOf(Message("WILBED", "System ready. Hello! I am Wilbed, your personal assistant and programming tutor."))
    }

    Scaffold(
        containerColor = Night,
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                val pages = listOf(Page.HOME, Page.CHAT, Page.ACADEMY, Page.SETTINGS)
                pages.forEach { item ->
                    val label = item.name.lowercase().replaceFirstChar { it.uppercase() }
                    val icon = when (item) {
                        Page.HOME -> Icons.Default.Home
                        Page.CHAT -> Icons.Default.AutoAwesome
                        Page.ACADEMY -> Icons.Default.MenuBook
                        Page.SETTINGS -> Icons.Default.Settings
                    }
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan, selectedTextColor = Cyan,
                            indicatorColor = Color(0xFF24334D),
                            unselectedIconColor = Muted, unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (page) {
            Page.HOME -> HomeScreen(Modifier.padding(padding), { page = Page.CHAT }, { page = Page.ACADEMY })
            Page.CHAT -> ChatScreen(
                Modifier.padding(padding), messages, draft, { draft = it },
                {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        messages.add(Message("YOU", text))
                        messages.add(Message("WILBED", brain.respond(text)))
                        draft = ""
                    }
                }
            )
            Page.ACADEMY -> AcademyScreen(Modifier.padding(padding)) { language ->
                messages.add(Message("WILBED", brain.respond("teach me " + language)))
                page = Page.CHAT
            }
            Page.SETTINGS -> SettingsScreen(Modifier.padding(padding))
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, onChat: () -> Unit, onAcademy: () -> Unit) {
    Column(modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = Cyan, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Text("WILBED AI", fontSize = 23.sp, fontWeight = FontWeight.Black, color = SoftWhite)
            Spacer(Modifier.weight(1f))
            Surface(color = Color(0xFF12343E), shape = RoundedCornerShape(30.dp)) {
                Text("● ONLINE", color = Cyan, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(36.dp))
        Box(
            Modifier.size(190.dp).clip(CircleShape).background(
                Brush.radialGradient(listOf(Color(0xFF5A43B9), Color(0xFF173A63), Night))
            ),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(112.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Cyan, Violet))))
            Icon(Icons.Default.AutoAwesome, "Wilbed AI core", tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text("Your personal AI assistant", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Ask questions, explore ideas, and learn to code one step at a time.", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))
        Button(onClick = onChat, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet)) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(Modifier.width(8.dp))
            Text("Start chatting")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onAcademy, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Icon(Icons.Default.Code, null)
            Spacer(Modifier.width(8.dp))
            Text("Open programming academy")
        }
        Spacer(Modifier.height(24.dp))
        Text("LEARN • CREATE • EXPLORE", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}

@Composable
private fun ChatScreen(
    modifier: Modifier,
    messages: List<Message>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(modifier.fillMaxSize().padding(14.dp)) {
        Text("Wilbed Chat", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Your offline assistant", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { message ->
                val mine = message.sender == "YOU"
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                    Surface(color = if (mine) Color(0xFF332B58) else Panel, shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.widthIn(max = 310.dp).padding(13.dp)) {
                            Text(message.sender, color = if (mine) Violet else Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            Text(message.text, color = SoftWhite, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft, onValueChange = onDraftChange, modifier = Modifier.weight(1f),
                placeholder = { Text("Message Wilbed...") }, maxLines = 3,
                shape = RoundedCornerShape(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSend, modifier = Modifier.size(50.dp).background(Violet, CircleShape)) {
                Icon(Icons.Default.Send, "Send message", tint = Color.White)
            }
        }
    }
}

@Composable
private fun AcademyScreen(modifier: Modifier, onChooseLanguage: (String) -> Unit) {
    val languages = listOf("Python", "Kotlin", "Java", "JavaScript", "C++", "HTML", "CSS")
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("Programming Academy", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Choose a language. Wilbed will guide you from the basics.", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(languages) { language ->
                Card(
                    onClick = { onChooseLanguage(language) },
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, null, tint = Cyan)
                        Spacer(Modifier.width(14.dp))
                        Text(language, color = SoftWhite, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("START  →", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(22.dp)) {
        Text("Settings", color = SoftWhite, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Text("WILBED AI", color = Cyan, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Version 2.0 • Kotlin + Jetpack Compose", color = Muted)
        Spacer(Modifier.height(18.dp))
        Text("This first rebuild stage uses Wilbed's on-device assistant logic. Voice features and optional online AI will be added in later stages.", color = SoftWhite)
    }
}
