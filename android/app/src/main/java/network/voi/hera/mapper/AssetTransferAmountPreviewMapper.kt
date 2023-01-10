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

package network.voi.hera.mapper

import network.voi.hera.models.AssetTransferAmountAssetPreview
import network.voi.hera.models.AssetTransferAmountPreview
import network.voi.hera.utils.Event
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class AssetTransferAmountPreviewMapper @Inject constructor() {

    @SuppressWarnings("LongParameterList")
    fun mapToSuccessPreview(
        assetTransferAmountAssetPreview: AssetTransferAmountAssetPreview,
        enteredAmountSelectedCurrencyValue: String?,
        decimalSeparator: String,
        selectedAmount: BigDecimal?,
        senderAddress: String,
        onMaxAmountEvent: Event<Unit>? = null,
        amountIsValidEvent: Event<BigInteger?>? = null,
        amountIsMoreThanBalanceEvent: Event<Unit>? = null,
        insufficientBalanceToPayFeeEvent: Event<Unit>? = null,
        minimumBalanceIsViolatedResultEvent: Event<String?>? = null,
        assetNotFoundErrorEvent: Event<Unit>? = null
    ): AssetTransferAmountPreview {
        return AssetTransferAmountPreview(
            assetPreview = assetTransferAmountAssetPreview,
            enteredAmountSelectedCurrencyValue = enteredAmountSelectedCurrencyValue,
            decimalSeparator = decimalSeparator,
            selectedAmount = selectedAmount,
            senderAddress = senderAddress,
            onPopulateAmountWithMaxEvent = onMaxAmountEvent,
            amountIsValidEvent = amountIsValidEvent,
            amountIsMoreThanBalanceEvent = amountIsMoreThanBalanceEvent,
            insufficientBalanceToPayFeeEvent = insufficientBalanceToPayFeeEvent,
            minimumBalanceIsViolatedResultEvent = minimumBalanceIsViolatedResultEvent,
            assetNotFoundErrorEvent = assetNotFoundErrorEvent
        )
    }

    fun mapToAssetNotFoundStatePreview() = AssetTransferAmountPreview(assetNotFoundErrorEvent = Event(Unit))
}
