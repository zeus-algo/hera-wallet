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

package network.voi.hera.modules.swap.introduction.ui.usecase

import network.voi.hera.modules.swap.introduction.domain.usecase.SetSwapFeatureIntroductionPageVisibilityUseCase
import network.voi.hera.modules.swap.introduction.ui.SwapIntroductionFragmentDirections
import network.voi.hera.modules.swap.introduction.ui.mapper.SwapIntroductionPreviewMapper
import network.voi.hera.modules.swap.introduction.ui.model.SwapIntroductionPreview
import network.voi.hera.utils.Event
import javax.inject.Inject

class SwapIntroductionPreviewUseCase @Inject constructor(
    private val setSwapFeatureIntroductionPageVisibilityUseCase: SetSwapFeatureIntroductionPageVisibilityUseCase,
    private val swapIntroductionPreviewMapper: SwapIntroductionPreviewMapper
) {

    suspend fun getSwapClickUpdatedPreview(
        accountAddress: String?,
        fromAssetId: Long?,
        toAssetId: Long?,
        defaultFromAssetIdArg: Long,
        defaultToAssetIdArg: Long
    ): SwapIntroductionPreview {
        setIntroductionPageAsShowed()
        val navDirectionEvent = if (accountAddress.isNullOrBlank()) {
            SwapIntroductionFragmentDirections.actionSwapIntroductionFragmentToSwapAccountSelectionNavigation(
                fromAssetId = fromAssetId ?: defaultFromAssetIdArg,
                toAssetId = toAssetId ?: defaultToAssetIdArg
            )
        } else {
            SwapIntroductionFragmentDirections.actionSwapIntroductionFragmentToSwapNavigation(
                accountAddress = accountAddress,
                fromAssetId = fromAssetId ?: defaultFromAssetIdArg,
                toAssetId = toAssetId ?: defaultToAssetIdArg
            )
        }
        return swapIntroductionPreviewMapper.mapToSwapIntroductionPreview(
            navigationDirectionEvent = Event(navDirectionEvent)
        )
    }

    private suspend fun setIntroductionPageAsShowed() {
        setSwapFeatureIntroductionPageVisibilityUseCase.setSwapFeatureIntroductionPageVisibility(false)
    }
}
