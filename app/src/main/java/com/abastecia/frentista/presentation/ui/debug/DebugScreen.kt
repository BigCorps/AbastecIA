package com.abastecia.frentista.presentation.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Logger global acessível de qualquer lugar do app
object AppLogger {
    private val _logs = mutableStateListOf<LogEntry>()
    val logs: List<LogEntry> get() = _logs
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    fun d(tag: String, message: String) {
        val entry = LogEntry("D", tag, message)
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            _logs.add(entry)
        } else {
            handler.post { _logs.add(entry) }
        }
        android.util.Log.d(tag, message)
    }

    fun e(tag: String, message: String) {
        val entry = LogEntry("E", tag, message)
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            _logs.add(entry)
        } else {
            handler.post { _logs.add(entry) }
        }
        android.util.Log.e(tag, message)
    }

    fun clear() {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            _logs.clear()
        } else {
            handler.post { _logs.clear() }
        }
    }
}

data class LogEntry(
    val level: String,
    val tag: String,
    val message: String,
    val time: String = java.text.SimpleDateFormat(
        "HH:mm:ss", java.util.Locale.getDefault()
    ).format(java.util.Date())
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onBack: () -> Unit) {
    val logs = AppLogger.logs
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    TextButton(onClick = { AppLogger.clear() }) {
                        Text("Limpar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1E1E1E)),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(logs) { log ->
                Text(
                    text = "[${log.time}][${log.level}/${log.tag}] ${log.message}",
                    color = when (log.level) {
                        "E" -> Color(0xFFFF6B6B)
                        else -> Color(0xFF90EE90)
                    },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
