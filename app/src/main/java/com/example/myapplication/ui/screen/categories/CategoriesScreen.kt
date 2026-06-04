package com.example.myapplication.ui.screen.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.db.entity.CategoryEntity

private val ColorOptions = listOf(
    0xFF4A90D9.toInt(), 0xFFE74C3C.toInt(), 0xFF2ECC71.toInt(),
    0xFFF39C12.toInt(), 0xFF9B59B6.toInt(), 0xFF1ABC9C.toInt(),
    0xFFE67E22.toInt(), 0xFF34495E.toInt(), 0xFFE91E90.toInt(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editCat by remember { mutableStateOf<CategoryEntity?>(null) }

    if (showDialog || editCat != null) {
        var name by remember(showDialog, editCat) { mutableStateOf(editCat?.name ?: "") }
        var color by remember(showDialog, editCat) { mutableIntStateOf(editCat?.color ?: ColorOptions[0]) }
        AlertDialog(
            onDismissRequest = { showDialog = false; editCat = null },
            title = { Text(if (editCat != null) "Edit" else "New Category") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorOptions.forEach { c ->
                            Box(
                                Modifier.size(32.dp).clip(CircleShape).background(Color(c))
                                    .clickable { color = c }
                                    .let { if (c == color) it.padding(2.dp) else it }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        if (editCat != null) viewModel.update(editCat!!.copy(name = name, color = color))
                        else viewModel.create(name, color)
                        showDialog = false; editCat = null
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false; editCat = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Filled.Add, "New") } },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.categories, key = { it.id }) { cat ->
                var showDelete by remember { mutableStateOf(false) }
                if (showDelete) {
                    AlertDialog(
                        onDismissRequest = { showDelete = false },
                        title = { Text("Delete?") },
                        text = { Text("Tasks in this category will become uncategorized.") },
                        confirmButton = { TextButton(onClick = { viewModel.delete(cat); showDelete = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
                        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } },
                    )
                }
                Card(Modifier.fillMaxWidth().clickable { editCat = cat }, shape = RoundedCornerShape(8.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(20.dp), shape = CircleShape, color = Color(cat.color)) {}
                        Spacer(Modifier.width(12.dp))
                        Text(cat.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { showDelete = true }) { Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}
