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

package network.voi.hera.ui.accounts

import androidx.fragment.app.viewModels
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.R
import network.voi.hera.models.AssetAction
import network.voi.hera.models.AssetTransaction
import network.voi.hera.modules.qrscanning.BaseQrScannerFragment
import network.voi.hera.modules.webexport.model.WebExportQrCode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountsQrScannerFragment : BaseQrScannerFragment(R.id.accountsQrScannerFragment) {

    private val accountsQrScannerViewModel: AccountsQrScannerViewModel by viewModels()

    override val shouldShowWcSessionsButton: Boolean
        get() = true

    @SuppressWarnings("MaxLineLength")
    override fun onAssetTransferWithNotOptInDeepLink(assetId: Long): Boolean {
        val assetAction = AssetAction(assetId)
        return true.also {
            nav(
                AccountsQrScannerFragmentDirections.actionAccountsQrScannerFragmentToUnsupportedAddAssetTryLaterBottomSheet(
                    assetAction
                )
            )
        }
    }

    @SuppressWarnings("MaxLineLength")
    override fun onAssetTransferDeepLink(assetTransaction: AssetTransaction): Boolean {
        return true.also {
            nav(AccountsQrScannerFragmentDirections.actionAccountsQrScannerFragmentToSendAlgoNavigation(assetTransaction))
        }
    }

    @SuppressWarnings("MaxLineLength")
    override fun onAccountAddressDeeplink(accountAddress: String, label: String?): Boolean {
        return true.also {
            nav(
                AccountsQrScannerFragmentDirections.actionAccountsQrScannerFragmentToAccountsAddressScanActionBottomSheet(
                    accountAddress,
                    label
                )
            )
        }
    }

    @SuppressWarnings("MaxLineLength")
    override fun onImportAccountDeepLink(mnemonic: String): Boolean {
        return true.also {
            if (accountsQrScannerViewModel.isAccountLimitExceed()) {
                showMaxAccountLimitExceededError()
                return@also
            }
            nav(
                AccountsQrScannerFragmentDirections.actionAccountsQrScannerFragmentToRecoverWithPassphraseNavigation(
                    mnemonic = mnemonic
                )
            )
        }
    }

    override fun onWebExportQrCodeDeepLink(webExportQrCode: WebExportQrCode): Boolean {
        return true.also {
            nav(
                HomeNavigationDirections.actionGlobalWebExportNavigation(
                    backupId = webExportQrCode.backupId,
                    modificationKey = webExportQrCode.modificationKey,
                    encryptionKey = webExportQrCode.encryptionKey
                )
            )
        }
    }

    override fun onAssetOptInDeepLink(assetAction: AssetAction): Boolean {
        return true.also {
            nav(
                AccountsQrScannerFragmentDirections.actionAccountsQrScannerFragmentToAddAssetAccountSelectionFragment(
                    assetId = assetAction.assetId
                )
            )
        }
    }

    override fun onWalletConnectConnectionDeeplink(wcUrl: String): Boolean {
        accountsQrScannerViewModel.logAccountsQrConnectEvent()
        handleWalletConnectUrl(wcUrl)
        return true.also { navBack() }
    }
}
