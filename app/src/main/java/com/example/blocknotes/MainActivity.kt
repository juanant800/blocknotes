package com.example.blocknotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blocknotes.ui.BlockNotesTheme
import com.example.blocknotes.ui.EditorScreen
import com.example.blocknotes.ui.PageListScreen
import com.example.blocknotes.ui.SearchScreen
import com.example.blocknotes.ui.TrashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockNotesTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "list/root") {
                    composable("list/{parent}") { e ->
                        val parent = e.arguments?.getString("parent")?.takeIf { it != "root" }
                        PageListScreen(
                            parentId = parent,
                            onOpen = { nav.navigate("page/$it") },
                            onBack = { nav.popBackStack() },
                            onSearch = { nav.navigate("search") },
                            onTrash = { nav.navigate("trash") },
                        )
                    }
                    composable("page/{id}") { e ->
                        EditorScreen(
                            pageId = e.arguments!!.getString("id")!!,
                            onSubpages = { nav.navigate("list/$it") },
                            onBack = { nav.popBackStack() },
                        )
                    }
                    composable("search") {
                        SearchScreen(onOpen = { nav.navigate("page/$it") }, onBack = { nav.popBackStack() })
                    }
                    composable("trash") {
                        TrashScreen(onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
