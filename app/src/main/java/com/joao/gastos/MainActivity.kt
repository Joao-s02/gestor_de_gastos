package com.joao.gastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.joao.gastos.ui.GastosAppRoot
import com.joao.gastos.ui.theme.GastosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as GastosApp).container.repository
        setContent {
            GastosTheme {
                GastosAppRoot(repository)
            }
        }
    }
}
