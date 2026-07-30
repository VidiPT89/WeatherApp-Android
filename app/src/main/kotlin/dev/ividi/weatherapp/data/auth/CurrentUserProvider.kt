package dev.ividi.weatherapp.data.auth

import dev.ividi.weatherapp.data.model.UserAccount
import kotlinx.coroutines.flow.StateFlow

/**
 * Minimal contract for reading the caller's own account -- extracted (mirroring [TokenStore])
 * so a ViewModel that only needs this (e.g. `AdminViewModel`, to hide the self-delete action on
 * the caller's own row) can be unit tested against an in-memory fake instead of the concrete
 * [AuthRepository], which pulls in a real Android Context-backed `TokenStorage`.
 */
interface CurrentUserProvider {
    val currentUser: StateFlow<UserAccount?>
}
