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
private const val KEY_REFRESH_TOKEN = "refresh_token"

/**
 * Persists the access + refresh tokens in [EncryptedSharedPreferences] (never plain
 * [SharedPreferences]) and exposes the access token both synchronously -- for
 * [dev.ividi.weatherapp.data.network.AuthInterceptor] and
 * [dev.ividi.weatherapp.data.network.TokenAuthenticator], which run on OkHttp's dispatcher
 * thread -- and reactively via [tokenFlow] for the UI layer to react to login/logout.
 */
@Singleton
class TokenStorage @Inject constructor(@ApplicationContext context: Context) : TokenStore {

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

    override fun getToken(): String? = _tokenFlow.value

    override fun getRefreshToken(): String? = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)

    override fun saveTokens(token: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString(KEY_JWT_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        _tokenFlow.value = token
    }

    override fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
        _tokenFlow.value = null
    }

    private fun readToken(): String? = encryptedPrefs.getString(KEY_JWT_TOKEN, null)
}
