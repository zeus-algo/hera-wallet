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

import network.voi.hera.models.AssetDetail
import network.voi.hera.models.AssetHolding
import network.voi.hera.models.WalletConnectAssetInformation
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.formatAsCurrency
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class WalletConnectAssetInformationMapper @Inject constructor() {

    fun algorandMapToWalletConnectAssetInformation(
        assetDetail: AssetDetail?,
        amount: BigDecimal,
        currencySymbol: String
    ): WalletConnectAssetInformation? {
        if (assetDetail == null) return null
        return WalletConnectAssetInformation(
            assetId = assetDetail.assetId,
            shortName = assetDetail.shortName,
            fullName = assetDetail.fullName,
            decimal = assetDetail.fractionDecimals ?: ALGO_DECIMALS,
            amount = amount.toBigInteger(),
            formattedSelectedCurrencyValue = amount.formatAsCurrency(currencySymbol),
            verificationTier = assetDetail.verificationTier
        )
    }

    fun otherAssetMapToWalletConnectAssetInformation(
        assetDetail: AssetDetail?,
        assetHolding: AssetHolding?,
        amount: BigInteger,
        selectedCurrencyUsdConversionRate: BigDecimal,
        currencySymbol: String
    ): WalletConnectAssetInformation? {
        val formattedSelectedCurrencyValue = if (assetDetail?.usdValue != null) {
            amount.toBigDecimal()
                .movePointLeft(assetDetail.fractionDecimals ?: DEFAULT_ASSET_DECIMAL)
                .multiply(selectedCurrencyUsdConversionRate)
                .multiply(assetDetail.usdValue)
        } else {
            null
        }

        if (assetDetail == null) return null
        return WalletConnectAssetInformation(
            assetId = assetDetail.assetId,
            shortName = assetDetail.shortName,
            fullName = assetDetail.fullName,
            decimal = assetDetail.fractionDecimals ?: DEFAULT_ASSET_DECIMAL,
            amount = assetHolding?.amount,
            formattedSelectedCurrencyValue = formattedSelectedCurrencyValue?.formatAsCurrency(currencySymbol),
            verificationTier = assetDetail.verificationTier
        )
    }
}
