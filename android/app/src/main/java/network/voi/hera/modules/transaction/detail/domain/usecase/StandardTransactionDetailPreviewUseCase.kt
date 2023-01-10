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
import network.voi.hera.modules.transaction.detail.domain.model.TransactionDetailPreview
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
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@SuppressWarnings("LongParameterList")
class StandardTransactionDetailPreviewUseCase @Inject constructor(
    private val getTransactionDetailUseCase: GetTransactionDetailUseCase,
    private val getTransactionDetailAccountUseCase: GetTransactionDetailAccountUseCase,
    private val transactionDetailItemMapper: TransactionDetailItemMapper,
    private val transactionDetailTooltipDisplayPreferenceUseCase: TransactionDetailTooltipDisplayPreferenceUseCase,
    private val transactionDetailPreviewMapper: TransactionDetailPreviewMapper,
    private val accountDetailUseCase: AccountDetailUseCase,
    getActiveNodeUseCase: GetActiveNodeUseCase,
    assetDetailUseCase: SimpleAssetDetailUseCase,
    collectibleUseCase: SimpleCollectibleUseCase,
    clearInnerTransactionStackCacheUseCase: ClearInnerTransactionStackCacheUseCase
) : BaseTransactionDetailPreviewUseCase(
    assetDetailUseCase = assetDetailUseCase,
    collectibleUseCase = collectibleUseCase,
    transactionDetailItemMapper = transactionDetailItemMapper,
    getActiveNodeUseCase = getActiveNodeUseCase,
    transactionDetailTooltipDisplayPreferenceUseCase = transactionDetailTooltipDisplayPreferenceUseCase,
    clearInnerTransactionStackCacheUseCase = clearInnerTransactionStackCacheUseCase
) {

    suspend fun getTransactionDetailPreview(
        transactionId: String,
        publicKey: String,
        isInnerTransaction: Boolean
    ) = flow {
        emit(transactionDetailPreviewMapper.mapTo(isLoading = true, transactionDetailItemList = emptyList()))
        getTransactionDetailUseCase.getTransactionDetail(transactionId).collect { transactionDetailResource ->
            transactionDetailResource.useSuspended(
                onSuccess = { baseTransactionDetail ->
                    val transactionDetailPreview = createTransactionDetailListItems(
                        baseTransactionDetail = baseTransactionDetail,
                        publicKey = publicKey,
                        transactionId = transactionId,
                        isInnerTransaction = isInnerTransaction
                    )
                    emit(transactionDetailPreview)
                },
                onFailed = {
                    // TODO: Currently, we don't have a design for this case. We should handle error cases after
                    //  preparing the design for this case.
                }
            )
        }
    }

    @SuppressWarnings("LongMethod")
    suspend fun createTransactionDetailListItems(
        baseTransactionDetail: BaseTransactionDetail,
        publicKey: String,
        transactionId: String,
        isInnerTransaction: Boolean
    ): TransactionDetailPreview {
        val assetId = getTransactionAssetId(baseTransactionDetail)
        val assetDetail = getAssetDetail(assetId)
        val assetDecimal = assetDetail?.fractionDecimals ?: DEFAULT_ASSET_DECIMAL
        val assetName = AssetName.createShortName(assetDetail?.shortName)
        val isAlgo = assetId == AssetInformation.ALGO_ID

        val transactionAmount = getTransactionDetailAmount(baseTransactionDetail, false)

        val receiverAccountPublicKey = baseTransactionDetail.receiverAccountAddress.orEmpty()
        val senderAccountPublicKey = baseTransactionDetail.senderAccountAddress.orEmpty()

        val areAccountsInCache = accountDetailUseCase.isThereAnyAccountWithPublicKey(senderAccountPublicKey) ||
            accountDetailUseCase.isThereAnyAccountWithPublicKey(receiverAccountPublicKey)

        val transactionSign = getTransactionSign(
            receiverAccountPublicKey = receiverAccountPublicKey,
            senderAccountPublicKey = senderAccountPublicKey,
            publicKey = publicKey,
            closeToAccountAddress = baseTransactionDetail.closeToAccountAddress,
            areAccountsInCache = areAccountsInCache
        )

        val isCloseTo = isTransactionCloseTo(baseTransactionDetail)

        val shouldShowCopyAddressTip = transactionDetailTooltipDisplayPreferenceUseCase.shouldShowCopyAddressTip()

        val transactionDetailItemList = mutableListOf<TransactionDetailItem>().apply {
            add(
                createTransactionAmount(
                    transactionSign = transactionSign,
                    transactionAmount = transactionAmount,
                    assetName = assetName,
                    assetDecimal = assetDecimal,
                    isAlgo = isAlgo
                )
            )

            if (isCloseTo) {
                val transactionFullAmount = getTransactionDetailAmount(baseTransactionDetail, true)
                add(
                    createTransactionCloseToAmountItem(
                        transactionSign = transactionSign,
                        transactionFullAmount = transactionFullAmount,
                        assetName = assetName,
                        assetDecimal = assetDecimal,
                        isAlgo = isAlgo
                    )
                )
            }

            if (baseTransactionDetail is BaseTransactionDetail.AssetConfigurationTransaction) {
                add(createTransactionAssetInformationItem(assetId, baseTransactionDetail))
            }

            add(createTransactionStatusItem())

            add(TransactionDetailItem.DividerItem)
            add(
                getTransactionDetailAccountUseCase.getTransactionFromAccount(
                    senderAccountPublicKey,
                    shouldShowCopyAddressTip
                )
            )
            if (receiverAccountPublicKey.isNotBlank()) {
                add(getTransactionDetailAccountUseCase.getTransactionToAccount(receiverAccountPublicKey))
            }

            val closeToAddress = baseTransactionDetail.closeToAccountAddress
            if (isCloseTo && !closeToAddress.isNullOrBlank()) {
                add(getTransactionDetailAccountUseCase.getTransactionCloseToAccount(closeToAddress))
            }
            add(createTransactionFeeItem(baseTransactionDetail.fee))
            add(
                transactionDetailItemMapper.mapToDateItem(
                    labelTextRes = R.string.date,
                    date = getTransactionFormattedDate(baseTransactionDetail.roundTimeAsTimestamp)
                )
            )
            add(
                transactionDetailItemMapper.mapToTransactionIdItem(
                    labelTextRes = getRequiredTransactionIdLabelTextResId(isInnerTransaction = isInnerTransaction),
                    transactionId = transactionId
                )
            )
            add(TransactionDetailItem.DividerItem)

            addNoteIfExist(this, baseTransactionDetail.noteInBase64)
            add(createTransactionChipGroupItem(transactionId))
        }

        return transactionDetailPreviewMapper.mapTo(
            isLoading = false,
            transactionDetailItemList = transactionDetailItemList,
            baseTransactionDetail.toolbarTitleResId
        )
    }

    private fun createTransactionAssetInformationItem(
        assetId: Long,
        transactionDetail: BaseTransactionDetail.AssetConfigurationTransaction
    ): TransactionDetailItem.StandardTransactionItem.AssetInformationItem {
        return transactionDetailItemMapper.mapToAssetInformationItem(
            assetFullName = AssetName.create(transactionDetail.name),
            assetShortName = AssetName.createShortName(transactionDetail.unitName),
            assetId = assetId
        )
    }
}
