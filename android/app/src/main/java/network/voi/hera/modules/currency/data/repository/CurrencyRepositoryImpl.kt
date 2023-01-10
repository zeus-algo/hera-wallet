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

package network.voi.hera.modules.currency.data.repository

import network.voi.hera.models.Result
import network.voi.hera.modules.currency.data.local.CurrencyLocalSource
import network.voi.hera.modules.currency.data.mapper.CurrencyOptionMapper
import network.voi.hera.modules.currency.domain.model.CurrencyOption
import network.voi.hera.modules.currency.domain.repository.CurrencyRepository
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.requestWithHipoErrorHandler
import network.voi.hera.sharedpref.SharedPrefLocalSource
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyLocalSource: CurrencyLocalSource,
    private val currencyOptionMapper: CurrencyOptionMapper,
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val hipoApiErrorHandler: RetrofitErrorHandler
) : CurrencyRepository {

    override fun setPrimaryCurrencyChangeListener(listener: SharedPrefLocalSource.OnChangeListener<String>) {
        currencyLocalSource.addListener(listener)
    }

    override fun removePrimaryCurrencyChangeListener(listener: SharedPrefLocalSource.OnChangeListener<String>) {
        currencyLocalSource.removeListener(listener)
    }

    override fun getPrimaryCurrencyPreference(defaultValue: String): String {
        return currencyLocalSource.getData(defaultValue)
    }

    override fun setPrimaryCurrencyPreference(currencyPreference: String) {
        currencyLocalSource.saveData(currencyPreference)
    }

    override suspend fun getCurrencyOptionList(): Result<List<CurrencyOption>> {
        val currencyOptionResponseResult = requestWithHipoErrorHandler(hipoApiErrorHandler) {
            mobileAlgorandApi.getCurrencies()
        }
        return currencyOptionResponseResult.map { currencyOptionResponseList ->
            currencyOptionResponseList.map { currencyOptionResponse ->
                currencyOptionMapper.mapToCurrencyOption(currencyOptionResponse)
            }
        }
    }
}
