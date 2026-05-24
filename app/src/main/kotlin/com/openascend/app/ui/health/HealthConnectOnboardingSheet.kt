package com.openascend.app.ui.health

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.openascend.app.R

@Composable
fun HealthConnectOnboardingSheet(
  onDismiss: () -> Unit,
  onBindVitals: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.health_connect_onboarding_title)) },
    text = {
      Text(
        stringResource(R.string.health_connect_onboarding_body),
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    confirmButton = {
      TextButton(onClick = onBindVitals) {
        Text(stringResource(R.string.health_connect_onboarding_bind))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.health_connect_onboarding_later))
      }
    },
  )
}
