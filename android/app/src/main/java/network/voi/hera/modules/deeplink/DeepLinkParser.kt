@file:Suppress("MaxLineLength", "TooManyFunctions")

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

package network.voi.hera.modules.deeplink

import android.net.Uri
import network.voi.hera.models.NotificationGroupType
import network.voi.hera.models.RawMnemonicPayload
import network.voi.hera.modules.deeplink.domain.model.RawDeepLink
import network.voi.hera.modules.webexport.model.WebExportQrCode
import network.voi.hera.utils.fromJson
import network.voi.hera.utils.isValidAddress
import com.squareup.moshi.Moshi
import java.math.BigInteger
import javax.inject.Inject

class DeepLinkParser @Inject constructor(
    private val moshi: Moshi
) {

    fun parseDeepLink(deepLink: String): RawDeepLink {
        val parsedUri = Uri.parse(deepLink)
        return RawDeepLink(
            accountAddress = getAccountAddress(parsedUri),
            walletConnectUrl = getWalletConnectUrl(parsedUri),
            assetId = getAssetId(parsedUri),
            amount = getAmount(parsedUri),
            note = getNote(parsedUri),
            xnote = getXnote(parsedUri),
            mnemonic = getMnemonic(parsedUri),
            label = getLabel(parsedUri),
            transactionId = getTransactionId(parsedUri),
            transactionStatus = getTransactionStatus(parsedUri),
            webExportQrCode = getWebExportData(parsedUri),
            notificationGroupType = getNotificationGroupType(parsedUri)
        )
    }

    private fun getXnote(parsedUri: Uri): String? {
        return parseQueryIfExist(XNOTE_QUERY_KEY, parsedUri)
    }

    private fun getNote(parsedUri: Uri): String? {
        return parseQueryIfExist(NOTE_QUERY_KEY, parsedUri)
    }

    private fun getAmount(parsedUri: Uri): BigInteger? {
        val amountAsString = parseQueryIfExist(AMOUNT_QUERY_KEY, parsedUri)
        return amountAsString?.toBigIntegerOrNull()
    }

    private fun getAssetId(parsedUri: Uri): Long? {
        val assetIdAsString = parseQueryIfExist(ASSET_ID_QUERY_KEY, parsedUri)
        return assetIdAsString?.toLongOrNull()
    }

    private fun getLabel(parsedUri: Uri): String? {
        return parseQueryIfExist(LABEL_QUERY_KEY, parsedUri)
    }

    private fun getTransactionId(parsedUri: Uri): String? {
        return parseQueryIfExist(TRANSACTION_ID_KEY, parsedUri)
    }

    private fun getTransactionStatus(parsedUri: Uri): String? {
        return parseQueryIfExist(TRANSACTION_STATUS_KEY, parsedUri)
    }

    private fun getAccountAddress(uri: Uri): String? {
        return with(uri) {
            if (isApplink(this)) {
                path?.split(PATH_SEPARATOR)?.firstOrNull { it.isValidAddress() }
            } else {
                parseQueryIfExist(ACCOUNT_ID_QUERY_KEY, this) ?: authority
            }.takeIf { it.isValidAddress() } ?: uri.toString().takeIf { it.isValidAddress() }
        }
    }

    private fun getWalletConnectUrl(uri: Uri): String? {
        return with(uri) {
            val parsedUrl = if (isApplink(this)) {
                val walletConnectUrl = toString().split(PERAWALLET_WC_AUTH_KEY).lastOrNull()
                walletConnectUrl?.removePrefix(PATH_SEPARATOR)
            } else {
                if (authority.isNullOrBlank()) {
                    uri.toString()
                } else {
                    removeAuthSeparator(schemeSpecificPart)
                }
            }
            parsedUrl.takeIf { it?.startsWith(WALLET_CONNECT_AUTH_KEY) == true }
        }
    }

    private fun getMnemonic(uri: Uri): String? {
        return try {
            moshi.fromJson<RawMnemonicPayload>(uri.toString())?.mnemonic
        } catch (exception: Exception) {
            null
        }
    }

    private fun getWebExportData(uri: Uri): WebExportQrCode? {
        return try {
            moshi.fromJson<WebExportQrCode>(uri.toString())
        } catch (exception: Exception) {
            null
        }
    }

    private fun getNotificationGroupType(uri: Uri): NotificationGroupType? {
        return with(uri) {
            when (authority + path) {
                NOTIFICATION_ACTION_ASSET_TRANSACTIONS -> NotificationGroupType.TRANSACTIONS
                NOTIFICATION_ACTION_ASSET_OPTIN -> NotificationGroupType.OPTIN
                else -> null
            }
        }
    }

    private fun parseQueryIfExist(queryKey: String, uri: Uri): String? {
        if (!uri.isHierarchical) return null
        val hasQueryKey = uri.queryParameterNames.contains(queryKey)
        return if (hasQueryKey) uri.getQueryParameter(queryKey) else null
    }

    private fun isApplink(uri: Uri): Boolean {
        return removeAuthSeparator(uri.schemeSpecificPart).startsWith(PERAWALLET_APPLINK_AUTH_KEY)
    }

    private fun removeAuthSeparator(uriString: String): String {
        return uriString.removePrefix(AUTH_SEPARATOR)
    }

    companion object {
        private const val PERAWALLET_APPLINK_AUTH_KEY = "perawallet.app"

        private const val PERAWALLET_WC_AUTH_KEY = "perawallet-wc"
        private const val WALLET_CONNECT_AUTH_KEY = "wc"

        private const val AMOUNT_QUERY_KEY = "amount"
        private const val ASSET_ID_QUERY_KEY = "asset"
        private const val ACCOUNT_ID_QUERY_KEY = "account"
        private const val NOTE_QUERY_KEY = "note"
        private const val XNOTE_QUERY_KEY = "xnote"
        private const val LABEL_QUERY_KEY = "label"
        private const val TRANSACTION_ID_KEY = "transactionId"
        private const val TRANSACTION_STATUS_KEY = "transactionStatus"
        private const val NOTIFICATION_ACTION_ASSET_TRANSACTIONS = "asset/transactions"
        private const val NOTIFICATION_ACTION_ASSET_OPTIN = "asset/opt-in"

        private const val AUTH_SEPARATOR = "//"
        private const val PATH_SEPARATOR = "/"
    }
}
