package com.lifehub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF172554)
private val Blue = Color(0xFF4568F5)
private val Violet = Color(0xFF7655E8)
private val Canvas = Color(0xFFF5F7FC)

data class LifeTask(val id: Long, val title: String, val done: Boolean)

class TaskStore(context: android.content.Context) {
    private val db = context.openOrCreateDatabase("lifehub.db", 0, null)
    init {
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, done INTEGER NOT NULL DEFAULT 0)")
    }
    fun all(): List<LifeTask> {
        val result = mutableListOf<LifeTask>()
        db.rawQuery("SELECT id, title, done FROM tasks ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) result.add(LifeTask(c.getLong(0), c.getString(1), c.getInt(2) == 1))
        }
        return result
    }
    fun add(title: String) {
        val values = android.content.ContentValues().apply { put("title", title); put("done", 0) }
        db.insert("tasks", null, values)
    }
    fun toggle(task: LifeTask) {
        val values = android.content.ContentValues().apply { put("done", if (task.done) 0 else 1) }
        db.update("tasks", values, "id=?", arrayOf(task.id.toString()))
    }
    fun delete(task: LifeTask) { db.delete("tasks", "id=?", arrayOf(task.id.toString())) }
}

class LifeHubViewModel(private val store: TaskStore) : ViewModel() {
    var tasks by mutableStateOf(store.all())
        private set
    fun add(title: String) {
        val clean = title.trim()
        if (clean.isNotEmpty()) { store.add(clean); refresh() }
    }
    fun toggle(task: LifeTask) { store.toggle(task); refresh() }
    fun delete(task: LifeTask) { store.delete(task); refresh() }
    private fun refresh() { tasks = store.all() }
}

class LifeHubViewModelFactory(private val store: TaskStore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LifeHubViewModel(store) as T
}

class MainActivity : ComponentActivity() {
    private val store by lazy { TaskStore(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: LifeHubViewModel = view(factory = LifeHubViewModelFactory(store))
            LifeHubApp(vm)
        }
    }
}

@Composable
fun LifeHubApp(vm: LifeHubViewModel) {
    var selected by remember { mutableStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var newTask by remember { mutableStateOf("") }
    val completed = vm.tasks.count { it.done }
    val pending = vm.tasks.size - completed

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue,
            secondary = Violet,
            background = Canvas,
            surface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Canvas,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(selected == 0, { selected = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                    NavigationBarItem(selected == 1, { selected = 1 }, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Tasks") })
                    NavigationBarItem(selected == 2, { selected = 2 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
                }
            },
            floatingActionButton = {
                if (selected != 2) FloatingActionButton(onClick = { showAdd = true }, containerColor = Blue, contentColor = Color.White) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
            }
        ) { padding ->
            when (selected) {
                0 -> HomeScreen(vm.tasks, completed, pending, Modifier.padding(padding), onTasks = { selected = 1 })
                1 -> TasksScreen(vm.tasks, Modifier.padding(padding), vm::toggle, vm::delete)
                else -> ProfileScreen(Modifier.padding(padding))
            }
        }

        if (showAdd) {
            AlertDialog(
                onDismissRequest = { showAdd = false; newTask = "" },
                title = { Text("Add a task") },
                text = {
                    OutlinedTextField(
                        value = newTask,
                        onValueChange = { newTask = it },
                        label = { Text("What do you need to do?") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = { vm.add(newTask); newTask = ""; showAdd = false }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showAdd = false; newTask = "" }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(tasks: List<LifeTask>, completed: Int, pending: Int, modifier: Modifier, onTasks: () -> Unit) {
    val date = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("LIFEHUB", color = Blue, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("Your day, in focus.", color = Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(date, color = Color(0xFF7A849B))
                }
                Box(Modifier.size(46.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Blue)
                }
            }
        }
        item {
            Box(
                Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Blue, Violet)), RoundedCornerShape(24.dp)).padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MAKE TODAY COUNT", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("Small steps. Big progress.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Keep your plans organized and move forward one task at a time.", color = Color.White.copy(alpha = .9f))
                }
            }
        }
        item {
            Text("Today's overview", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 10.dp)) {
                StatCard("To do", pending.toString(), Icons.Default.PendingActions, Modifier.weight(1f))
                StatCard("Completed", completed.toString(), Icons.Default.TaskAlt, Modifier.weight(1f))
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your tasks", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onTasks) { Text("See all") }
            }
        }
        if (tasks.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Checklist, null, tint = Blue, modifier = Modifier.size(34.dp))
                        Text("Nothing planned yet", fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("Tap + to add your first task.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            items(tasks.take(3), key = { it.id }) { task ->
                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (task.done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (task.done) Blue else Color.Gray)
                    Spacer(Modifier.width(10.dp))
                    Text(task.title, color = if (task.done) Color.Gray else Ink)
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = Color.White) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Blue)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = Ink, fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFF7A849B))
        }
    }
}

@Composable
fun TasksScreen(tasks: List<LifeTask>, modifier: Modifier, onToggle: (LifeTask) -> Unit, onDelete: (LifeTask) -> Unit) {
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("My Tasks", color = Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp))
        Text("Keep track of what matters.", color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Checklist, null, tint = Blue, modifier = Modifier.size(48.dp))
                    Text("No tasks yet", fontWeight = FontWeight.Bold)
                    Text("Use the + button to add one.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(tasks, key = { it.id }) { task -> TaskRow(task, { onToggle(task) }, { onDelete(task) }) }
            }
        }
    }
}

@Composable
fun TaskRow(task: LifeTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Text(task.title, modifier = Modifier.weight(1f), color = if (task.done) Color.Gray else Ink, fontWeight = FontWeight.Medium)
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, "Delete task", tint = Color.Gray) }
        }
    }
}

@Composable
fun ProfileScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, color = Ink, fontWeight = FontWeight.Bold)
        Surface(color = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).background(Color(0xFFE9EDFF), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Blue, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Welcome to LifeHub", fontWeight = FontWeight.Bold, color = Ink)
                    Text("Your personal space", color = Color.Gray)
                }
            }
        }
        Text("Version 1.0.0", color = Color.Gray)
        Text("Your tasks are stored locally on this device.", color = Color.Gray)
    }
}
