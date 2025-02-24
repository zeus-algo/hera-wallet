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

package com.algorand.android.models

import androidx.room.Embedded
import androidx.room.Relation
import com.algorand.android.models.WalletConnectSessionAccountEntity.Companion.WALLET_CONNECT_SESSION_ACCOUNT_TABLE_SESSION_ID_COLUMN_NAME
import com.algorand.android.models.WalletConnectSessionEntity.Companion.WALLET_CONNECT_SESSION_TABLE_SESSION_ID_COLUMN_NAME

data class WalletConnectSessionWithAccountsAddresses(
    @Embedded
    val walletConnectSessions: WalletConnectSessionEntity,
    @Relation(
        parentColumn = WALLET_CONNECT_SESSION_TABLE_SESSION_ID_COLUMN_NAME,
        entityColumn = WALLET_CONNECT_SESSION_ACCOUNT_TABLE_SESSION_ID_COLUMN_NAME
    )
    val walletConnectSessionAccounts: List<WalletConnectSessionAccountEntity>
)
