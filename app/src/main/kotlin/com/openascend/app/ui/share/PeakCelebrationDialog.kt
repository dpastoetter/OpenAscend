package com.openascend.app.ui.share

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.openascend.app.R

@Composable
fun PeakCelebrationDialog(
    title: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Text(stringResource(R.string.share_peak_card))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_continue))
            }
        },
    )
}
