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

package network.voi.hera.modules.collectibles.manage

import android.os.Bundle
import android.view.View
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetManageNftsBinding
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.utils.viewbinding.viewBinding

abstract class BaseManageNFTsBottomSheet : BaseBottomSheet(R.layout.bottom_sheet_manage_nfts) {

    private val toolbarConfiguration = ToolbarConfiguration(
        titleResId = R.string.manage_nfts,
        startIconClick = ::navBack,
        startIconResId = R.drawable.ic_close
    )

    private val binding by viewBinding(BottomSheetManageNftsBinding::bind)

    abstract fun onSortCollectiblesClick()

    abstract fun onFilterCollectiblesClick()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        with(binding) {
            toolbar.configure(toolbarConfiguration)
            filterCollectiblesButton.setOnClickListener { onFilterCollectiblesClick() }
            sortCollectiblesButton.setOnClickListener { onSortCollectiblesClick() }
        }
    }
}
