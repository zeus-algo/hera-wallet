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

package network.voi.hera.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import network.voi.hera.R
import network.voi.hera.databinding.CustomWarningTextViewBinding
import network.voi.hera.utils.viewbinding.viewBinding

class WarningTextView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    private val binding = viewBinding(CustomWarningTextViewBinding::inflate)

    init {
        initAttributes()
    }

    private fun initAttributes() {
        context.obtainStyledAttributes(attrs, R.styleable.WarningTextView).use {
            binding.warningTextView.text = it.getString(R.styleable.WarningTextView_warningText)
        }
    }

    fun setText(warningText: Int) {
        binding.warningTextView.text = context.resources.getText(warningText)
    }
}
