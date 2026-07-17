package dev.ividi.weatherapp.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ENCRYPTED_PREFS_FILE_NAME = "weather_app_secure_prefs"
private const val KEY_JWT_TOKEN = "jwt_token"

/**
 * Persists the JWT in [EncryptedSharedPreferences] (never plain [SharedPreferences]) and
 * exposes it both synchronously -- for [dev.ividi.weatherapp.data.network.AuthInterceptor],
 * which runs on OkHttp's dispatcher thread -- and reactively via [tokenFlow] for the UI layer
 * to react to login/logout.
 */
@Singleton
class TokenStorage @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _tokenFlow = MutableStateFlow(readToken())
    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    fun getToken(): String? = _tokenFlow.value

    fun saveToken(token: String) {
        encryptedPrefs.edit().putString(KEY_JWT_TOKEN, token).apply()
        _tokenFlow.value = token
    }

    fun clearToken() {
        encryptedPrefs.edit().remove(KEY_JWT_TOKEN).apply()
        _tokenFlow.value = null
    }

    private fun readToken(): String? = encryptedPrefs.getString(KEY_JWT_TOKEN, null)
}
