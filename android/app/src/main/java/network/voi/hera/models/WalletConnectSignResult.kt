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

package network.voi.hera.models

import android.content.Context
import androidx.annotation.StringRes
import network.voi.hera.R
import network.voi.hera.models.WalletConnectSignResult.Error.Defined
import network.voi.hera.utils.getXmlStyledString

sealed class WalletConnectSignResult {

    data class Success(
        val sessionId: Long,
        val requestId: Long,
        val signedTransaction: List<ByteArray?>
    ) : WalletConnectSignResult()

    sealed class Error(@StringRes val titleResId: Int) : WalletConnectSignResult() {
        fun getMessage(context: Context): Pair<String, CharSequence> {
            val title = context.getString(titleResId)
            return when (this) {
                is Defined -> Pair(title, context.getXmlStyledString(description))
                is Api -> Pair(title, errorMessage)
            }
        }

        class Defined(
            val description: AnnotatedString,
            @StringRes titleResId: Int = R.string.error_default_title
        ) : Error(titleResId)

        class Api(
            val errorMessage: String,
            @StringRes titleResId: Int = R.string.error_default_title
        ) : Error(titleResId)
    }

    data class TransactionCancelled(
        val error: Error = Defined(AnnotatedString(R.string.error_cancelled_message), R.string.error_cancelled_title)
    ) : WalletConnectSignResult()

    object Loading : WalletConnectSignResult()

    data class LedgerWaitingForApproval(
        val ledgerName: String?,
        val currentTransactionIndex: Int?,
        val totalTransactionCount: Int?,
        val isTransactionIndicatorVisible: Boolean
    ) : WalletConnectSignResult()

    object LedgerScanFailed : WalletConnectSignResult()
    object CanBeSigned : WalletConnectSignResult()
}
