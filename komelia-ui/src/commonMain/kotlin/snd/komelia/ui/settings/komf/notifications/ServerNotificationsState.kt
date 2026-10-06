package snd.komelia.ui.settings.komf.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.ui.settings.komf.KomfSharedState
import snd.komf.api.PatchValue
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.KavitaConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.KomgaConfigUpdateRequest
import snd.komf.api.config.MediaServerNotificationsConfigDto
import snd.komf.api.config.MediaServerNotificationsUpdateRequest
import snd.komf.client.KomfConfigClient

/**
 * Per-server notification targets. A null list means the server uses the
 * global list from the Discord or Apprise tab; an empty list means the server
 * sends nothing there.
 */
class ServerNotificationsState(
    private val komfConfigClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    private val komfConfig: KomfSharedState,
    private val coroutineScope: CoroutineScope,
) {
    val komga = ServerTargets { KomfConfigUpdateRequest(komga = Some(KomgaConfigUpdateRequest(notifications = Some(it)))) }
    val kavita = ServerTargets { KomfConfigUpdateRequest(kavita = Some(KavitaConfigUpdateRequest(notifications = Some(it)))) }

    fun initialize(config: KomfConfig) {
        komga.initialize(config.komga.notifications)
        kavita.initialize(config.kavita.notifications)
    }

    inner class ServerTargets(
        private val toRequest: (MediaServerNotificationsUpdateRequest) -> KomfConfigUpdateRequest
    ) {
        var discordWebhooks by mutableStateOf<List<String>?>(null)
            private set
        var appriseUrls by mutableStateOf<List<String>?>(null)
            private set

        internal fun initialize(config: MediaServerNotificationsConfigDto) {
            discordWebhooks = config.discordWebhooks
            appriseUrls = config.appriseUrls
        }

        // Turning "use global" off starts an empty list (Some(emptyMap())), so the
        // server sends nothing until targets are added; turning it back on (None)
        // removes the list so the global one applies again.
        fun onUseGlobalDiscordChange(useGlobal: Boolean) {
            discordWebhooks = if (useGlobal) null else emptyList()
            update(MediaServerNotificationsUpdateRequest(discordWebhooks = useGlobalPatch(useGlobal)))
        }

        fun onDiscordWebhookAdd(webhook: String) {
            val webhooks = (discordWebhooks ?: emptyList()).plus(webhook)
            discordWebhooks = webhooks
            update(MediaServerNotificationsUpdateRequest(discordWebhooks = Some(mapOf(webhooks.size - 1 to webhook))))
        }

        fun onDiscordWebhookRemove(webhook: String) {
            val webhooks = discordWebhooks ?: return
            val removeIndex = webhooks.indexOf(webhook)
            if (removeIndex == -1) return
            discordWebhooks = webhooks.minus(webhook)
            update(MediaServerNotificationsUpdateRequest(discordWebhooks = Some(mapOf(removeIndex to null))))
        }

        fun onUseGlobalAppriseChange(useGlobal: Boolean) {
            appriseUrls = if (useGlobal) null else emptyList()
            update(MediaServerNotificationsUpdateRequest(appriseUrls = useGlobalPatch(useGlobal)))
        }

        fun onAppriseUrlAdd(url: String) {
            val urls = (appriseUrls ?: emptyList()).plus(url)
            appriseUrls = urls
            update(MediaServerNotificationsUpdateRequest(appriseUrls = Some(mapOf(urls.size - 1 to url))))
        }

        fun onAppriseUrlRemove(url: String) {
            val urls = appriseUrls ?: return
            val removeIndex = urls.indexOf(url)
            if (removeIndex == -1) return
            appriseUrls = urls.minus(url)
            update(MediaServerNotificationsUpdateRequest(appriseUrls = Some(mapOf(removeIndex to null))))
        }

        private fun useGlobalPatch(useGlobal: Boolean): PatchValue<Map<Int, String?>> =
            if (useGlobal) PatchValue.None else Some(emptyMap())

        private fun update(request: MediaServerNotificationsUpdateRequest) {
            coroutineScope.launch {
                appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(toRequest(request)) }
                    .onFailure { this@ServerNotificationsState.initialize(komfConfig.getConfig().first()) }
            }
        }
    }
}
