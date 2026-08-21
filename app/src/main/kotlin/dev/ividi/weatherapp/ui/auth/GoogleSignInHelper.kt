package dev.ividi.weatherapp.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Drives Android's Credential Manager to get a native Google ID token -- the same shape the
 * backend's `OidcIdTokenVerifier` already checks for the web and iOS clients. [serverClientId] is
 * the WeatherApp Google Cloud project's *Web* OAuth client id (`BuildConfig.GOOGLE_WEB_CLIENT_ID`)
 * -- Credential Manager calls this the "server client id" regardless of the caller being a native
 * app, since it's the audience the backend later verifies the token against.
 */
suspend fun signInWithGoogle(context: Context, serverClientId: String): String {
    val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

    val result = CredentialManager.create(context).getCredential(context, request)

    val credential = result.credential
    require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        "Unexpected credential type from Google sign-in"
    }
    return GoogleIdTokenCredential.createFrom(credential.data).idToken
}
