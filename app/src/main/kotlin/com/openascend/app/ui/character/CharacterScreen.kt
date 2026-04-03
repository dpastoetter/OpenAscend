package com.openascend.app.ui.character

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.openascend.app.ui.components.ProfileAvatar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    onBack: () -> Unit,
    viewModel: CharacterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Character sheet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (state == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val ui = state!!
        val context = LocalContext.current
        var menuExpanded by remember { mutableStateOf(false) }
        var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

        val pickImage = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri -> uri?.let { viewModel.importAvatar(it) } }

        val takePicture = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            pendingCameraUri?.let { u ->
                if (success) viewModel.importAvatar(u)
            }
            pendingCameraUri = null
        }

        val captureFile = remember(context) { File(context.cacheDir, "camera_avatar_capture.jpg") }

        val requestCameraPermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (!granted) return@rememberLauncherForActivityResult
            captureFile.delete()
            captureFile.createNewFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                captureFile,
            )
            pendingCameraUri = uri
            takePicture.launch(uri)
        }

        fun launchCameraCapture() {
            captureFile.delete()
            captureFile.createNewFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                captureFile,
            )
            pendingCameraUri = uri
            takePicture.launch(uri)
        }

        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileAvatar(
                    avatarRelativePath = ui.profile.avatarRelativePath,
                    size = 96.dp,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        ui.profile.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Gallery or camera — stored only on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Profile photo options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Choose from gallery") },
                            onClick = {
                                menuExpanded = false
                                pickImage.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Take a selfie") },
                            onClick = {
                                menuExpanded = false
                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA,
                                    ) == PackageManager.PERMISSION_GRANTED -> launchCameraCapture()
                                    else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
                                }
                            },
                        )
                        if (ui.profile.avatarRelativePath != null) {
                            DropdownMenuItem(
                                text = { Text("Remove photo") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.clearAvatar()
                                },
                            )
                        }
                    }
                }
            }
            Text("Archetype: ${ui.progress.archetype.displayName}", fontWeight = FontWeight.SemiBold)
            Text(ui.progress.archetype.tagline, style = MaterialTheme.typography.bodyMedium)
            Text("Level ${ui.progress.level}", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = {
                    val denom = (ui.progress.xpInLevel + ui.progress.xpToNext).coerceAtLeast(1)
                    ui.progress.xpInLevel.toFloat() / denom
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Total XP ${ui.progress.totalXp}")
            Text("Streak armor: ${ui.progress.streakArmor} (narrative shield from consistency)")
            Text("Recovery ${ui.stats.recovery}")
            Text("Stamina ${ui.stats.stamina}")
            Text("Stability ${ui.stats.stability}")
            Text("Discipline ${ui.stats.discipline}")
            Text("Vitality ${ui.stats.vitality}")
            Text("XP event log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            ui.xpLog.forEach { ev ->
                Text("+${ev.amount} · ${ev.reason}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
