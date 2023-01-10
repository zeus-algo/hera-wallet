package network.voi.hera.modules.walletconnectfallbackbrowser.ui.mapper

import network.voi.hera.modules.walletconnectfallbackbrowser.ui.decider.FallbackBrowserItemIconResIdDecider
import network.voi.hera.modules.walletconnectfallbackbrowser.ui.decider.FallbackBrowserItemNameDecider
import network.voi.hera.modules.walletconnectfallbackbrowser.ui.model.FallbackBrowserListItem
import network.voi.hera.modules.walletconnectfallbackbrowser.domain.model.WalletConnectFallbackBrowser
import javax.inject.Inject

class WalletConnectFallbackBrowserItemMapper @Inject constructor(
    private val fallbackBrowserItemIconResIdDecider: FallbackBrowserItemIconResIdDecider,
    private val fallbackBrowserItemNameDecider: FallbackBrowserItemNameDecider
) {

    fun mapTo(walletConnectFallbackBrowser: WalletConnectFallbackBrowser): FallbackBrowserListItem {
        return FallbackBrowserListItem(
            iconDrawableResId = fallbackBrowserItemIconResIdDecider.provideFallbackBrowserItemIconResId(
                walletConnectFallbackBrowser
            ),
            nameStringResId = fallbackBrowserItemNameDecider.provideFallbackBrowserItemNameResId(
                walletConnectFallbackBrowser
            ),
            packageName = walletConnectFallbackBrowser.packageName
        )
    }
}
