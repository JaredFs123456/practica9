package com.example.practica9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HidratacionApp() }
    }
}

@Composable
fun HidratacionApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val vasosGuardados by HidratacionStore.vasosFlow(context).collectAsState(initial = 0)
    val eventosGuardados by HidratacionStore.eventosFlow(context).collectAsState(initial = emptyList())

    var vasos by remember { mutableIntStateOf(0) }
    val eventos = remember { mutableStateListOf<String>() }

    LaunchedEffect(vasosGuardados) { vasos = vasosGuardados }
    LaunchedEffect(eventosGuardados) {
        eventos.clear()
        eventos.addAll(eventosGuardados)
    }

    fun horaActual(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun agregarEvento(texto: String) {
        val item = "${horaActual()}  $texto"
        eventos.add(0, item)

        val limitado = eventos.take(20) // en teléfono podemos guardar más
        eventos.clear()
        eventos.addAll(limitado)

        coroutineScope.launch {
            HidratacionStore.setEventos(context, eventos.toList())
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vasos hoy",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = vasos.toString(),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    val nuevo = vasos + 1
                    vasos = nuevo
                    agregarEvento("💧 Tomé agua")
                    coroutineScope.launch { HidratacionStore.setVasos(context, nuevo) }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Tomé agua") }

            OutlinedButton(
                onClick = { agregarEvento("🧍 Pausa activa") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Pausa activa") }

            OutlinedButton(
                onClick = {
                    vasos = 0
                    eventos.clear()
                    coroutineScope.launch { HidratacionStore.clearAll(context) }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reiniciar día") }

            if (eventos.isNotEmpty()) {
                Text(
                    text = "Últimos registros",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                eventos.take(10).forEach { item ->
                    Text(
                        text = "• $item",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
