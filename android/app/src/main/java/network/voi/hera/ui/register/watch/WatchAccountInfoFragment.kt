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

package network.voi.hera.ui.register.watch

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.customviews.WarningTextView
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.IconButton
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.ui.common.BaseInfoFragment
import network.voi.hera.utils.browser.openWatchAccountSupportUrl
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WatchAccountInfoFragment : BaseInfoFragment() {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    private val watchAccountInfoViewModel: WatchAccountInfoViewModel by viewModels()

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolbar()
    }

    override fun setImageView(imageView: ImageView) {
        val icon = R.drawable.ic_eye
        imageView.setImageResource(icon)
        imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.info_image_color))
    }

    override fun setTitleText(textView: TextView) {
        val title = R.string.watch_account
        textView.setText(title)
    }

    override fun setDescriptionText(textView: TextView) {
        val description = R.string.monitor_activity_of
        textView.setText(description)
    }

    override fun setWarningFrame(warningTextView: WarningTextView) {
        warningTextView.visibility = View.VISIBLE
        warningTextView.setText(R.string.if_you_do_not)
    }

    override fun setFirstButton(materialButton: MaterialButton) {
        val buttonText = R.string.create_a_watch
        materialButton.setText(buttonText)
        materialButton.setOnClickListener { navigateToRegisterWatchAccountFragment() }
    }

    private fun navigateToRegisterWatchAccountFragment() {
        watchAccountInfoViewModel.logOnboardingCreateWatchAccountClickEvent()
        nav(WatchAccountInfoFragmentDirections.actionWatchAccountInfoFragmentToRegisterWatchAccountNavigation())
    }

    private fun configureToolbar() {
        getAppToolbar()?.setEndButton(button = IconButton(R.drawable.ic_info, onClick = ::onInfoClick))
    }

    private fun onInfoClick() {
        context?.openWatchAccountSupportUrl()
    }
}
