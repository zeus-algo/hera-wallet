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

package network.voi.hera.modules.currency.domain.repository

import network.voi.hera.models.Result
import network.voi.hera.modules.currency.domain.model.CurrencyOption
import network.voi.hera.sharedpref.SharedPrefLocalSource

interface CurrencyRepository {

    fun setPrimaryCurrencyChangeListener(listener: SharedPrefLocalSource.OnChangeListener<String>)
    fun removePrimaryCurrencyChangeListener(listener: SharedPrefLocalSource.OnChangeListener<String>)

    fun getPrimaryCurrencyPreference(defaultValue: String): String
    fun setPrimaryCurrencyPreference(currencyPreference: String)

    suspend fun getCurrencyOptionList(): Result<List<CurrencyOption>>

    companion object {
        const val INJECTION_NAME = "currencyRepositoryInjectionName"
    }
}
