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

package network.voi.hera.ui.send.confirmation.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import network.voi.hera.R
import network.voi.hera.SendAlgoNavigationDirections
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentTransactionConfirmationBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.ui.send.confirmation.ui.model.TransactionStatusPreview
import network.voi.hera.utils.setFragmentNavigationResult
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TransactionConfirmationFragment : BaseFragment(R.layout.fragment_transaction_confirmation) {

    override val fragmentConfiguration = FragmentConfiguration()

    private val binding by viewBinding(FragmentTransactionConfirmationBinding::bind)

    private val transactionConfirmationViewModel by viewModels<TransactionConfirmationViewModel>()

    private val transactionStatusPreviewFlowCollector: suspend (TransactionStatusPreview) -> Unit = { preview ->
        updatePreview(preview)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLottieAnimatorListener()
        initObservers()
    }

    private fun setLottieAnimatorListener() {
        binding.transactionStatusLottieView.addAnimatorUpdateListener { valueAnimator ->
            valueAnimator.doOnEnd { transactionConfirmationViewModel.onTransactionIsLoaded() }
        }
    }

    private fun updatePreview(preview: TransactionStatusPreview) {
        with(preview) {
            with(binding) {
                transactionStatusLottieView.apply {
                    transactionStatusAnimationResId?.run {
                        setAnimation(transactionStatusAnimationResId)
                    }
                    transactionStatusAnimationDrawableResId?.run {
                        setImageDrawable(ContextCompat.getDrawable(context, this))
                    }
                    transactionStatusAnimationDrawableTintResId?.run {
                        imageTintList = getColorStateList(this)
                    }
                    setBackgroundResource(transactionStatusAnimationBackgroundResId)
                    backgroundTintList = getColorStateList(transactionStatusAnimationBackgroundTintResId)
                }
                transactionTitleTextView.setText(transactionStatusTitleResId)
                transactionInfoTextView.setText(transactionStatusDescriptionResId)
            }
            onExitSendAlgoNavigationEvent?.consume()?.run { popSendAlgoNavigation() }
        }
    }

    // TODO: we can create an extension function
    private fun getColorStateList(@ColorRes colorResId: Int): ColorStateList {
        return ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, colorResId))
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            transactionConfirmationViewModel.transactionStatusPreviewFlow.collectLatest(
                transactionStatusPreviewFlowCollector
            )
        }
    }

    private fun popSendAlgoNavigation() {
        // TODO: use new extension function to return fragment result
        setFragmentNavigationResult(TRANSACTION_CONFIRMATION_KEY, true)
        nav(SendAlgoNavigationDirections.actionSendAlgoNavigationPop())
    }

    companion object {
        const val TRANSACTION_CONFIRMATION_KEY = "transaction_confirmation_key"
    }
}
