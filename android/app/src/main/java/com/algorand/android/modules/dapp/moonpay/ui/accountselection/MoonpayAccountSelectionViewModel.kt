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

package com.algorand.android.modules.dapp.moonpay.ui.accountselection

import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorand.android.models.BaseAccountSelectionListItem
import com.algorand.android.modules.dapp.moonpay.domain.usecase.MoonpayAccountSelectionPreviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MoonpayAccountSelectionViewModel @Inject constructor(
    private val moonpayAccountSelectionPreviewUseCase: MoonpayAccountSelectionPreviewUseCase
) : ViewModel() {

    val accountItemsFlow: Flow<List<BaseAccountSelectionListItem>>
        get() = _accountItemsFlow
    private val _accountItemsFlow = MutableStateFlow<List<BaseAccountSelectionListItem>>(emptyList())

    init {
        initAccountItems()
    }

    private fun initAccountItems() {
        viewModelScope.launch {
            _accountItemsFlow.emit(moonpayAccountSelectionPreviewUseCase.getMoonpayAccountSelectionPreview())
        }
    }
}
