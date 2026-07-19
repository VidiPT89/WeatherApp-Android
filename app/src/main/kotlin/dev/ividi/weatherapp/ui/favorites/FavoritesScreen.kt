package dev.ividi.weatherapp.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.FavoriteEntry
import dev.ividi.weatherapp.ui.common.UiState

@Composable
fun FavoritesScreen(
    onFavoriteSelected: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val favoritesState by viewModel.favoritesState.collectAsStateWithLifecycle()
    val addFavoriteMessage by viewModel.addFavoriteMessage.collectAsStateWithLifecycle()
    var newCityText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newCityText,
                onValueChange = { newCityText = it },
                label = { Text(stringResource(R.string.favorites_add_placeholder)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = {
                viewModel.addFavorite(newCityText)
                newCityText = ""
            }) {
                Text(stringResource(R.string.favorites_add_button))
            }
        }

        addFavoriteMessage?.let { message ->
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3_000)
                viewModel.consumeAddFavoriteMessage()
            }
        }

        when (val state = favoritesState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Empty -> Text(stringResource(R.string.favorites_empty))
            is UiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.data) { favorite ->
                        FavoriteRow(favorite = favorite, onClick = { onFavoriteSelected(favorite.city) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(favorite: FavoriteEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null)
            Text(text = favorite.city, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
