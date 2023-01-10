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

package network.voi.hera.usecase

import network.voi.hera.models.AccountCacheData
import network.voi.hera.models.AssetInformation
import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.AccountCacheManager
import network.voi.hera.utils.formatAmount
import javax.inject.Inject

class AssetTransferAmountUseCase @Inject constructor(
    private val accountCacheManager: AccountCacheManager,
    private val transactionTipsUseCase: TransactionTipsUseCase,
    private val getBaseOwnedAssetDataUseCase: GetBaseOwnedAssetDataUseCase
) {

    fun getAssetInformation(publicKey: String, assetId: Long): AssetInformation? {
        return accountCacheManager.getAssetInformation(publicKey, assetId)
    }

    fun getAccountInformation(publicKey: String): AccountCacheData? {
        return accountCacheManager.getCacheData(publicKey)
    }

    fun shouldShowTransactionTips(): Boolean {
        return transactionTipsUseCase.shouldShowTransactionTips()
    }

    fun getMaximumAmountOfAsset(assetId: Long, publicKey: String): String {
        val assetDetail = getAccountAssetData(assetId, publicKey)
        return if (assetDetail?.isAlgo == true) {
            assetDetail.amount.formatAmount(ALGO_DECIMALS)
        } else {
            assetDetail?.formattedAmount.orEmpty()
        }
    }

    private fun getAccountAssetData(assetId: Long, publicKey: String): BaseAccountAssetData.BaseOwnedAssetData? {
        return getBaseOwnedAssetDataUseCase.getBaseOwnedAssetData(assetId, publicKey)
    }
}
