package com.openascend.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.openascend.app.ui.OpenAscendRoot
import com.openascend.app.ui.theme.OpenAscendTheme
import com.openascend.domain.model.ThemePreference

@Composable
fun OpenAscendAppContent() {
    val owner = checkNotNull(LocalViewModelStoreOwner.current)
    val vm: MainActivityViewModel = hiltViewModel(owner)
    val themePref by vm.themePreference.collectAsState()
    val darkTheme = when (themePref) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val dynamicColor = themePref == ThemePreference.SYSTEM
    OpenAscendTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        Surface(modifier = Modifier.fillMaxSize()) {
            OpenAscendRoot()
        }
    }
}
