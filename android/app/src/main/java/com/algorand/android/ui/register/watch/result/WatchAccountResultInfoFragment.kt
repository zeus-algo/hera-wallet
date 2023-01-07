/*
 *  Copyright 2022 Pera Wallet, LDA
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package com.algorand.android.ui.register.watch.result

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import network.voi.hera.R
import com.algorand.android.models.FragmentConfiguration
import com.algorand.android.ui.common.BaseInfoFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WatchAccountResultInfoFragment : BaseInfoFragment() {
    override val fragmentConfiguration = FragmentConfiguration()

    private val watchAccountResultInfoViewModel: WatchAccountResultInfoViewModel by viewModels()

    override fun setImageView(imageView: ImageView) {
        with(imageView) {
            setImageResource(R.drawable.ic_check)
            setColorFilter(ContextCompat.getColor(requireContext(), R.color.info_image_color))
        }
    }

    override fun setTitleText(textView: TextView) {
        textView.setText(watchAccountResultInfoViewModel.getPreviewTitle())
    }

    override fun setDescriptionText(textView: TextView) {
        textView.setText(watchAccountResultInfoViewModel.getPreviewDescription())
    }

    override fun setFirstButton(materialButton: MaterialButton) {
        with(materialButton) {
            setText(watchAccountResultInfoViewModel.getPreviewFirstButtonText())
            setOnClickListener { onContinueClick() }
        }
    }

    private fun onContinueClick() {
        if (watchAccountResultInfoViewModel.shouldForceLockNavigation()) {
            navToForceLockNavigation()
        } else {
            navToHomeNavigation()
        }
    }

    private fun navToHomeNavigation() {
        nav(WatchAccountResultInfoFragmentDirections.actionWatchAccountResultInfoFragmentToHomeNavigation())
    }

    private fun navToForceLockNavigation() {
        nav(
            WatchAccountResultInfoFragmentDirections.actionWatchAccountResultInfoFragmentToLockPreferenceNavigation(
                shouldNavigateHome = true
            )
        )
    }
}
