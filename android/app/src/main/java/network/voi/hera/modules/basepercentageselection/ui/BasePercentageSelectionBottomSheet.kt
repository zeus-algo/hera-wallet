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

import android.os.Bundle
import android.view.View
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.customviews.PeraChipGroup
import network.voi.hera.customviews.PeraChipGroup.PeraChipItem
import network.voi.hera.databinding.BottomSheetPercentageSelectionBinding
import network.voi.hera.models.TextButton
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.hideKeyboard
import network.voi.hera.utils.viewbinding.viewBinding

abstract class BasePercentageSelectionBottomSheet : BaseBottomSheet(R.layout.bottom_sheet_percentage_selection) {

    abstract val toolbarConfiguration: ToolbarConfiguration

    abstract val inputFieldHintText: Int

    abstract fun onChipItemSelected(peraChipItem: PeraChipItem, selectedChipIndex: Int)

    protected val binding by viewBinding(BottomSheetPercentageSelectionBinding::bind)

    abstract val basePercentageSelectionViewModel: BasePercentageSelectionViewModel

    private val peraCheckGroupListener = object : PeraChipGroup.PeraChipGroupListener {
        override fun onCheckChange(peraChipItem: PeraChipItem, selectedChipIndex: Int) {
            onChipItemSelected(peraChipItem, selectedChipIndex)
        }
    }

    private val basePercentageSelectionPreviewCollector: suspend (BasePercentageSelectionPreview?) -> Unit = {
        it?.run {
            binding.predefinedPercentageChipGroup.initPeraChipGroup(chipOptionList, checkedOption)
            returnResultEvent?.consume()?.run { handleResult(this) }
            showErrorEvent?.consume()?.run { showGlobalError(this) }
            requestFocusToInputEvent?.consume()?.run { focusOnInputEditText() }
            prefilledAmountInputValue?.consume()?.run { updateCustomInputEditText(this) }
        }
    }

    private fun updateCustomInputEditText(inputValue: String) {
        binding.customPercentageInput.text = inputValue
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
        basePercentageSelectionViewModel.initPreview(resources)
    }

    protected open fun handleResult(value: Float) {
        binding.root.hideKeyboard()
    }

    private fun focusOnInputEditText() {
        binding.customPercentageInput.requestFocusAndShowKeyboard()
    }

    protected open fun initUi() {
        with(binding) {
            toolbar.apply {
                configure(toolbarConfiguration)
                setEndButton(TextButton(R.string.done, R.color.link_primary, ::onDoneClick))
            }
            customPercentageInput.apply {
                hint = getString(inputFieldHintText)
                setOnEditorEnterClickListener { onDoneClick() }
            }
            predefinedPercentageChipGroup.setListener(peraCheckGroupListener)
        }
    }

    protected fun setCustomPercentageChangeListener(onChange: (String) -> Unit) {
        binding.customPercentageInput.setOnTextChangeListener { onChange(it) }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            basePercentageSelectionViewModel.basePercentageSelectionPreviewFlow,
            basePercentageSelectionPreviewCollector
        )
    }

    private fun onDoneClick() {
        val percentageInput = binding.customPercentageInput.text
        basePercentageSelectionViewModel.onDoneClick(resources, percentageInput)
    }
}
