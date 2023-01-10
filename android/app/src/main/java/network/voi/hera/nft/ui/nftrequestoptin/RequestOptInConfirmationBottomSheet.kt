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

package network.voi.hera.nft.ui.nftrequestoptin

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import network.voi.hera.MainNavigationDirections
import network.voi.hera.R
import network.voi.hera.models.AnnotatedString
import network.voi.hera.nft.ui.model.RequestOptInConfirmationPreview
import network.voi.hera.utils.BaseDoubleButtonBottomSheet
import network.voi.hera.utils.extensions.collectOnLifecycle
import network.voi.hera.utils.getCustomClickableSpan
import network.voi.hera.utils.getXmlStyledString
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestOptInConfirmationBottomSheet : BaseDoubleButtonBottomSheet() {

    private val requestOptInConfirmationViewModel by viewModels<RequestOptInConfirmationViewModel>()

    override fun setTitleText(textView: TextView) {
        textView.setText(R.string.ask_recipient_to)
    }

    private val requestOptInConfirmationPreviewCollector: suspend (RequestOptInConfirmationPreview) -> Unit = {
        updateUi(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    override fun setDescriptionText(textView: TextView) {
        val linkTextColor = ContextCompat.getColor(textView.context, R.color.link_primary)
        val annotatedString = AnnotatedString(
            stringResId = R.string.you_cant_send,
            replacementList = listOf(
                "nft_name" to requestOptInConfirmationViewModel.getCollectibleDisplayText(),
                "wallet_address" to requestOptInConfirmationViewModel.getReceiverPublicKey()
            ),
            customAnnotationList = listOf(
                "learn_more" to getCustomClickableSpan(linkTextColor) { onLearnMoreButtonClicked() },
            )
        )
        textView.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = context.getXmlStyledString(annotatedString)
        }
    }

    private fun onLearnMoreButtonClicked() {
        nav(
            MainNavigationDirections.actionGlobalSingleButtonBottomSheet(
                titleAnnotatedString = AnnotatedString(R.string.information_about_opt),
                descriptionAnnotatedString = AnnotatedString(R.string.to_avoid_spammy)
            )
        )
    }

    override fun setAcceptButton(materialButton: MaterialButton) {
        materialButton.apply {
            text = getString(R.string.request_opt_in)
            setOnClickListener { requestOptInConfirmationViewModel.sendOptInRequest() }
        }
    }

    override fun setCancelButton(materialButton: MaterialButton) {
        materialButton.apply {
            text = getString(R.string.cancel)
            setOnClickListener { navBack() }
        }
    }

    override fun setIconImageView(imageView: ImageView) {
        imageView.apply {
            setImageResource(R.drawable.ic_info)
            imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.link_primary)
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.collectOnLifecycle(
            requestOptInConfirmationViewModel.requestOptInPreviewFlow,
            requestOptInConfirmationPreviewCollector
        )
    }

    private fun updateUi(requestOptInConfirmationPreview: RequestOptInConfirmationPreview) {
        with(requestOptInConfirmationPreview) {
            setProgressVisibility(isLoadingVisible)
            globalErrorEvent?.consume()?.run {
                showGlobalError(this)
                navBack()
            }
            requestSendSuccessEvent?.consume()?.run { navBack() }
        }
    }
}
