package dev.ividi.weatherapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.data.model.GeocodingResult

private const val SUGGESTIONS_MAX_HEIGHT_DP = 240

/**
 * A city search field with a debounced autocomplete dropdown. The debounce itself lives in
 * the caller's ViewModel (see [dev.ividi.weatherapp.util.citySuggestionsFlow]) -- this
 * composable is purely presentational.
 */
@Composable
fun SearchAutocompleteField(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<GeocodingResult>,
    onSuggestionSelected: (GeocodingResult) -> Unit,
    onSearchSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Procurar cidade...",
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit(query) }),
        )

        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = SUGGESTIONS_MAX_HEIGHT_DP.dp)
                    .padding(top = 4.dp),
            ) {
                LazyColumn {
                    items(suggestions) { suggestion ->
                        ListItem(
                            headlineContent = { Text(suggestion.name) },
                            supportingContent = { Text(suggestion.country) },
                            leadingContent = {
                                Icon(Icons.Filled.LocationOn, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(suggestion) },
                        )
                    }
                }
            }
        }
    }
}
