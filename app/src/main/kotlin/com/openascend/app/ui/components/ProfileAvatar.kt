package com.openascend.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ProfileAvatar(
    avatarRelativePath: String?,
    modifier: Modifier = Modifier,
    size: Dp,
    contentDescription: String = "Profile picture",
    onClick: (() -> Unit)? = null,
    /** Used for accessibility when [onClick] is set (e.g. “Character sheet”). */
    onClickLabel: String? = null,
) {
    val context = LocalContext.current
    val file = remember(avatarRelativePath) {
        avatarRelativePath?.let { File(context.filesDir, it).takeIf { f -> f.exists() } }
    }
    val shapeAndSize = modifier
        .size(size)
        .clip(CircleShape)
    val interactive = if (onClick != null) {
        shapeAndSize.clickable(onClick = onClick, onClickLabel = onClickLabel)
    } else {
        shapeAndSize
    }
    Box(
        interactive
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (file != null) {
            AsyncImage(
                model = file,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(size * 0.55f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
