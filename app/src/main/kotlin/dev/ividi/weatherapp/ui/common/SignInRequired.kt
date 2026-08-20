package dev.ividi.weatherapp.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R

/**
 * Shown in place of Favorites/History content for a guest (not signed in) user, since those two
 * features are the only ones still gated behind an account -- weather lookup itself is anonymous.
 */
@Composable
fun SignInRequired(message: String, onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onSignIn, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.sign_in_required_action))
        }
    }
}
