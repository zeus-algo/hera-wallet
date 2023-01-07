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

package com.algorand.android.utils

import android.content.Intent
import androidx.navigation.NavController
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.R
import com.algorand.android.models.Account
import com.algorand.android.models.AssetAction
import com.algorand.android.models.AssetInformation
import com.algorand.android.models.AssetTransaction
import com.algorand.android.models.DecodedQrCode
import com.algorand.android.models.NotificationType
import com.algorand.android.models.User
import com.algorand.android.modules.dapp.moonpay.domain.model.MoonpayTransactionStatus

const val SELECTED_ACCOUNT_KEY = "selectedAccountKey"
const val SELECTED_ASSET_ID_KEY = "selectedAssetIdKey"
const val ASSET_SUPPORT_REQUESTED_PUBLIC_KEY = "supportRequestedPublicKey"
const val ASSET_SUPPORT_REQUESTED_ASSET_KEY = "supportRequestedAsset"
const val DEEPLINK_AND_NAVIGATION_INTENT = "deeplinknavIntent"
const val WC_TRANSACTION_ID_INTENT_KEY = "wcTransactionId"
private const val NO_VALUE = -1L

private fun NavController.handleSelectedAssetNavigation(
    accountCacheManager: AccountCacheManager,
    selectedAccountKey: String,
    selectedAssetId: Long
) {
    val selectedAccountCacheData = accountCacheManager.getCacheData(selectedAccountKey)
    if (selectedAccountCacheData != null) {
        navigateSafe(
            HomeNavigationDirections.actionGlobalAssetProfileNavigation(
                assetId = selectedAssetId,
                accountAddress = selectedAccountCacheData.account.address
            )
        )
    }
}

fun NavController.handleDeeplink(
    decodedQrCode: DecodedQrCode,
    accountCacheManager: AccountCacheManager,
    onWalletConnectResult: ((String) -> Unit?)? = null
): Boolean {
    if (decodedQrCode is DecodedQrCode.Success.Deeplink) {
        when (decodedQrCode) {
            is DecodedQrCode.Success.Deeplink.WalletConnect -> {
                onWalletConnectResult?.invoke(decodedQrCode.walletConnectUrl)
            }
            is DecodedQrCode.Success.Deeplink.AssetTransaction -> {
                // If deeplink does not contain assetId then it should be Algo
                val assetId = decodedQrCode.getDecodedAssetID()

                val accountAssetPairList = accountCacheManager.getAccountCacheWithSpecificAsset(
                    assetId, listOf(Account.Type.WATCH)
                )

                if (accountAssetPairList.isEmpty()) {
                    val assetAction = AssetAction(assetId = assetId)
                    // No account owns this asset
                    navigateSafe(
                        HomeNavigationDirections.actionGlobalUnsupportedAddAssetTryLaterBottomSheet(assetAction)
                    )
                    return false
                }

                val assetTransaction = AssetTransaction(
                    assetId = assetId,
                    note = decodedQrCode.note, // normal note
                    xnote = decodedQrCode.xnote, // locked note
                    amount = decodedQrCode.amount,
                    receiverUser = User(
                        publicKey = decodedQrCode.address,
                        name = decodedQrCode.label ?: decodedQrCode.address,
                        imageUriAsString = null
                    )
                )
                navigateSafe(HomeNavigationDirections.actionGlobalSendAlgoNavigation(assetTransaction))
            }
            is DecodedQrCode.Success.Deeplink.MoonPayResult -> {
                navigateSafe(
                    HomeNavigationDirections.actionGlobalMoonpayResultNavigation(
                        walletAddress = decodedQrCode.address,
                        transactionStatus = MoonpayTransactionStatus.getByValueOrDefault(
                            decodedQrCode.transactionStatus.orEmpty()
                        )
                    )
                )
            }
            is DecodedQrCode.Success.Deeplink.AddContact -> {
                navigateSafe(
                    HomeNavigationDirections.actionGlobalContactAdditionNavigation(
                        contactName = decodedQrCode.contactName,
                        contactPublicKey = decodedQrCode.contactPublicKey
                    )
                )
            }
        }
        return true
    } else {
        return false
    }
}

fun NavController.handleIntent(
    intentToHandle: Intent,
    accountCacheManager: AccountCacheManager,
    onWalletConnectResult: (String) -> Unit,
    onIntentHandlingFailed: (Int) -> Unit
): Boolean {
    with(intentToHandle) {
        return when {
            dataString != null -> {
                val decodedDeeplink = decodeDeeplink(dataString) ?: return false
                handleDeeplink(decodedDeeplink, accountCacheManager, onWalletConnectResult)
            }
            else -> handleIntentWithBundle(this, accountCacheManager, onIntentHandlingFailed)
        }
    }
}

fun NavController.handleIntentWithBundle(
    intentToHandle: Intent,
    accountCacheManager: AccountCacheManager,
    onIntentHandlingFailed: (Int) -> Unit
): Boolean {
    with(intentToHandle) {
        // TODO change your architecture for the bug here. https://issuetracker.google.com/issues/37053389
        // This fixes the problem for now. Be careful when adding more than one parcelable.
        setExtrasClassLoader(AssetInformation::class.java.classLoader)

        val selectedAssetToOpen = getLongExtra(SELECTED_ASSET_ID_KEY, NO_VALUE)
        val selectedPublicKeyToOpen = getStringExtra(SELECTED_ACCOUNT_KEY)

        if (getLongExtra(WC_TRANSACTION_ID_INTENT_KEY, -1L) != -1L) {
            navigateSafe(HomeNavigationDirections.actionGlobalWalletConnectRequestNavigation())
            return true
        }

        if (!selectedPublicKeyToOpen.isNullOrBlank() && selectedAssetToOpen != NO_VALUE) {
            handleSelectedAssetNavigation(accountCacheManager, selectedPublicKeyToOpen, selectedAssetToOpen)
            return true
        }

        val assetSupportRequestedPublicKey = getStringExtra(ASSET_SUPPORT_REQUESTED_PUBLIC_KEY)
        val assetSupportRequestedAsset = getParcelableExtra<AssetInformation>(ASSET_SUPPORT_REQUESTED_ASSET_KEY)

        if (!assetSupportRequestedPublicKey.isNullOrBlank() && assetSupportRequestedAsset != null) {
            val assetAction = AssetAction(
                assetId = assetSupportRequestedAsset.assetId,
                publicKey = assetSupportRequestedPublicKey,
                asset = assetSupportRequestedAsset
            )
            val accountType = accountCacheManager.getCacheData(assetSupportRequestedPublicKey)?.account?.type
            val isAccountExist = accountCacheManager.getCacheData(assetSupportRequestedPublicKey) != null
            when {
                !isAccountExist -> onIntentHandlingFailed.invoke(R.string.you_cannot_take)
                !canSignTransaction(accountType) -> onIntentHandlingFailed.invoke(R.string.you_cannot_optin)
                else -> {
                    navigateSafe(
                        HomeNavigationDirections.actionGlobalUnsupportedAssetNotificationRequestActionBottomSheet(
                            assetAction
                        )
                    )
                }
            }
            return true
        }
    }
    return false
}

// TODO change according to notification type later on.
fun NavController.isNotificationCanBeShown(notificationType: NotificationType, isAppUnlocked: Boolean): Boolean {
    if (isAppUnlocked) {
        return true
    }
    return false
}
