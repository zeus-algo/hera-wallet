/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package network.voi.hera.modules.deeplink.ui

import network.voi.hera.models.AssetAction
import network.voi.hera.models.AssetInformation
import network.voi.hera.models.AssetTransaction
import network.voi.hera.models.NotificationGroupType
import network.voi.hera.models.User
import network.voi.hera.modules.deeplink.DeepLinkParser
import network.voi.hera.modules.deeplink.domain.model.BaseDeepLink
import network.voi.hera.modules.deeplink.domain.model.BaseDeepLink.WalletConnectConnectionDeepLink
import network.voi.hera.modules.webexport.model.WebExportQrCode
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.toShortenedAddress
import javax.inject.Inject

class DeeplinkHandler @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val deepLinkParser: DeepLinkParser
) {

    private var listener: Listener? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun handleDeepLink(uri: String) {
        val rawDeepLink = deepLinkParser.parseDeepLink(uri)
        val parsedDeepLink = BaseDeepLink.create(rawDeepLink)
        handleDeeplink(parsedDeepLink)
    }

    private fun handleDeeplink(baseDeeplink: BaseDeepLink) {
        val isDeeplinkHandled: Boolean = when (baseDeeplink) {
            is BaseDeepLink.AccountAddressDeepLink -> handleAccountAddressDeepLink(baseDeeplink)
            is BaseDeepLink.AssetTransferDeepLink -> handleAssetTransferDeepLink(baseDeeplink)
            is BaseDeepLink.AssetOptInDeepLink -> handleAssetOptInDeepLink(baseDeeplink.assetId)
            is BaseDeepLink.MnemonicDeepLink -> handleMnemonicDeepLink(baseDeeplink)
            is WalletConnectConnectionDeepLink -> handleWalletConnectConnectionDeepLink(baseDeeplink)
            is BaseDeepLink.UndefinedDeepLink -> handleUndefinedDeepLink(baseDeeplink)
            is BaseDeepLink.MoonpayResultDeepLink -> handleMoonpayResultDeepLink(baseDeeplink)
            is BaseDeepLink.WebExportQrCodeDeepLink -> handleWebExportQrCodeDeepLink(baseDeeplink)
            is BaseDeepLink.NotificationDeepLink -> handleNotificationDeepLink(baseDeeplink)
        }
        if (!isDeeplinkHandled) listener?.onDeepLinkNotHandled(baseDeeplink)
    }

    private fun handleAccountAddressDeepLink(deepLink: BaseDeepLink.AccountAddressDeepLink): Boolean {
        return triggerListener { it.onAccountAddressDeeplink(deepLink.accountAddress, deepLink.label) }
    }

    private fun handleAssetOptInDeepLink(assetId: Long): Boolean {
        val assetAction = AssetAction(assetId = assetId)
        return triggerListener { it.onAssetOptInDeepLink(assetAction) }
    }

    private fun handleMnemonicDeepLink(mnemonicDeeplink: BaseDeepLink.MnemonicDeepLink): Boolean {
        return triggerListener { it.onImportAccountDeepLink(mnemonicDeeplink.mnemonic) }
    }

    private fun handleWalletConnectConnectionDeepLink(wcConnectionDeeplink: WalletConnectConnectionDeepLink): Boolean {
        return triggerListener {
            it.onWalletConnectConnectionDeeplink(wcUrl = wcConnectionDeeplink.url)
        }
    }

    private fun handleUndefinedDeepLink(undefinedDeeplink: BaseDeepLink.UndefinedDeepLink): Boolean {
        return triggerListener { it.onUndefinedDeepLink(undefinedDeeplink); true }
    }

    private fun handleMoonpayResultDeepLink(moonpayResultDeepLink: BaseDeepLink.MoonpayResultDeepLink): Boolean {
        return triggerListener {
            it.onMoonpayResultDeepLink(
                accountAddress = moonpayResultDeepLink.accountAddress,
                txnStatus = moonpayResultDeepLink.transactionStatus,
                txnId = moonpayResultDeepLink.transactionId
            )
        }
    }

    private fun handleWebExportQrCodeDeepLink(webExportQrCodeDeepLink: BaseDeepLink.WebExportQrCodeDeepLink): Boolean {
        return triggerListener {
            it.onWebExportQrCodeDeepLink(
                webExportQrCode = webExportQrCodeDeepLink.webExportQrCode
            )
        }
    }

    private fun handleAssetTransferDeepLink(assetTransferDeeplink: BaseDeepLink.AssetTransferDeepLink): Boolean {
        val assetId = assetTransferDeeplink.assetId
        val isAssetOwnedByAnyAccount = if (assetId == AssetInformation.ALGO_ID) {
            true
        } else {
            accountDetailUseCase.isAssetOwnedByAnyAccount(assetTransferDeeplink.assetId)
        }
        return if (isAssetOwnedByAnyAccount) {
            with(assetTransferDeeplink) {
                val assetTransaction = AssetTransaction(
                    assetId = assetId,
                    note = note, // normal note
                    xnote = xnote, // locked note
                    amount = amount,
                    receiverUser = User(
                        publicKey = receiverAccountAddress,
                        name = label ?: receiverAccountAddress.toShortenedAddress(),
                        imageUriAsString = null
                    )
                )
                triggerListener { it.onAssetTransferDeepLink(assetTransaction) }
            }
        } else {
            triggerListener { it.onAssetTransferWithNotOptInDeepLink(assetId) }
        }
    }

    private fun handleNotificationDeepLink(notificationDeepLink: BaseDeepLink.NotificationDeepLink): Boolean {
        return triggerListener {
            it.onNotificationDeepLink(
                accountAddress = notificationDeepLink.address,
                assetId = notificationDeepLink.assetId,
                notificationGroupType = notificationDeepLink.notificationGroupType
            )
        }
    }

    private fun triggerListener(action: (Listener) -> Boolean): Boolean {
        return listener?.run(action) ?: false
    }

    interface Listener {
        fun onAssetTransferDeepLink(assetTransaction: AssetTransaction): Boolean = false
        fun onAssetOptInDeepLink(assetAction: AssetAction): Boolean = false
        fun onImportAccountDeepLink(mnemonic: String): Boolean = false
        fun onAccountAddressDeeplink(accountAddress: String, label: String?): Boolean = false
        fun onWalletConnectConnectionDeeplink(wcUrl: String): Boolean = false
        fun onAssetTransferWithNotOptInDeepLink(assetId: Long): Boolean = false
        fun onMoonpayResultDeepLink(accountAddress: String, txnStatus: String, txnId: String?): Boolean = false
        fun onWebExportQrCodeDeepLink(webExportQrCode: WebExportQrCode): Boolean = false
        fun onNotificationDeepLink(
            accountAddress: String,
            assetId: Long,
            notificationGroupType: NotificationGroupType
        ): Boolean = false
        fun onUndefinedDeepLink(undefinedDeeplink: BaseDeepLink.UndefinedDeepLink)
        fun onDeepLinkNotHandled(deepLink: BaseDeepLink)
    }
}
