package dev.ividi.weatherapp.util

import dev.ividi.weatherapp.data.model.GeocodingResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

private const val SEARCH_DEBOUNCE_MS = 300L
private const val MIN_QUERY_LENGTH = 2

/**
 * Shared debounced-autocomplete pipeline used by the Dashboard's city search box: waits
 * [SEARCH_DEBOUNCE_MS], requires at least [MIN_QUERY_LENGTH] characters, and cancels any
 * in-flight lookup as soon as a newer query arrives.
 */
@OptIn(FlowPreview::class)
fun citySuggestionsFlow(
    queryFlow: Flow<String>,
    search: suspend (String) -> List<GeocodingResult>,
): Flow<List<GeocodingResult>> =
    queryFlow
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.trim().length < MIN_QUERY_LENGTH) {
                flowOf(emptyList())
            } else {
                flow { emit(runCatching { search(query.trim()) }.getOrDefault(emptyList())) }
            }
        }
