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

package network.voi.hera.modules.swap.balancepercentage.ui

import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.customviews.PeraChipGroup
import network.voi.hera.models.PeraFloatChipItem
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.basepercentageselection.ui.BasePercentageSelectionBottomSheet
import network.voi.hera.modules.basepercentageselection.ui.BasePercentageSelectionViewModel
import network.voi.hera.utils.DecimalDigitsInputFilter
import network.voi.hera.utils.setFragmentNavigationResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BalancePercentageBottomSheet : BasePercentageSelectionBottomSheet() {

    override val toolbarConfiguration = ToolbarConfiguration(
        startIconClick = ::navBack,
        startIconResId = R.drawable.ic_close,
        titleResId = R.string.balance_percentage
    )
    override val inputFieldHintText: Int = R.string.set_custom_percentage

    override fun onChipItemSelected(peraChipItem: PeraChipGroup.PeraChipItem, selectedChipIndex: Int) {
        (peraChipItem as? PeraFloatChipItem)?.run { handleResult(value) }
    }

    private val balancePercentageViewModel by viewModels<BalancePercentageViewModel>()

    override val basePercentageSelectionViewModel: BasePercentageSelectionViewModel
        get() = balancePercentageViewModel

    override fun handleResult(value: Float) {
        super.handleResult(value)
        setFragmentNavigationResult(
            key = CHECKED_BALANCE_PERCENTAGE_KEY,
            value = value
        )
        dismissAllowingStateLoss()
    }

    override fun initUi() {
        super.initUi()
        initCustomInputLayout()
    }

    private fun initCustomInputLayout() {
        binding.customPercentageInput.setInputFilter(DecimalDigitsInputFilter(BALANCE_INPUT_DECIMAL_LIMIT))
    }

    companion object {
        private const val BALANCE_INPUT_DECIMAL_LIMIT = 3
        const val CHECKED_BALANCE_PERCENTAGE_KEY = "checked_balance_percentage_key"
    }
}
