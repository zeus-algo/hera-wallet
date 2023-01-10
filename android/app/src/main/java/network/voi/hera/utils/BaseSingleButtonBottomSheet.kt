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

package network.voi.hera.utils

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetSingleButtonBinding
import network.voi.hera.models.AnnotatedString
import network.voi.hera.utils.extensions.hide
import network.voi.hera.utils.extensions.show
import network.voi.hera.utils.viewbinding.viewBinding

abstract class BaseSingleButtonBottomSheet : BaseBottomSheet(
    layoutResId = R.layout.bottom_sheet_single_button
) {

    private val binding by viewBinding(BottomSheetSingleButtonBinding::bind)

    protected abstract val title: AnnotatedString
    protected abstract val iconDrawableResId: Int
    protected abstract val iconDrawableTintResId: Int
    protected abstract val descriptionAnnotatedString: AnnotatedString?
    protected open val buttonStringResId: Int = R.string.close
    protected open val errorAnnotatedString: AnnotatedString? = null
    protected open val shouldDescriptionHasLinkMovementMethod: Boolean = false

    abstract fun onConfirmationButtonClick()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitle()
        initDescription()
        initIcon()
        initConfirmationButton()
        initErrorText()
    }

    private fun initTitle() {
        binding.titleTextView.text = context?.getXmlStyledString(title)
    }

    private fun initDescription() {
        with(binding) {
            descriptionAnnotatedString
                ?.let {
                    descriptionTextView.apply {
                        text = context?.getXmlStyledString(it)
                        if (shouldDescriptionHasLinkMovementMethod) {
                            movementMethod = LongClickLinkMovementMethod.getInstance()
                        }
                    }
                } ?: descriptionTextView.hide()
        }
    }

    private fun initIcon() {
        binding.iconImageView.apply {
            if (iconDrawableResId == 0) {
                hide()
            } else {
                setImageResource(iconDrawableResId)
                if (iconDrawableTintResId != 0) {
                    imageTintList = ContextCompat.getColorStateList(context, iconDrawableTintResId)
                }
            }
        }
    }

    private fun initConfirmationButton() {
        binding.confirmationButton.apply {
            setText(buttonStringResId)
            setOnClickListener { onConfirmationButtonClick() }
        }
    }

    private fun initErrorText() {
        errorAnnotatedString?.let { safeErrorString ->
            with(binding.errorGroupLayout) {
                errorTextView.text = context?.getXmlStyledString(safeErrorString)
                errorGroup.show()
            }
        }
    }
}
