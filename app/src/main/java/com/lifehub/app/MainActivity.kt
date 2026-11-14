
package com.lifehub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Blue = Color(0xFF4568F5)
private val Purple = Color(0xFF7655E8)
private val Ink = Color(0xFF172554)
private val Background = Color(0xFFF5F7FC)

data class Task(
    val id: Int,
    val title: String,
    val done: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LifeHubApp()
        }
    }
}

@Composable
fun LifeHubApp() {
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("") }

    val completed = tasks.count { it.done }
    val pending = tasks.size - completed

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue,
            secondary = Purple,
            background = Background
        )
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.CheckCircle, null) },
                        label = { Text("Tasks") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Profile") }
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab != 2) {
                    FloatingActionButton(
                        onClick = { showDialog = true },
                        containerColor = Blue,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, "Add task")
                    }
                }
            }
        ) { padding ->

            when (selectedTab) {
                0 -> HomeScreen(
                    tasks, pending, completed,
                    Modifier.padding(padding)
                )

                1 -> TaskScreen(
                    tasks,
                    Modifier.padding(padding),
                    onToggle = { task ->
                        tasks = tasks.map {
                            if (it.id == task.id)
                                it.copy(done = !it.done)
                            else it
                        }
                    },
                    onDelete = { task ->
                        tasks = tasks.filterNot {
                            it.id == task.id
                        }
                    }
                )

                2 -> ProfileScreen(Modifier.padding(padding))
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add a task") },
                text = {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (taskName.isNotBlank()) {
                            tasks = tasks + Task(
                                id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                                title = taskName.trim()
                            )
                            taskName = ""
                            showDialog = false
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        taskName = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    tasks: List<Task>,
    pending: Int,
    completed: Int,
    modifier: Modifier
) {
    val date = SimpleDateFormat(
        "EEEE, MMMM d",
        Locale.getDefault()
    ).format(Date())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                "LIFEHUB",
                color = Blue,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Your day, in focus.",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                fontWeight = FontWeight.Bold
            )

            Text(date, color = Color.Gray)
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Blue, Purple)
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "MAKE TODAY COUNT",
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Text(
                        "Small steps. Big progress.",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Organize your life, one task at a time.",
                        color = Color.White
                    )
                }
            }
        }

        item {
            Text(
                "Today's overview",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                StatCard("To do", pending, Modifier.weight(1f))
                StatCard("Completed", completed, Modifier.weight(1f))
            }
        }

        item {
            Text(
                "Your tasks",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        }

        if (tasks.isEmpty()) {
            item {
                Text(
                    "Nothing planned yet. Tap + to add your first task.",
                    color = Color.Gray
                )
            }
        } else {
            items(tasks.take(3)) { task ->
                Text(
                    (if (task.done) "✓ " else "○ ") + task.title,
                    color = Ink
                )
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: Int,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.TaskAlt, null, tint = Blue)
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
            Text(label, color = Color.Gray)
        }
    }
}

@Composable
fun TaskScreen(
    tasks: List<Task>,
    modifier: Modifier,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Column(modifier.padding(20.dp)) {
        Text(
            "My Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Text("Keep track of what matters.", color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.done,
                            onCheckedChange = { onToggle(task) }
                        )

                        Text(
                            task.title,
                            Modifier.weight(1f),
                            color = Ink
                        )

                        IconButton(
                            onClick = { onDelete(task) }
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                "Delete"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(modifier: Modifier) {
    Column(
        modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            fontWeight = FontWeight.Bold
        )

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    null,
                    tint = Blue,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    "Welcome to LifeHub",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text("Version 1.0.0", color = Color.Gray)
        Text("Your personal productivity app.", color = Color.Gray)
    }
}
