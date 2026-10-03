package dev.ividi.weatherapp.ui.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.BuildConfig
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.ui.common.UiState
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

/**
 * The email/password + Google form shared by [LoginScreen] and [RegisterScreen]. The two only
 * differ in their texts and in which [AuthViewModel] call the primary button makes.
 */
@Composable
internal fun AuthForm(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    @StringRes submitLabel: Int,
    @StringRes switchPrompt: Int,
    onSubmit: (email: String, password: String) -> Unit,
    onSuccess: () -> Unit,
    onSwitch: () -> Unit,
    viewModel: AuthViewModel,
    onClose: (() -> Unit)? = null,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(title), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        if (uiState is UiState.Error) {
            Text(
                text = (uiState as UiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(
            onClick = { onSubmit(email, password) },
            enabled = uiState !is UiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(stringResource(submitLabel))
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    val idToken = try {
                        signInWithGoogle(context, BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: GetCredentialCancellationException) {
                        // The user dismissing the account picker is not a failure worth reporting.
                        return@launch
                    } catch (_: NoCredentialException) {
                        viewModel.showError(context.getString(R.string.auth_google_no_account))
                        return@launch
                    } catch (_: Exception) {
                        viewModel.showError(context.getString(R.string.auth_google_sign_in_failed))
                        return@launch
                    }
                    viewModel.loginWithOAuth("google", idToken)
                }
            },
            enabled = uiState !is UiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Text(stringResource(R.string.auth_google_button))
        }

        TextButton(
            onClick = onSwitch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            Text(stringResource(switchPrompt))
        }

        if (onClose != null) {
            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.auth_close_action))
            }
        }
    }
}
