package dev.ividi.weatherapp.ui.auth

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ividi.weatherapp.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onClose: (() -> Unit)? = null,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    AuthForm(
        title = R.string.app_name,
        subtitle = R.string.auth_login_subtitle,
        submitLabel = R.string.auth_login_button,
        switchPrompt = R.string.auth_no_account_prompt,
        onSubmit = viewModel::login,
        onSuccess = onLoginSuccess,
        onSwitch = onNavigateToRegister,
        viewModel = viewModel,
        onClose = onClose,
    )
}
