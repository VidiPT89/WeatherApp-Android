package dev.ividi.weatherapp.ui.auth

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ividi.weatherapp.R

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    AuthForm(
        title = R.string.auth_register_title,
        subtitle = R.string.auth_register_subtitle,
        submitLabel = R.string.auth_register_button,
        switchPrompt = R.string.auth_has_account_prompt,
        onSubmit = viewModel::register,
        onSuccess = onRegisterSuccess,
        onSwitch = onNavigateToLogin,
        viewModel = viewModel,
    )
}
