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

package network.voi.hera.modules.webexport.accountconfirmation.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.core.DaggerBaseFragment
import network.voi.hera.databinding.FragmentWebExportAccountConfirmationBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.webexport.accountconfirmation.ui.adapter.WebExportAccountConfirmationAdapter
import network.voi.hera.modules.webexport.accountconfirmation.ui.model.WebExportAccountConfirmationPreview
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebExportAccountConfirmationFragment : DaggerBaseFragment(R.layout.fragment_web_export_account_confirmation) {

    private val webExportAccountConfirmationViewModel: WebExportAccountConfirmationViewModel by viewModels()

    private val webExportAccountConfirmationAdapter: WebExportAccountConfirmationAdapter =
        WebExportAccountConfirmationAdapter()

    private val webExportAccountConfirmationPreviewCollector:
        suspend (preview: WebExportAccountConfirmationPreview) -> Unit = {
            with(binding) {
                progressbar.root.isVisible = it.isLoadingVisible
            }

            webExportAccountConfirmationAdapter.submitList(it.listItems)
            it.globalErrorEvent?.consume()?.let { errorString ->
                showGlobalError(errorString)
            }
            it.requestSendSuccessEvent?.consume()?.let {
                // We navigate in any case if we get a response
                nav(
                    WebExportAccountConfirmationFragmentDirections
                        .actionWebExportAccountConfirmationFragmentToWebExportSuccessResultFragment()
                )
            }
        }

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration =
        FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val binding by viewBinding(FragmentWebExportAccountConfirmationBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
    }

    private fun initObservers() {
        with(webExportAccountConfirmationViewModel) {
            viewLifecycleOwner.collectLatestOnLifecycle(
                webExportAccountConfirmationPreviewFlow,
                webExportAccountConfirmationPreviewCollector
            )
        }
    }

    private fun initUi() {
        with(binding) {
            continueButton.setOnClickListener { onContinueButtonClicked() }
            cancelButton.setOnClickListener { navBack() }
            accountConfirmationRecyclerview.adapter = webExportAccountConfirmationAdapter
        }
    }

    private fun onContinueButtonClicked() {
        webExportAccountConfirmationViewModel.onConfirmExport()
    }
}
