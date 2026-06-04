package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.datastore.PinDataStore
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.repository.CategoryRepository
import com.example.myapplication.data.repository.PinRepository
import com.example.myapplication.data.repository.TaskRepository
import com.example.myapplication.ui.navigation.AppNavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val pinDataStore = PinDataStore(this)
        val pinRepo = PinRepository(pinDataStore)
        val taskRepo = TaskRepository(db.taskDao())
        val catRepo = CategoryRepository(db.categoryDao())

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        pinRepo = pinRepo,
                        taskRepo = taskRepo,
                        catRepo = catRepo,
                    )
                }
            }
        }
    }
}
