package com.example.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.presentation.theme.Practica9Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    Practica9Theme {
        val eventos = remember { mutableStateListOf<String>() }
        var vasos by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    vasos += 1
                    eventos.add(0, "💧 Tomé agua")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tomé agua")
            }

            OutlinedButton(
                onClick = {
                    eventos.add(0, "🧍 Pausa activa")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pausa activa")
            }

            if (eventos.isNotEmpty()) {
                Text(
                    text = "Últimos registros",
                    style = MaterialTheme.typography.title3,
                    modifier = Modifier.padding(top = 4.dp)
                )

                eventos.take(3).forEach { item ->
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
