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

package network.voi.hera.ui.analyticsdetail

import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.ChartEntryData
import network.voi.hera.models.ChartInterval
import network.voi.hera.models.ChartTimeFrame
import network.voi.hera.usecase.AnalyticsDetailUseCase
import network.voi.hera.utils.Resource
import network.voi.hera.utils.coremanager.ParityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

// TODO Refactor AnalyticsDetailViewModel line by line and update it based on new architecture
@HiltViewModel
class AnalyticsDetailViewModel @Inject constructor(
    private val analyticsDetailUseCase: AnalyticsDetailUseCase,
    private val parityManager: ParityManager
) : BaseViewModel() {

    private val algoPriceHistoryCollector: suspend (value: Resource<ChartEntryData>) -> Unit = {
        _algoPriceHistoryFlow.emit(it)
    }

    private val currencyTimeFrameCollector: suspend (ChartTimeFrame, BigDecimal) -> Unit = { timeFrame, exchangePrice ->
        getAlgoPriceHistory(timeFrame.interval, exchangePrice)
    }
    private val selectedTimeFrameFlow = MutableStateFlow<ChartTimeFrame>(ChartTimeFrame.DEFAULT_TIME_FRAME)

    private val _algoPriceHistoryFlow = MutableStateFlow<Resource<ChartEntryData>>(Resource.Loading)
    val algoPriceHistoryFlow: StateFlow<Resource<ChartEntryData>> = _algoPriceHistoryFlow

    init {
        selectedTimeFrameFlow
            .combine(
                analyticsDetailUseCase.getAlgoExchangeValueFlow().distinctUntilChanged(),
                currencyTimeFrameCollector
            )
            .launchIn(viewModelScope)
    }

    fun updateSelectedTimeFrame(chartTimeFrame: ChartTimeFrame) {
        viewModelScope.launch {
            selectedTimeFrameFlow.emit(chartTimeFrame)
        }
    }

    fun getCurrencyFormattedPrice(price: String): String {
        val currencySymbolOrId = analyticsDetailUseCase.getDisplayedCurrencyId()
        return if (isCurrencySymbolExists(currencySymbolOrId)) {
            "$currencySymbolOrId$price"
        } else {
            "$currencySymbolOrId $price"
        }
    }

    // We do this check because some currencies have same symbol as it's name.
    // For example AED has a symbol AED. If it's like that we show it with one space
    private fun isCurrencySymbolExists(currencyInfo: String): Boolean {
        return currencyInfo.length <= 1
    }

    fun refreshCachedAlgoPrice() {
        viewModelScope.launch {
            parityManager.refreshSelectedCurrencyDetailCache()
        }
    }

    private fun getAlgoPriceHistory(selectedInterval: ChartInterval, cachedCurrencyValueOfPerAlgo: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            analyticsDetailUseCase.getAlgoPriceHistory(
                cachedCurrencyValueOfPerAlgo,
                selectedInterval,
                this
            ).collectLatest(algoPriceHistoryCollector)
        }
    }
}
