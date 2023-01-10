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

package network.voi.hera.modules.swap.confirmswap.ui.usecase

import androidx.lifecycle.Lifecycle
import network.voi.hera.R
import network.voi.hera.models.AnnotatedString
import network.voi.hera.modules.accounts.domain.usecase.AccountDetailSummaryUseCase
import network.voi.hera.modules.currency.domain.model.Currency.ALGO
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapType
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuote
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuoteAssetDetail
import network.voi.hera.modules.swap.assetswap.domain.usecase.GetSwapQuoteUseCase
import network.voi.hera.modules.swap.confirmswap.domain.SwapTransactionSignManager
import network.voi.hera.modules.swap.confirmswap.domain.model.SwapQuoteTransaction
import network.voi.hera.modules.swap.confirmswap.domain.usecase.CreateSwapQuoteTransactionsUseCase
import network.voi.hera.modules.swap.confirmswap.ui.mapper.ConfirmSwapAssetDetailMapper
import network.voi.hera.modules.swap.confirmswap.ui.mapper.ConfirmSwapPreviewMapper
import network.voi.hera.modules.swap.confirmswap.ui.model.ConfirmSwapPreview
import network.voi.hera.modules.swap.ledger.signwithledger.ui.model.LedgerDialogPayload
import network.voi.hera.modules.swap.utils.getFormattedMinimumReceivedAmount
import network.voi.hera.modules.swap.utils.priceratioprovider.SwapPriceRatioProviderMapper
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult.Error
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult.LedgerScanFailed
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult.LedgerWaitingForApproval
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult.Loading
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult.Success
import network.voi.hera.modules.transaction.signmanager.ExternalTransactionSignResult.TransactionCancelled
import network.voi.hera.utils.ErrorResource.Api
import network.voi.hera.utils.ErrorResource.LocalErrorResource.Defined
import network.voi.hera.utils.ErrorResource.LocalErrorResource.Local
import network.voi.hera.utils.Event
import network.voi.hera.utils.formatAmount
import network.voi.hera.utils.formatAsAlgoAmount
import network.voi.hera.utils.formatAsAssetAmount
import network.voi.hera.utils.formatAsCurrency
import network.voi.hera.utils.formatAsPercentage
import java.io.IOException
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

class ConfirmSwapPreviewUseCase @Inject constructor(
    private val confirmSwapPreviewMapper: ConfirmSwapPreviewMapper,
    private val confirmSwapAssetDetailMapper: ConfirmSwapAssetDetailMapper,
    private val getSwapQuoteUseCase: GetSwapQuoteUseCase,
    private val parityUseCase: ParityUseCase,
    private val createSwapQuoteTransactionsUseCase: CreateSwapQuoteTransactionsUseCase,
    private val swapTransactionSignManager: SwapTransactionSignManager,
    private val swapPriceRatioProviderMapper: SwapPriceRatioProviderMapper,
    private val accountDetailSummaryUseCase: AccountDetailSummaryUseCase
) {

    fun getConfirmSwapPreview(swapQuote: SwapQuote): ConfirmSwapPreview {
        val accountDetailSummary = accountDetailSummaryUseCase.getAccountDetailSummary(swapQuote.accountAddress)
        return with(swapQuote) {
            confirmSwapPreviewMapper.mapToConfirmSwapPreview(
                fromAssetDetail = createAssetDetail(fromAssetDetail, fromAssetAmount, fromAssetAmountInUsdValue),
                toAssetDetail = createAssetDetail(toAssetDetail, toAssetAmount, toAssetAmountInUsdValue),
                priceRatioProvider = swapPriceRatioProviderMapper.mapToSwapPriceRatioProvider(swapQuote),
                slippageTolerance = slippage.formatAsPercentage(),
                formattedPriceImpact = priceImpact.formatAsPercentage(),
                minimumReceived = getFormattedMinimumReceivedAmount(swapQuote),
                formattedPeraFee = peraFeeAmount.formatAsCurrency(ALGO.symbol),
                formattedExchangeFee = getFormattedExchangeFee(swapQuote),
                swapQuote = swapQuote,
                isLoading = false,
                isPriceImpactErrorVisible = priceImpact > PRICE_IMPACT_ERROR_VISIBILITY_PERCENTAGE,
                errorEvent = null,
                slippageToleranceUpdateSuccessEvent = null,
                accountIconResource = accountDetailSummary.accountIconResource,
                accountDisplayName = accountDetailSummary.accountDisplayName
            )
        }
    }

    suspend fun updateSlippageTolerance(
        slippageTolerance: Float,
        swapQuote: SwapQuote,
        previousState: ConfirmSwapPreview
    ): Flow<ConfirmSwapPreview> = flow {
        with(swapQuote) {
            if (slippage == slippageTolerance) return@flow
            val swapAmount = if (swapType == SwapType.FIXED_INPUT) fromAssetAmount else toAssetAmount
            emit(previousState)
            getSwapQuoteUseCase.getSwapQuote(
                fromAssetId = fromAssetDetail.assetId,
                toAssetId = toAssetDetail.assetId,
                amount = swapAmount.toBigInteger(),
                swapType = swapType,
                accountAddress = accountAddress,
                slippage = slippageTolerance
            ).collect {
                it.useSuspended(
                    onSuccess = { newSwapQuote ->
                        val newState = getConfirmSwapPreview(newSwapQuote).copy(
                            slippageToleranceUpdateSuccessEvent = Event(Unit)
                        )
                        emit(newState)
                    },
                    onFailed = { errorDataResource ->
                        val errorMessage = errorDataResource.exception?.message
                        val errorEvent = if (!errorMessage.isNullOrBlank()) {
                            Event(Api(errorMessage))
                        } else {
                            null
                        }
                        val newState = previousState.copy(
                            isLoading = false,
                            errorEvent = errorEvent
                        )
                        emit(newState)
                    }
                )
            }
        }
    }

    suspend fun createQuoteAndUpdateUi(
        quoteId: Long,
        accountAddress: String,
        previousState: ConfirmSwapPreview
    ): Flow<ConfirmSwapPreview> = channelFlow {
        swapTransactionSignManager.manualStopAllResources()
        createSwapQuoteTransactionsUseCase.createQuoteTransactions(quoteId, accountAddress).useSuspended(
            onSuccess = {
                with(swapTransactionSignManager) {
                    signSwapQuoteTransaction(it)
                    swapTransactionSignResultFlow.collectLatest { result ->
                        trySend(updatePreviewWithSignResult(result, previousState))
                    }
                }
            },
            onFailed = {
                val errorResource = if (it.exception is IOException) {
                    Local(R.string.the_internet_connection)
                } else {
                    val errorMessage = it.exception?.message
                    if (errorMessage.isNullOrBlank()) {
                        Local(R.string.we_encountered_an_unexpected, R.string.something_went_wrong)
                    } else {
                        Api(errorMessage)
                    }
                }
                val newState = previousState.copy(
                    errorEvent = Event(errorResource)
                )
                trySend(newState)
            }
        )
    }

    private fun updatePreviewWithSignResult(
        result: ExternalTransactionSignResult,
        previousState: ConfirmSwapPreview
    ): ConfirmSwapPreview {
        with(previousState) {
            return when (result) {
                is Success<*> -> {
                    (result.signedTransaction as? List<SwapQuoteTransaction>)?.let { signedTransactions ->
                        copy(navigateToTransactionStatusFragmentEvent = Event(signedTransactions))
                    } ?: copy(errorEvent = Event(Local(R.string.an_error_occured)))
                }
                Loading -> copy(isLoading = true)
                is Error.Api -> copy(errorEvent = Event(Api(result.errorMessage)))
                is Error.Defined -> copy(errorEvent = Event(Defined(result.description)))
                LedgerScanFailed -> copy(navigateToLedgerNotFoundDialogEvent = Event(Unit))
                is LedgerWaitingForApproval -> {
                    val ledgerPayload = LedgerDialogPayload(
                        result.ledgerName,
                        result.currentTransactionIndex,
                        result.totalTransactionCount,
                        result.isTransactionIndicatorVisible
                    )
                    copy(navigateToLedgerWaitingForApprovalDialogEvent = Event(ledgerPayload))
                }
                is TransactionCancelled -> {
                    val annotatedString = (result.error as? Error.Defined)?.description
                        ?: AnnotatedString(R.string.an_error_occured)
                    copy(
                        errorEvent = Event(Defined(annotatedString)),
                        dismissLedgerWaitingForApprovalDialogEvent = Event(Unit)
                    )
                }
                else -> previousState
            }
        }
    }

    private fun createAssetDetail(
        assetDetail: SwapQuoteAssetDetail,
        amount: BigDecimal,
        approximateValueInUsd: BigDecimal,
    ): ConfirmSwapPreview.SwapAssetDetail {
        val formattedAmount = amount.movePointLeft(assetDetail.fractionDecimals)
            .formatAmount(assetDetail.fractionDecimals, isDecimalFixed = false)
        return confirmSwapAssetDetailMapper.mapToAssetDetail(
            assetId = assetDetail.assetId,
            formattedAmount = formattedAmount,
            formattedApproximateValue = getFormattedApproximateValue(approximateValueInUsd),
            shortName = assetDetail.shortName,
            verificationTier = assetDetail.verificationTier
        )
    }

    private fun getFormattedApproximateValue(approximateValueInUsd: BigDecimal): String {
        val usdToSelectedCurrencyRate = parityUseCase.getDisplayedCurrencyRatio()
        val primaryCurrencySymbol = parityUseCase.getDisplayedCurrencySymbol()
        return usdToSelectedCurrencyRate.multiply(approximateValueInUsd)
            .formatAsCurrency(primaryCurrencySymbol, isFiat = true)
    }

    private fun getFormattedExchangeFee(swapQuote: SwapQuote): String {
        return with(swapQuote) {
            exchangeFeeAmount.stripTrailingZeros().toPlainString().run {
                if (isFromAssetAlgo) {
                    formatAsAlgoAmount()
                } else {
                    formatAsAssetAmount(fromAssetDetail.shortName.getName())
                }
            }
        }
    }

    fun setupSwapTransactionSignManager(lifecycle: Lifecycle) {
        swapTransactionSignManager.setup(lifecycle)
    }

    fun stopAllResources() {
        swapTransactionSignManager.stopAllResources()
    }

    companion object {
        private const val PRICE_IMPACT_ERROR_VISIBILITY_PERCENTAGE = 5f
    }
}
