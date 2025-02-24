/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 *
 */

package com.algorand.android.models.builder

import com.algorand.android.R
import com.algorand.android.models.AnnotatedString
import com.algorand.android.models.BaseAppCallTransaction
import com.algorand.android.models.WalletConnectTransactionSummary
import javax.inject.Inject

class BaseAppCallTransactionSummaryUiBuilder @Inject constructor() :
    WalletConnectTransactionSummaryUIBuilder<BaseAppCallTransaction> {

    override fun buildTransactionSummary(txn: BaseAppCallTransaction): WalletConnectTransactionSummary {
        return with(txn) {
            val titleText = AnnotatedString(
                stringResId = appOnComplete.summaryTitle,
                replacementList = listOf("app_id" to appId.toString())
            )
            WalletConnectTransactionSummary(
                accountName = fromAccount?.name,
                accountIconResource = createAccountIconResource(),
                summaryTitle = titleText,
                showWarning = warningCount != null,
                showMoreButtonText = R.string.show_all_details
            )
        }
    }
}
