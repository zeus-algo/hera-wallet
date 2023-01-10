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

package network.voi.hera.usecase

import network.voi.hera.mapper.AccountAssetDataMapper
import network.voi.hera.models.AssetHolding
import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.models.BaseAssetDetail
import network.voi.hera.modules.parity.domain.usecase.PrimaryCurrencyParityCalculationUseCase
import network.voi.hera.modules.parity.domain.usecase.SecondaryCurrencyParityCalculationUseCase
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.formatAmount
import javax.inject.Inject

class AccountAssetAmountUseCase @Inject constructor(
    private val accountAssetDataMapper: AccountAssetDataMapper,
    private val primaryCurrencyParityCalculationUseCase: PrimaryCurrencyParityCalculationUseCase,
    private val secondaryCurrencyParityCalculationUseCase: SecondaryCurrencyParityCalculationUseCase
) {

    // TODO: 16.03.2022 Find a better name since it returns OwnedAssetData but not amount
    fun getAssetAmount(
        assetHolding: AssetHolding,
        assetItem: BaseAssetDetail
    ): BaseAccountAssetData.BaseOwnedAssetData.OwnedAssetData {
        val safeDecimal = assetItem.fractionDecimals ?: DEFAULT_ASSET_DECIMAL
        val assetParityValueInSelectedCurrency = primaryCurrencyParityCalculationUseCase.getAssetParityValue(
            assetHolding,
            assetItem
        )
        val assetParityValueInSecondaryCurrency = secondaryCurrencyParityCalculationUseCase.getAssetParityValue(
            assetHolding,
            assetItem
        )
        return accountAssetDataMapper.mapToOwnedAssetData(
            assetDetail = assetItem,
            amount = assetHolding.amount,
            formattedAmount = assetHolding.amount.formatAmount(safeDecimal),
            formattedCompactAmount = assetHolding.amount.formatAmount(safeDecimal, isCompact = true),
            parityValueInSelectedCurrency = assetParityValueInSelectedCurrency,
            parityValueInSecondaryCurrency = assetParityValueInSecondaryCurrency,
            optedInAtRound = assetHolding.optedInAtRound
        )
    }
}
