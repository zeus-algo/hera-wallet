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

package network.voi.hera.modules.transaction.csv.domain.model

data class TransactionCsvDetail(
    val transactionId: String,
    val formattedAmount: String,
    val formattedFee: String,
    val closeAmount: String,
    val closeToAddress: String,
    val receiverAddress: String,
    val senderAddress: String,
    val confirmedRound: String,
    val formattedDate: String,
    val noteAsString: String,
    val assetId: Long?
)
