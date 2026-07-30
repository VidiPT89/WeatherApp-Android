package dev.ividi.weatherapp.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.UserAccount
import dev.ividi.weatherapp.ui.common.UiState
import dev.ividi.weatherapp.util.toDisplayDateTime

@Composable
fun AdminScreen(onBack: () -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val usersState by viewModel.usersState.collectAsStateWithLifecycle()
    val deleteError by viewModel.deleteError.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<UserAccount?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.admin_back_action))
            }
            Text(text = stringResource(R.string.admin_title), style = MaterialTheme.typography.titleLarge)
        }
        Text(text = stringResource(R.string.admin_subtitle), style = MaterialTheme.typography.bodyMedium)

        deleteError?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3_000)
                viewModel.consumeDeleteError()
            }
        }

        when (val state = usersState) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is UiState.Empty -> Text(stringResource(R.string.admin_empty))
            is UiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.data, key = { it.id }) { user ->
                        UserRow(
                            user = user,
                            isSelf = user.id == viewModel.currentUserId,
                            onDeleteRequest = { pendingDelete = user },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.admin_confirm_title)) },
            text = { Text(stringResource(R.string.admin_confirm_message, user.email)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(user.id)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.admin_confirm_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.admin_confirm_cancel))
                }
            },
        )
    }
}

@Composable
private fun UserRow(user: UserAccount, isSelf: Boolean, onDeleteRequest: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.email, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(
                        if (user.isAdmin) R.string.admin_role_admin else R.string.admin_role_user,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(text = user.createdAt.toDisplayDateTime(), style = MaterialTheme.typography.labelMedium)
            }
            if (!isSelf) {
                IconButton(onClick = onDeleteRequest) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.admin_delete_action, user.email),
                    )
                }
            }
        }
    }
}
