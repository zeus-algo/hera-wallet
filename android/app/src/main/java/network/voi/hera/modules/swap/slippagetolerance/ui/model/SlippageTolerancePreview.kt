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

package network.voi.hera.modules.swap.slippagetolerance.ui.model

import network.voi.hera.models.PeraFloatChipItem
import network.voi.hera.modules.basepercentageselection.ui.BasePercentageSelectionPreview
import network.voi.hera.utils.Event

data class SlippageTolerancePreview(
    override val chipOptionList: List<PeraFloatChipItem>,
    override var checkedOption: PeraFloatChipItem?,
    override val returnResultEvent: Event<Float>?,
    override val showErrorEvent: Event<String>?,
    override val requestFocusToInputEvent: Event<Unit>?,
    override val prefilledAmountInputValue: Event<String>?
) : BasePercentageSelectionPreview(
    chipOptionList,
    checkedOption,
    returnResultEvent,
    showErrorEvent,
    requestFocusToInputEvent,
    prefilledAmountInputValue
)
