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

package network.voi.hera.modules.swap.assetswap.domain.usecase

import network.voi.hera.deviceregistration.domain.usecase.DeviceIdUseCase
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.modules.currency.domain.usecase.DisplayedCurrencyUseCase
import network.voi.hera.modules.parity.domain.model.ParityValue
import network.voi.hera.modules.swap.assetselection.base.ui.model.SwapType
import network.voi.hera.modules.swap.assetswap.domain.mapper.SwapQuoteAssetDetailMapper
import network.voi.hera.modules.swap.assetswap.domain.mapper.SwapQuoteMapper
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuote
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuoteAssetDetail
import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuoteProvider
import network.voi.hera.modules.swap.assetswap.domain.model.dto.SwapQuoteAssetDetailDTO
import network.voi.hera.modules.swap.assetswap.domain.model.dto.SwapQuoteDTO
import network.voi.hera.modules.swap.assetswap.domain.repository.AssetSwapRepository
import network.voi.hera.modules.swap.utils.defaultExchangeSwapFee
import network.voi.hera.modules.swap.utils.defaultPeraSwapFee
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.DEFAULT_ASSET_DECIMAL
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.toBigDecimalOrZero
import network.voi.hera.utils.toBigIntegerOrZero
import network.voi.hera.utils.toFloatOrZero
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GetSwapQuoteUseCase @Inject constructor(
    @Named(AssetSwapRepository.INJECTION_NAME)
    private val assetSwapRepository: AssetSwapRepository,
    private val swapQuoteMapper: SwapQuoteMapper,
    private val swapQuoteAssetDetailMapper: SwapQuoteAssetDetailMapper,
    private val deviceIdUseCase: DeviceIdUseCase,
    private val displayedCurrencyUseCase: DisplayedCurrencyUseCase
) {

    suspend fun getSwapQuote(
        fromAssetId: Long,
        toAssetId: Long,
        amount: BigInteger,
        swapType: SwapType,
        accountAddress: String,
        slippage: Float
    ) = flow<DataResource<SwapQuote>> {
        emit(DataResource.Loading())
        val safeFromAssetId = if (fromAssetId == ALGO_ID) 0 else fromAssetId
        val safeToAssetId = if (toAssetId == ALGO_ID) 0 else toAssetId
        val deviceId = deviceIdUseCase.getSelectedNodeDeviceId().orEmpty()
        val providers = SwapQuoteProvider.getProviders() // TODO Get this from UI when design is ready
        assetSwapRepository.getSwapQuote(
            safeFromAssetId,
            safeToAssetId,
            amount,
            swapType,
            accountAddress,
            deviceId,
            slippage,
            providers
        ).map { result ->
            result.use(
                onSuccess = { swapQuoteDto ->
                    val swapQuote = mapSwapQuoteOrNull(swapQuoteDto, swapType)
                    if (swapQuote == null) {
                        emit(DataResource.Error.Local(IllegalArgumentException())) // TODO Use proper exception
                    } else {
                        emit(DataResource.Success(swapQuote))
                    }
                },
                onFailed = { exception, code ->
                    emit(DataResource.Error.Api(exception, code))
                }
            )
        }.collect()
    }

    private fun mapSwapQuoteOrNull(swapQuoteDto: SwapQuoteDTO, swapType: SwapType): SwapQuote? {
        return with(swapQuoteDto) {
            swapQuoteMapper.mapToSwapQuote(
                swapQuoteDTO = this,
                quoteId = id ?: return null,
                swapType = swapType,
                fromAssetDetail = mapSwapQuoteAssetDetail(swapQuoteDto.assetInAssetDetail) ?: return null,
                toAssetDetail = mapSwapQuoteAssetDetail(swapQuoteDto.assetOutAssetDetail) ?: return null,
                fromAssetAmount = swapQuoteDto.assetInAmount.toBigDecimalOrZero(),
                toAssetAmount = swapQuoteDto.assetOutAmount.toBigDecimalOrZero(),
                fromAssetAmountInUsdValue = swapQuoteDto.assetInAmountInUsdValue.toBigDecimalOrZero(),
                fromAssetAmountInSelectedCurrency = getFromAssetAmountInSelectedCurrency(swapQuoteDto),
                fromAssetAmountWithSlippage = swapQuoteDto.assetInAmountWithSlippage.toBigDecimalOrZero(),
                toAssetAmountInUsdValue = swapQuoteDto.assetOutAmountInUsdValue.toBigDecimalOrZero(),
                toAssetAmountInSelectedCurrency = getToAssetAmountInSelectedCurrency(swapQuoteDto),
                toAssetAmountWithSlippage = swapQuoteDto.assetOutAmountWithSlippage.toBigDecimalOrZero(),
                slippage = swapQuoteDto.slippage.toFloatOrZero() * SLIPPAGE_TOLERANCE_RESPONSE_MULTIPLIER,
                price = swapQuoteDto.price.toFloatOrZero(),
                priceImpact = swapQuoteDto.priceImpact.toFloatOrZero() * PRICE_IMPACT_RESPONSE_MULTIPLIER,
                peraFeeAmount = swapQuoteDto.peraFeeAmount?.movePointLeft(ALGO_DECIMALS) ?: defaultPeraSwapFee,
                exchangeFeeAmount = swapQuoteDto.exchangeFeeAmount?.movePointLeft(
                    swapQuoteDto.assetInAssetDetail?.fractionDecimals ?: DEFAULT_ASSET_DECIMAL_FOR_SWAP
                ) ?: defaultExchangeSwapFee,
                swapperAddress = swapperAddress ?: return null
            )
        }
    }

    private fun getFromAssetAmountInSelectedCurrency(swapQuoteDto: SwapQuoteDTO): ParityValue {
        with(swapQuoteDto) {
            val usdValuePerAsset = getUsdValuePerAsset(
                assetAmount = assetInAmount,
                assetDecimal = assetInAssetDetail?.fractionDecimals,
                totalAssetAmountInUsdValue = assetInAmountInUsdValue
            )
            return displayedCurrencyUseCase.getDisplayedCurrencyParityValue(
                assetAmount = assetInAmount.toBigIntegerOrZero(),
                assetUsdValue = usdValuePerAsset,
                assetDecimal = assetInAssetDetail?.fractionDecimals ?: DEFAULT_ASSET_DECIMAL_FOR_SWAP
            )
        }
    }

    private fun getToAssetAmountInSelectedCurrency(swapQuoteDto: SwapQuoteDTO): ParityValue {
        return with(swapQuoteDto) {
            val usdValuePerAsset = getUsdValuePerAsset(
                assetAmount = assetOutAmount,
                assetDecimal = assetOutAssetDetail?.fractionDecimals,
                totalAssetAmountInUsdValue = assetOutAmountInUsdValue
            )
            displayedCurrencyUseCase.getDisplayedCurrencyParityValue(
                assetAmount = assetOutAmount.toBigIntegerOrZero(),
                assetUsdValue = usdValuePerAsset,
                assetDecimal = assetOutAssetDetail?.fractionDecimals ?: DEFAULT_ASSET_DECIMAL_FOR_SWAP
            )
        }
    }

    private fun getUsdValuePerAsset(
        assetAmount: String?,
        assetDecimal: Int?,
        totalAssetAmountInUsdValue: String?
    ): BigDecimal {
        val assetOutAmountAsBigDecimal = assetAmount.toBigDecimalOrZero()

        if (assetOutAmountAsBigDecimal == BigDecimal.ZERO) {
            return BigDecimal.ZERO
        }

        val safeAssetDecimal = assetDecimal ?: DEFAULT_ASSET_DECIMAL_FOR_SWAP

        return totalAssetAmountInUsdValue
            .toBigDecimalOrZero()
            .divide(assetOutAmountAsBigDecimal.movePointLeft(safeAssetDecimal), safeAssetDecimal, RoundingMode.DOWN)
    }

    private fun mapSwapQuoteAssetDetail(dto: SwapQuoteAssetDetailDTO?): SwapQuoteAssetDetail? {
        if (dto == null) return null
        return swapQuoteAssetDetailMapper.mapToSwapQuoteAssetDetail(
            dto = dto,
            assetId = dto.assetId ?: return null,
            fractionDecimals = dto.fractionDecimals ?: DEFAULT_ASSET_DECIMAL_FOR_SWAP,
            usdValue = dto.usdValue.toBigDecimalOrZero()
        )
    }

    companion object {
        private const val SLIPPAGE_TOLERANCE_RESPONSE_MULTIPLIER = 100f
        private const val PRICE_IMPACT_RESPONSE_MULTIPLIER = 100f
        private const val DEFAULT_ASSET_DECIMAL_FOR_SWAP = DEFAULT_ASSET_DECIMAL
    }
}
