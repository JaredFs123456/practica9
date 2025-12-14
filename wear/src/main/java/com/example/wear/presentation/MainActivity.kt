package com.example.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.presentation.theme.Practica9Theme
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent { WearApp() }
    }
}

@Composable
fun WearApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val vasosGuardados by HidratacionStore.vasosFlow(context).collectAsState(initial = 0)
    val eventosGuardados by HidratacionStore.eventosFlow(context).collectAsState(initial = emptyList())

    var vasos by remember { mutableIntStateOf(0) }
    val eventos = remember { mutableStateListOf<String>() }

    val scrollState = rememberScrollState()



    LaunchedEffect(vasosGuardados) {
        vasos = vasosGuardados
    }

    LaunchedEffect(eventosGuardados) {
        eventos.clear()
        eventos.addAll(eventosGuardados)
    }

    fun horaActual(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    fun agregarEvento(texto: String) {
        val item = "${horaActual()}  $texto"
        eventos.add(0, item)

        // Guardar historial (limitamos a 10 para que no crezca)
        val limitado = eventos.take(10)
        eventos.clear()
        eventos.addAll(limitado)

        coroutineScope.launch {
            HidratacionStore.setEventos(context, eventos.toList())
        }
    }


    suspend fun detectarMovimiento10s(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return true // si no hay sensor, asumimos ok

        var movimientos = 0
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]
                val magnitud = kotlin.math.sqrt(ax * ax + ay * ay + az * az)

                // Umbral simple (ajustable). Si pasa el umbral, contamos movimiento.
                if (magnitud > 12.5f) movimientos++
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_NORMAL)
        delay(10_000) // 10 segundos
        sensorManager.unregisterListener(listener)

        // Si casi no hubo movimiento, lo consideramos inactividad
        return movimientos >= 2
    }




    Practica9Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vasos hoy",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.title3
            )

            Text(
                text = vasos.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.display1
            )

            Button(
                onClick = {
                    val nuevo = vasos + 1
                    vasos = nuevo
                    agregarEvento("💧 Tomé agua")

                    coroutineScope.launch {
                        HidratacionStore.setVasos(context, nuevo)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Tomé agua") }

            OutlinedButton(
                onClick = {
                    agregarEvento("🧍 Pausa activa")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Pausa activa") }


            OutlinedButton(
                onClick = {
                    agregarEvento("🔎 Midiendo movimiento 10s...")
                    coroutineScope.launch {
                        val huboMovimiento = detectarMovimiento10s(context)
                        if (huboMovimiento) {
                            agregarEvento("✅ Movimiento detectado")
                        } else {
                            agregarEvento("⚠️ Inactividad detectada")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Detectar movimiento") }


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
                    style = MaterialTheme.typography.title3,
                    modifier = Modifier.padding(top = 4.dp)
                )

                eventos.take(4).forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}
