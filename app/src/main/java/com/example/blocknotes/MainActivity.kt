package com.example.blocknotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blocknotes.ui.EditorScreen
import com.example.blocknotes.ui.PageListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "list/root") {
                    composable("list/{parent}") { e ->
                        val parent = e.arguments?.getString("parent")?.takeIf { it != "root" }
                        PageListScreen(parent, onOpen = { nav.navigate("page/$it") }, onBack = { nav.popBackStack() })
                    }
                    composable("page/{id}") { e ->
                        EditorScreen(e.arguments!!.getString("id")!!, onSubpages = { nav.navigate("list/$it") }, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
