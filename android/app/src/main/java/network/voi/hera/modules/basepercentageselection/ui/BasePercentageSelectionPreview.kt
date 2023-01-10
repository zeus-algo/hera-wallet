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

package network.voi.hera.modules.basepercentageselection.ui

import network.voi.hera.models.PeraFloatChipItem
import network.voi.hera.utils.Event

open class BasePercentageSelectionPreview(
    open val chipOptionList: List<PeraFloatChipItem>,
    open var checkedOption: PeraFloatChipItem?,
    open val returnResultEvent: Event<Float>?,
    open val showErrorEvent: Event<String>?,
    open val requestFocusToInputEvent: Event<Unit>?,
    open val prefilledAmountInputValue: Event<String>?
)
