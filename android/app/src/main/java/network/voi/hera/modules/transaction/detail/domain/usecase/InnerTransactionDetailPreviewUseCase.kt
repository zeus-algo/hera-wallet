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

package network.voi.hera.modules.transaction.detail.domain.usecase

import network.voi.hera.R
import network.voi.hera.models.AssetInformation
import network.voi.hera.modules.transaction.detail.domain.model.BaseTransactionDetail
import network.voi.hera.modules.transaction.detail.domain.model.TransactionSign
import network.voi.hera.modules.transaction.detail.ui.mapper.TransactionDetailItemMapper
import network.voi.hera.modules.transaction.detail.ui.mapper.TransactionDetailPreviewMapper
import network.voi.hera.modules.transaction.detail.ui.model.TransactionDetailItem
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.tooltip.domain.usecase.TransactionDetailTooltipDisplayPreferenceUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.GetActiveNodeUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.appendAssetName
import network.voi.hera.utils.formatAmount
import network.voi.hera.utils.formatAsAlgoAmount
import network.voi.hera.utils.toShortenedAddress
import javax.inject.Inject
import kotlinx.coroutines.flow.flow

@SuppressWarnings("LongParameterList")
class InnerTransactionDetailPreviewUseCase @Inject constructor(
    private val transactionDetailPreviewMapper: TransactionDetailPreviewMapper,
    private val transactionDetailItemMapper: TransactionDetailItemMapper,
    private val peekInnerTransactionFromCacheUseCase: PeekInnerTransactionFromCacheUseCase,
    private val popInnerTransactionFromStackCacheUseCase: PopInnerTransactionFromStackCacheUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    assetDetailUseCase: SimpleAssetDetailUseCase,
    collectibleUseCase: SimpleCollectibleUseCase,
    getActiveNodeUseCase: GetActiveNodeUseCase,
    transactionDetailTooltipDisplayPreferenceUseCase: TransactionDetailTooltipDisplayPreferenceUseCase,
    clearInnerTransactionStackCacheUseCase: ClearInnerTransactionStackCacheUseCase
) : BaseTransactionDetailPreviewUseCase(
    assetDetailUseCase = assetDetailUseCase,
    collectibleUseCase = collectibleUseCase,
    transactionDetailItemMapper = transactionDetailItemMapper,
    getActiveNodeUseCase = getActiveNodeUseCase,
    transactionDetailTooltipDisplayPreferenceUseCase = transactionDetailTooltipDisplayPreferenceUseCase,
    clearInnerTransactionStackCacheUseCase = clearInnerTransactionStackCacheUseCase
) {

    suspend fun peekInnerTransactionFromCache(): List<BaseTransactionDetail> {
        return peekInnerTransactionFromCacheUseCase.peekInnerTransactionFromCacheUseCase() ?: emptyList()
    }

    suspend fun popInnerTransactionFromStackCache() {
        popInnerTransactionFromStackCacheUseCase.popInnerTransactionFromStackCache()
    }

    suspend fun getTransactionDetailPreview(publicKey: String, transactions: List<BaseTransactionDetail>) = flow {
        val transactionDetailItemList = mutableListOf<TransactionDetailItem>().apply {
            transactions.forEach { baseTransactionDetail ->
                when (baseTransactionDetail) {
                    is BaseTransactionDetail.ApplicationCallTransaction -> {
                        createApplicationCallTransactionItem(baseTransactionDetail)
                    }
                    is BaseTransactionDetail.AssetConfigurationTransaction,
                    is BaseTransactionDetail.AssetTransferTransaction,
                    is BaseTransactionDetail.PaymentTransaction,
                    is BaseTransactionDetail.UndefinedTransaction -> {
                        createStandardTransactionItem(transaction = baseTransactionDetail, publicKey = publicKey)
                    }
                }.apply { add(this) }
            }
            val innerTransactionTitleItem = transactionDetailItemMapper.mapToInnerTransactionTitleItem(
                innerTransactionCount = transactions.count()
            )
            add(TITLE_ITEM_INDEX, innerTransactionTitleItem)
        }
        emit(
            transactionDetailPreviewMapper.mapTo(
                isLoading = false,
                transactionDetailItemList = transactionDetailItemList,
                toolbarTitleResId = R.string.inner_transactions
            )
        )
    }

    private fun createApplicationCallTransactionItem(
        transaction: BaseTransactionDetail.ApplicationCallTransaction
    ): TransactionDetailItem.InnerTransactionItem.ApplicationInnerTransactionItem {
        return transactionDetailItemMapper.mapToApplicationInnerTransactionItem(
            accountAddress = transaction.senderAccountAddress.toShortenedAddress(),
            transactionSign = TransactionSign.POSITIVE,
            innerTransactionCount = transaction.innerTransactionCount,
            transaction = transaction
        )
    }

    private fun createStandardTransactionItem(
        transaction: BaseTransactionDetail,
        publicKey: String
    ): TransactionDetailItem.InnerTransactionItem.StandardInnerTransactionItem {
        val transactionAmount = getTransactionDetailAmount(transaction, true)
        val assetId = getTransactionAssetId(transaction)
        val assetDetail = getAssetDetail(assetId)
        val assetDecimal = assetDetail?.fractionDecimals ?: DEFAULT_ASSET_DECIMAL
        val assetName = AssetName.createShortName(assetDetail?.shortName)
        val isAlgo = assetId == AssetInformation.ALGO_ID
        val formattedTransactionAmount = with(transactionAmount.formatAmount(assetDecimal)) {
            if (isAlgo) formatAsAlgoAmount() else appendAssetName(assetName)
        }

        val receiverAccountPublicKey = transaction.receiverAccountAddress.orEmpty()
        val senderAccountPublicKey = transaction.senderAccountAddress.orEmpty()

        val areAccountsInCache = accountDetailUseCase.isThereAnyAccountWithPublicKey(senderAccountPublicKey) ||
            accountDetailUseCase.isThereAnyAccountWithPublicKey(receiverAccountPublicKey)

        return transactionDetailItemMapper.mapToStandardInnerTransactionItem(
            accountAddress = transaction.senderAccountAddress.toShortenedAddress(),
            transactionSign = getTransactionSign(
                receiverAccountPublicKey = transaction.receiverAccountAddress.orEmpty(),
                senderAccountPublicKey = transaction.senderAccountAddress.orEmpty(),
                publicKey = publicKey,
                closeToAccountAddress = transaction.closeToAccountAddress,
                areAccountsInCache = areAccountsInCache
            ),
            transactionAmount = transactionAmount,
            formattedTransactionAmount = formattedTransactionAmount,
            transaction = transaction
        )
    }

    companion object {
        private const val TITLE_ITEM_INDEX = 0
    }
}
