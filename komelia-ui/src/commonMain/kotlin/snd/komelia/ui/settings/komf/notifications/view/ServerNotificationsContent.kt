package snd.komelia.ui.settings.komf.notifications.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_apprise_add_url
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_discord_add_webhook
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_servers_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_servers_no_targets
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_servers_use_global_apprise
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notification_servers_use_global_discord
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.settings.komf.notifications.ServerNotificationsState

@Composable
fun ServerNotificationsContent(state: ServerNotificationsState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(Res.string.komf_notification_servers_desc),
            style = MaterialTheme.typography.bodyMedium,
        )
        ServerTargetsSection("Komga", state.komga)
        HorizontalDivider()
        ServerTargetsSection("Kavita", state.kavita)
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun ServerTargetsSection(serverName: String, targets: ServerNotificationsState.ServerTargets) {
    Text(serverName, style = MaterialTheme.typography.titleMedium)

    TargetsEditor(
        useGlobalLabel = stringResource(Res.string.komf_notification_servers_use_global_discord),
        targets = targets.discordWebhooks,
        onUseGlobalChange = targets::onUseGlobalDiscordChange,
        onRemove = targets::onDiscordWebhookRemove,
        addLabel = stringResource(Res.string.komf_notification_discord_add_webhook),
        addDialog = { onDismiss -> AddDiscordWebhookDialog(onDismissRequest = onDismiss, onWebhookAdd = targets::onDiscordWebhookAdd) },
    )

    TargetsEditor(
        useGlobalLabel = stringResource(Res.string.komf_notification_servers_use_global_apprise),
        targets = targets.appriseUrls,
        onUseGlobalChange = targets::onUseGlobalAppriseChange,
        onRemove = targets::onAppriseUrlRemove,
        addLabel = stringResource(Res.string.komf_notification_apprise_add_url),
        addDialog = { onDismiss -> AddUrlDialog(onDismissRequest = onDismiss, onUrlAdd = targets::onAppriseUrlAdd) },
    )
}

@Composable
private fun TargetsEditor(
    useGlobalLabel: String,
    targets: List<String>?,
    onUseGlobalChange: (Boolean) -> Unit,
    onRemove: (String) -> Unit,
    addLabel: String,
    addDialog: @Composable (onDismiss: () -> Unit) -> Unit,
) {
    SwitchWithLabel(
        checked = targets == null,
        onCheckedChange = onUseGlobalChange,
        label = { Text(useGlobalLabel) },
    )
    if (targets == null) return

    if (targets.isEmpty()) {
        Text(
            stringResource(Res.string.komf_notification_servers_no_targets),
            style = MaterialTheme.typography.bodySmall,
        )
    }
    targets.forEach { target ->
        Row {
            TextField(
                value = target,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onRemove(target) }, modifier = Modifier.cursorForHand()) {
                Icon(Icons.Default.Delete, null)
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    FilledTonalButton(onClick = { showAddDialog = true }, modifier = Modifier.cursorForHand()) {
        Text(addLabel)
    }
    if (showAddDialog) addDialog { showAddDialog = false }
}
