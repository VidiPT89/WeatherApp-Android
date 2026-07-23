package dev.ividi.weatherapp.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.repository.AppLanguage
import dev.ividi.weatherapp.data.repository.ThemeMode
import dev.ividi.weatherapp.ui.common.UiState

private const val WEBSITE_URL = "https://ividi.dev"
private const val GITHUB_URL = "https://github.com/VidiPT89"

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferencesState by viewModel.preferencesState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        Text(text = stringResource(R.string.settings_units_label), style = MaterialTheme.typography.bodyLarge)

        when (val state = preferencesState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Empty -> Unit
            is UiState.Success -> {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    Units.entries.forEachIndexed { index, unit ->
                        SegmentedButton(
                            selected = state.data == unit,
                            onClick = { viewModel.updateUnits(unit) },
                            shape = SegmentedButtonDefaults.itemShape(index, Units.entries.size),
                        ) {
                            Text(unit.temperatureSymbol)
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.settings_language_title), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(R.string.settings_language_subtitle), style = MaterialTheme.typography.bodySmall)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AppLanguage.entries.forEachIndexed { index, entry ->
                    SegmentedButton(
                        selected = language == entry,
                        onClick = { viewModel.updateLanguage(entry) },
                        shape = SegmentedButtonDefaults.itemShape(index, AppLanguage.entries.size),
                    ) {
                        Text(
                            when (entry) {
                                AppLanguage.PT -> stringResource(R.string.settings_language_pt)
                                AppLanguage.EN -> stringResource(R.string.settings_language_en)
                            },
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(R.string.settings_theme_subtitle), style = MaterialTheme.typography.bodySmall)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, entry ->
                    SegmentedButton(
                        selected = themeMode == entry,
                        onClick = { viewModel.updateThemeMode(entry) },
                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                    ) {
                        Text(
                            when (entry) {
                                ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                                ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                                ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                            },
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val context = LocalContext.current
            Text(text = stringResource(R.string.settings_about_title), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL))) }) {
                Text(stringResource(R.string.settings_about_website))
            }
            TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))) }) {
                Text(stringResource(R.string.settings_about_github))
            }
            Text(text = stringResource(R.string.settings_about_footer), style = MaterialTheme.typography.bodySmall)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = {
                viewModel.logout()
                onLoggedOut()
            }) {
                Text(stringResource(R.string.settings_logout))
            }
        }
    }
}
