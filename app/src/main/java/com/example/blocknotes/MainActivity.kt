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
import com.example.blocknotes.ui.MainScreen
import com.example.blocknotes.ui.PageListScreen
import com.example.blocknotes.ui.Prefs
import com.example.blocknotes.ui.SearchScreen
import com.example.blocknotes.ui.TrashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Prefs.load(this)
        setContent {
            BlockNotesTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            onOpenPage = { nav.navigate("page/$it") },
                            onSearch = { nav.navigate("search") },
                            onTrash = { nav.navigate("trash") },
                        )
                    }
                    composable("list/{parent}") { e ->
                        PageListScreen(
                            parentId = e.arguments?.getString("parent"),
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
