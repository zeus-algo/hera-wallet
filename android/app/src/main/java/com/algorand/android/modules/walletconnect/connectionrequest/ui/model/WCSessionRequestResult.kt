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

package com.algorand.android.modules.walletconnect.connectionrequest.ui.model

import android.os.Parcelable
import com.algorand.android.models.WalletConnectSession
import kotlinx.parcelize.Parcelize

sealed class WCSessionRequestResult {

    abstract val wcSessionRequest: WalletConnectSession

    @Parcelize
    data class ApproveRequest(
        override val wcSessionRequest: WalletConnectSession,
        val accountAddresses: List<String>
    ) : WCSessionRequestResult(), Parcelable

    @Parcelize
    data class RejectRequest(
        override val wcSessionRequest: WalletConnectSession
    ) : WCSessionRequestResult(), Parcelable
}
