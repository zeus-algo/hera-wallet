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

package network.voi.hera.modules.transactionhistory.ui.usecase

import network.voi.hera.R
import network.voi.hera.decider.TransactionUserUseCase
import network.voi.hera.models.BaseAssetDetail
import network.voi.hera.modules.transactionhistory.domain.model.BaseTransaction
import network.voi.hera.modules.transactionhistory.domain.model.BaseTransaction.Transaction.AssetTransfer
import network.voi.hera.modules.transactionhistory.domain.model.BaseTransaction.Transaction.Pay
import network.voi.hera.modules.transactionhistory.ui.mapper.TransactionItemMapper
import network.voi.hera.modules.transactionhistory.ui.model.BaseTransactionItem
import network.voi.hera.modules.transactionhistory.ui.model.BaseTransactionItem.TransactionItem.PayItem.PayReceiveItem
import network.voi.hera.modules.transactionhistory.ui.model.BaseTransactionItem.TransactionItem.PayItem.PaySelfItem
import network.voi.hera.modules.transactionhistory.ui.model.BaseTransactionItem.TransactionItem.PayItem.PaySendItem
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.NEGATIVE_SIGN
import network.voi.hera.utils.POSITIVE_SIGN
import network.voi.hera.utils.formatAmount
import network.voi.hera.utils.formatAsAlgoAmount
import network.voi.hera.utils.formatAsAssetAmount
import network.voi.hera.utils.toShortenedAddress

open class BaseTransactionPreviewUseCase constructor(
    private val transactionItemMapper: TransactionItemMapper,
    private val transactionUserUseCase: TransactionUserUseCase,
    private val collectibleUseCase: SimpleCollectibleUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase
) {

    suspend fun createBaseTransactionItem(
        txn: BaseTransaction,
        publicKey: String
    ): BaseTransactionItem {
        return when (txn) {
            is Pay.Send -> createPayTransactionSendItem(txn, publicKey)
            is Pay.Receive -> createPayTransactionReceiveItem(txn, publicKey)
            is Pay.Self -> createPayTransactionSelfItem(txn, publicKey)
            is AssetTransfer.BaseSend.Send -> createAssetTransferSendItem(txn, publicKey)
            is AssetTransfer.BaseSend.SendOptOut -> createAssetTransferSendOptOutItem(txn, publicKey)
            is AssetTransfer.BaseReceive.Receive -> createAssetTransferReceiveItem(txn, publicKey)
            is AssetTransfer.BaseReceive.ReceiveOptOut -> createAssetTransferReceiveOptOutItem(txn, publicKey)
            is AssetTransfer.OptOut -> createAssetTransferOptOutItem(txn)
            is AssetTransfer.BaseSelf.Self -> createAssetTransferSelfItem(txn, publicKey)
            is AssetTransfer.BaseSelf.SelfOptIn -> createAssetTransferSelfOptInItem(txn, publicKey)
            is BaseTransaction.Transaction.AssetConfiguration -> transactionItemMapper.mapToAssetConfigurationItem(txn)
            is BaseTransaction.Transaction.ApplicationCall -> transactionItemMapper.mapToApplicationCallItem(txn)
            is BaseTransaction.Transaction.Undefined -> transactionItemMapper.mapToUndefinedItem(txn)
            is BaseTransaction.TransactionDateTitle -> transactionItemMapper.mapToTransactionDateTitle(txn)
            is BaseTransaction.PendingTransactionTitle -> transactionItemMapper.mapToPendingTransactionTitle(txn)
        }
    }

    private fun getAssetDetail(assetId: Long?): BaseAssetDetail? {
        if (assetId == null) return null
        return simpleAssetDetailUseCase.getCachedAssetDetail(assetId)?.data
            ?: collectibleUseCase.getCachedCollectibleById(assetId)?.data
    }

    private fun getAssetDecimals(transaction: AssetTransfer): Int {
        return getAssetDetail(transaction.assetId)?.fractionDecimals ?: DEFAULT_ASSET_DECIMAL
    }

    private fun getAssetShortName(transaction: AssetTransfer): String {
        return getAssetDetail(transaction.assetId)?.shortName.orEmpty()
    }

    private fun getTransactionSign(transaction: BaseTransaction.Transaction): String? {
        return if (transaction is Pay.Send || transaction is AssetTransfer.BaseSend) {
            if (isSenderAndReceiverSame(transaction)) null else NEGATIVE_SIGN
        } else if (transaction is Pay.Receive || transaction is AssetTransfer.BaseReceive) {
            if (isSenderAndReceiverSame(transaction)) null else POSITIVE_SIGN
        } else {
            null
        }
    }

    private fun getTransactionColorRes(transaction: BaseTransaction.Transaction): Int? {
        return if (transaction is Pay.Send || transaction is AssetTransfer.BaseSend) {
            if (isSenderAndReceiverSame(transaction)) null else R.color.transaction_amount_negative_color
        } else if (transaction is Pay.Receive || transaction is AssetTransfer.BaseReceive) {
            if (isSenderAndReceiverSame(transaction)) null else R.color.transaction_amount_positive_color
        } else {
            null
        }
    }

    private fun isSenderAndReceiverSame(transaction: BaseTransaction): Boolean {
        return if (transaction is BaseTransaction.Transaction) {
            transaction.senderAddress == transaction.receiverAddress
        } else {
            false
        }
    }

    private suspend fun getTransactionTargetUserDisplayName(
        transaction: BaseTransaction.Transaction,
        publicKey: String
    ): String? {
        with(transaction) {
            return if (receiverAddress == publicKey && publicKey == senderAddress) {
                null
            } else {
                val otherPublicKey = if (receiverAddress == publicKey) senderAddress else receiverAddress
                otherPublicKey?.let { transactionUserUseCase.getTransactionTargetUser(otherPublicKey).displayName }
            }
        }
    }

    private suspend fun createPayTransactionSendItem(
        txn: Pay.Send,
        publicKey: String
    ): PaySendItem {
        return transactionItemMapper.mapToPayTransactionSendItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = ALGO_DECIMALS, isCompact = true)
                .formatAsAlgoAmount(getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createPayTransactionReceiveItem(
        txn: Pay.Receive,
        publicKey: String
    ): PayReceiveItem {
        return transactionItemMapper.mapToPayTransactionReceiveItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = ALGO_DECIMALS, isCompact = true)
                .formatAsAlgoAmount(getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createPayTransactionSelfItem(
        txn: Pay.Self,
        publicKey: String
    ): PaySelfItem {
        return transactionItemMapper.mapToPayTransactionSelfItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = ALGO_DECIMALS, isCompact = true)
                .formatAsAlgoAmount(getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createAssetTransferSendItem(
        txn: AssetTransfer.BaseSend.Send,
        publicKey: String
    ): BaseTransactionItem.TransactionItem.AssetTransferItem.BaseAssetSendItem.AssetSendItem {
        return transactionItemMapper.mapToAssetTransactionSendItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = getAssetDecimals(txn), isCompact = true)
                .formatAsAssetAmount(getAssetShortName(txn), getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createAssetTransferSendOptOutItem(
        txn: AssetTransfer.BaseSend.SendOptOut,
        publicKey: String
    ): BaseTransactionItem.TransactionItem.AssetTransferItem.BaseAssetSendItem.AssetSendOptOutItem {
        return transactionItemMapper.mapToAssetTransactionSendOptOutItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = getAssetDecimals(txn), isCompact = true)
                .formatAsAssetAmount(getAssetShortName(txn), getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createAssetTransferReceiveItem(
        txn: AssetTransfer.BaseReceive.Receive,
        publicKey: String
    ): BaseTransactionItem.TransactionItem.AssetTransferItem.BaseReceiveItem.AssetReceiveItem {
        return transactionItemMapper.mapToAssetTransactionReceiveItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = getAssetDecimals(txn), isCompact = true)
                .formatAsAssetAmount(getAssetShortName(txn), getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createAssetTransferReceiveOptOutItem(
        txn: AssetTransfer.BaseReceive.ReceiveOptOut,
        publicKey: String
    ): BaseTransactionItem.TransactionItem.AssetTransferItem.BaseReceiveItem.AssetReceiveOptOutItem {
        return transactionItemMapper.mapToAssetTransactionReceiveOptOutItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = getAssetDecimals(txn), isCompact = true)
                .formatAsAssetAmount(getAssetShortName(txn), getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private fun createAssetTransferOptOutItem(
        txn: AssetTransfer.OptOut
    ): BaseTransactionItem.TransactionItem.AssetTransferItem.AssetOptOutItem {
        return transactionItemMapper.mapToAssetTransactionOptOutItem(
            transaction = txn,
            description = txn.closeToAddress.toShortenedAddress(),
            formattedAmount = txn.amount
                .formatAmount(decimals = getAssetDecimals(txn), isCompact = true)
                .formatAsAssetAmount(getAssetShortName(txn), getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createAssetTransferSelfItem(
        txn: AssetTransfer.BaseSelf.Self,
        publicKey: String
    ): BaseTransactionItem.TransactionItem.AssetTransferItem.BaseSelfItem.AssetSelfItem {
        return transactionItemMapper.mapToAssetTransactionSelfItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = getAssetDecimals(txn), isCompact = true)
                .formatAsAssetAmount(getAssetShortName(txn), getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }

    private suspend fun createAssetTransferSelfOptInItem(
        txn: AssetTransfer.BaseSelf.SelfOptIn,
        publicKey: String
    ): BaseTransactionItem.TransactionItem.AssetTransferItem.BaseSelfItem.AssetSelfOptInItem {
        return transactionItemMapper.mapToAssetTransactionSelfOptInItem(
            transaction = txn,
            description = getTransactionTargetUserDisplayName(txn, publicKey),
            formattedAmount = txn.amount
                .formatAmount(decimals = getAssetDecimals(txn), isCompact = true)
                .formatAsAssetAmount(getAssetShortName(txn), getTransactionSign(txn)),
            amountColorRes = getTransactionColorRes(txn)
        )
    }
}
