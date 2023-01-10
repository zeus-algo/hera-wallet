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

package network.voi.hera.ui.common.warningconfirmation

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.IconButton
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.ui.common.BaseInfoFragment
import network.voi.hera.ui.common.warningconfirmation.BackupInfoFragmentDirections.Companion.actionBackupInfoFragmentToWriteDownInfoFragment
import network.voi.hera.utils.browser.openRecoveryPassphraseSupportUrl
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackupInfoFragment : BaseInfoFragment() {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val backupInfoViewModel: BackupInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolbar()
    }

    override fun setImageView(imageView: ImageView) {
        val icon = R.drawable.ic_shield
        imageView.setImageResource(icon)
        imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.info_image_color))
    }

    override fun setTitleText(textView: TextView) {
        val title = R.string.create_a_passphrase_backup
        textView.setText(title)
    }

    override fun setDescriptionText(textView: TextView) {
        val description = R.string.creating_a_passphrase_backup
        textView.setText(description)
    }

    override fun setFirstButton(materialButton: MaterialButton) {
        val buttonText = R.string.i_understand
        materialButton.setText(buttonText)
        materialButton.setOnClickListener { navigateToWriteDownFragment() }
    }

    private fun navigateToWriteDownFragment() {
        backupInfoViewModel.logOnboardingIUnderstandClickEvent()
        nav(actionBackupInfoFragmentToWriteDownInfoFragment())
    }

    private fun configureToolbar() {
        getAppToolbar()?.setEndButton(button = IconButton(R.drawable.ic_info, onClick = ::onInfoClick))
    }

    private fun onInfoClick() {
        context?.openRecoveryPassphraseSupportUrl()
    }
}
