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

package network.voi.hera.modules.swap.slippagetolerance.ui

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import network.voi.hera.customviews.PeraChipGroup
import network.voi.hera.modules.basepercentageselection.ui.BasePercentageSelectionPreview
import network.voi.hera.modules.basepercentageselection.ui.BasePercentageSelectionViewModel
import network.voi.hera.modules.swap.slippagetolerance.ui.model.SlippageTolerancePreview
import network.voi.hera.modules.swap.slippagetolerance.ui.usecase.SlippageTolerancePreviewUseCase
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SlippageToleranceViewModel @Inject constructor(
    private val slippageTolerancePreviewUseCase: SlippageTolerancePreviewUseCase,
    savedStateHandle: SavedStateHandle
) : BasePercentageSelectionViewModel() {

    private val previousSelectedTolerance: Float = savedStateHandle.getOrThrow(SLIPPAGE_TOLERANCE_PARAM_KEY)

    override fun getInitialPreview(resources: Resources): BasePercentageSelectionPreview {
        return slippageTolerancePreviewUseCase.getSlippageTolerancePreview(resources, previousSelectedTolerance)
    }

    override fun getCustomInputResultUpdatedPreview(
        resources: Resources,
        inputValue: String
    ): BasePercentageSelectionPreview? {
        return (getCurrentState() as? SlippageTolerancePreview)?.run {
            slippageTolerancePreviewUseCase.getDoneClickUpdatedPreview(resources, inputValue, this)
        } ?: getCurrentState()
    }

    fun onChipItemSelected(peraChipItem: PeraChipGroup.PeraChipItem, selectedChipIndex: Int) {
        (getCurrentState() as? SlippageTolerancePreview)?.run {
            val newState = slippageTolerancePreviewUseCase
                .getChipItemSelectedUpdatedPreview(selectedChipIndex, peraChipItem, this)
            updatePreviewFlow(newState)
        }
    }

    fun onCustomPercentageChange(value: String) {
        (getCurrentState() as? SlippageTolerancePreview)?.run {
            val newState = slippageTolerancePreviewUseCase.getCustomItemUpdatedPreview(this)
            updatePreviewFlow(newState)
        }
    }

    companion object {
        private const val SLIPPAGE_TOLERANCE_PARAM_KEY = "slippageTolerance"
    }
}
