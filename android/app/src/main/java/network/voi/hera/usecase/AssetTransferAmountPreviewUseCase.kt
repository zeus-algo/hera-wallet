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

import network.voi.hera.mapper.AssetTransferAmountAssetPreviewMapper
import network.voi.hera.mapper.AssetTransferAmountPreviewMapper
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.AssetTransferAmountPreview
import network.voi.hera.models.AssetTransferAmountValidationPreviewResult
import network.voi.hera.modules.currency.domain.usecase.CurrencyUseCase
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.utils.Event
import network.voi.hera.utils.formatAsCurrency
import network.voi.hera.utils.getDecimalSeparator
import network.voi.hera.utils.validator.AmountTransactionValidationUseCase
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class AssetTransferAmountPreviewUseCase @Inject constructor(
    private val assetTransferAmountPreviewMapper: AssetTransferAmountPreviewMapper,
    private val getBaseOwnedAssetDataUseCase: GetBaseOwnedAssetDataUseCase,
    private val parityUseCase: ParityUseCase,
    private val currencyUseCase: CurrencyUseCase,
    private val amountTransactionValidationUseCase: AmountTransactionValidationUseCase,
    private val assetTransferAmountAssetPreviewMapper: AssetTransferAmountAssetPreviewMapper
) {

    fun getAssetTransferAmountPreview(
        senderAddress: String,
        assetId: Long,
        amount: BigDecimal? = null
    ): AssetTransferAmountPreview {
        val accountAssetData = getBaseOwnedAssetDataUseCase.getBaseOwnedAssetData(assetId, senderAddress)
            ?: return assetTransferAmountPreviewMapper.mapToAssetNotFoundStatePreview()
        val enteredAmountSelectedCurrencyValue = formatEnteredAmount(
            amount = amount ?: BigDecimal.ZERO,
            usdValue = accountAssetData.usdValue,
            usdToDisplayedCurrencyConversionRate = getUsdToDisplayedCurrencyConversionRate(assetId),
            displayCurrencySymbol = getDisplayCurrencySymbol(assetId)
        )
        val decimalSeparator = getDecimalSeparator()
        return assetTransferAmountPreviewMapper.mapToSuccessPreview(
            assetTransferAmountAssetPreview = assetTransferAmountAssetPreviewMapper.mapTo(accountAssetData),
            enteredAmountSelectedCurrencyValue = enteredAmountSelectedCurrencyValue,
            decimalSeparator = decimalSeparator,
            selectedAmount = amount,
            senderAddress = senderAddress
        )
    }

    fun getAmountValidatedPreview(
        preview: AssetTransferAmountPreview,
        amount: BigDecimal
    ): AssetTransferAmountPreview {
        // TODO: Discuss this approach with the team, and think on possible namings instead of "..PreviewResult"
        return when (val amountValidationPreviewResult = getAssetTransferAmountValidationResult(preview, amount)) {
            is AssetTransferAmountValidationPreviewResult.AmountIsValidResult -> {
                preview.copy(amountIsValidEvent = Event(amountValidationPreviewResult.selectedAmount))
            }
            is AssetTransferAmountValidationPreviewResult.AmountIsMoreThanBalanceResult -> {
                preview.copy(amountIsMoreThanBalanceEvent = Event(Unit))
            }
            is AssetTransferAmountValidationPreviewResult.InsufficientBalanceToPayFeeResult -> {
                preview.copy(insufficientBalanceToPayFeeEvent = Event(Unit))
            }
            is AssetTransferAmountValidationPreviewResult.MinimumBalanceIsViolatedResult -> {
                preview.copy(minimumBalanceIsViolatedResultEvent = Event(preview.senderAddress))
            }
            else -> preview
        }
    }

    fun getCalculatedSendableAmount(address: String, assetId: Long, amount: BigDecimal): BigInteger? {
        with(amountTransactionValidationUseCase) {
            val amountInBigInteger = getAmountAsBigInteger(amount, assetId) ?: return null
            val maximumSendableAmount = getMaximumSendableAmount(address, assetId) ?: return null
            return minOf(amountInBigInteger, maximumSendableAmount)
        }
    }

    private fun getUsdToDisplayedCurrencyConversionRate(assetId: Long): BigDecimal {
        return if (shouldUseSecondaryCurrency(assetId)) {
            parityUseCase.getUsdToSecondaryCurrencyConversionRate()
        } else {
            parityUseCase.getUsdToPrimaryCurrencyConversionRate()
        }
    }

    private fun getDisplayCurrencySymbol(assetId: Long): String {
        return if (shouldUseSecondaryCurrency(assetId)) {
            parityUseCase.getSecondaryCurrencySymbol()
        } else {
            parityUseCase.getPrimaryCurrencySymbolOrName()
        }
    }

    private fun shouldUseSecondaryCurrency(assetId: Long): Boolean {
        return assetId == ALGO_ID && currencyUseCase.isPrimaryCurrencyAlgo()
    }

    private fun formatEnteredAmount(
        amount: BigDecimal,
        usdValue: BigDecimal?,
        usdToDisplayedCurrencyConversionRate: BigDecimal?,
        displayCurrencySymbol: String
    ): String? {
        return amount.multiply(usdValue ?: return null)
            .multiply(usdToDisplayedCurrencyConversionRate ?: return null)
            .formatAsCurrency(displayCurrencySymbol)
    }

    private fun getAssetTransferAmountValidationResult(
        preview: AssetTransferAmountPreview,
        amount: BigDecimal
    ): AssetTransferAmountValidationPreviewResult? {
        // Even though we are not passing PreviewResult to UI anymore, I think it a good a idea to still keep it
        // It enforces to create a single UI result, even though we are still passing Events separately to the fragment
        // TODO: Discuss this approach with the team, and think on possible namings instead of "..PreviewResult"
        if (preview.senderAddress == null || preview.assetPreview?.assetId == null) return null
        with(
            amountTransactionValidationUseCase.validateAssetAmount(
                amountInBigDecimal = amount,
                senderAddress = preview.senderAddress,
                assetId = preview.assetPreview.assetId
            )
        ) {
            return when {
                isAmountMoreThanBalance == true -> {
                    AssetTransferAmountValidationPreviewResult.AmountIsMoreThanBalanceResult
                }
                isBalanceInsufficientForPayingFee == true -> {
                    AssetTransferAmountValidationPreviewResult.InsufficientBalanceToPayFeeResult
                }
                isMinimumBalanceViolated == true -> {
                    AssetTransferAmountValidationPreviewResult.MinimumBalanceIsViolatedResult
                }
                isAmountMoreThanBalance == false &&
                    isBalanceInsufficientForPayingFee == false &&
                    isMinimumBalanceViolated == false -> {
                    AssetTransferAmountValidationPreviewResult.AmountIsValidResult(selectedAmount)
                }
                else -> null
            }
        }
    }
}
