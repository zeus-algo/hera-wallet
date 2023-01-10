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

package network.voi.hera.modules.informationbottomsheet.ui

import android.os.Bundle
import android.view.View
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetBaseInformationBinding
import network.voi.hera.utils.viewbinding.viewBinding

abstract class BaseInformationBottomSheet : BaseBottomSheet(R.layout.bottom_sheet_base_information) {

    abstract val titleTextResId: Int
    abstract val descriptionTextResId: Int
    abstract val neutralButtonTextResId: Int

    private val binding by viewBinding(BottomSheetBaseInformationBinding::bind)

    abstract fun onNeutralButtonClick()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        with(binding) {
            informationTitleTextView.setText(titleTextResId)
            informationDescriptionTextView.setText(descriptionTextResId)
            neutralButton.apply {
                setText(neutralButtonTextResId)
                setOnClickListener {
                    onNeutralButtonClick()
                    dismissAllowingStateLoss()
                }
            }
        }
    }
}
