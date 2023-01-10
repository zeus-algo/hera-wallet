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

package network.voi.hera.modules.collectibles.profile.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import network.voi.hera.R
import network.voi.hera.models.AccountIconResource
import network.voi.hera.modules.assets.profile.asaprofile.ui.model.AsaStatusPreview
import network.voi.hera.modules.collectibles.detail.base.ui.BaseCollectibleDetailFragment
import network.voi.hera.modules.collectibles.profile.ui.model.CollectibleProfilePreview
import network.voi.hera.utils.AccountIconDrawable
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.extensions.show
import network.voi.hera.utils.openTextShareBottomMenuChooser
import network.voi.hera.utils.setDrawable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectibleProfileFragment : BaseCollectibleDetailFragment() {

    override val baseCollectibleDetailViewModel: CollectibleProfileViewModel by viewModels()

    private val collectibleProfileCollector: suspend (CollectibleProfilePreview?) -> Unit = {
        if (it != null) initCollectibleProfilePreview(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initObservers()
    }

    override fun initObservers() {
        collectLatestOnLifecycle(
            flow = baseCollectibleDetailViewModel.collectibleProfilePreviewFlow,
            collection = collectibleProfileCollector
        )
    }

    private fun initCollectibleProfilePreview(collectibleProfilePreview: CollectibleProfilePreview) {
        with(collectibleProfilePreview) {
            setCollectibleMedias(mediaListOfNFT)
            setPrimaryWarningText(primaryWarningResId)
            setSecondaryWarningText(secondaryWarningResId)
            setCollectionName(collectionNameOfNFT)
            setNFTName(nftName)
            setNFTDescription(nftDescription)
            setNFTId(nftId)
            setCollectibleAssetIdClickListener(nftId, accountAddress)
            setNFTCreatorAccount(creatorAccountAddressOfNFT, formattedCreatorAccountAddressOfNFT)
            setNFTTraits(traitListOfNFT)
            setShowOnPeraExplorer(peraExplorerUrl)
            setProgressBarVisibility(isLoadingVisible)
            setAsaStatusPreview(collectibleStatusPreview)
        }
    }

    private fun setAsaStatusPreview(collectibleStatusPreview: AsaStatusPreview?) {
        with(collectibleStatusPreview ?: return) {
            binding.collectibleStatusConstraintLayout.root.show()
            updateBottomPadding()
            initAsaStatusValue(this)
            initAsaStatusLabel(statusLabelTextResId)
            initAsaStatusActionButton(this)
        }
    }

    private fun initAsaStatusLabel(@StringRes statusLabelTextResId: Int) {
        binding.collectibleStatusConstraintLayout.statusLabelTextView.setText(statusLabelTextResId)
    }

    private fun initAsaStatusValue(asaStatusPreview: AsaStatusPreview) {
        binding.collectibleStatusConstraintLayout.statusValueTextView.apply {
            when (asaStatusPreview) {
                is AsaStatusPreview.AdditionStatus -> {
                    isVisible = asaStatusPreview.accountName != null
                    text = asaStatusPreview.accountName?.getDisplayAddress()
                    setDrawable(
                        start = AccountIconDrawable.create(
                            context = context,
                            accountIconResource = asaStatusPreview.accountName?.accountIconResource
                                ?: AccountIconResource.DEFAULT_ACCOUNT_ICON_RESOURCE,
                            size = resources.getDimension(R.dimen.account_icon_size_small).toInt()
                        )
                    )
                    setOnLongClickListener {
                        onAccountAddressCopied(asaStatusPreview.accountName?.publicKey.orEmpty())
                        true
                    }
                }
                is AsaStatusPreview.RemovalStatus.CollectibleRemovalStatus -> {
                    isVisible = asaStatusPreview.accountName != null
                    text = asaStatusPreview.accountName?.getDisplayAddress()
                    setDrawable(
                        start = AccountIconDrawable.create(
                            context = context,
                            accountIconResource = asaStatusPreview.accountName?.accountIconResource
                                ?: AccountIconResource.DEFAULT_ACCOUNT_ICON_RESOURCE,
                            size = resources.getDimension(R.dimen.account_icon_size_small).toInt()
                        )
                    )
                    setOnLongClickListener {
                        onAccountAddressCopied(asaStatusPreview.accountName?.publicKey.orEmpty())
                        true
                    }
                }
                is AsaStatusPreview.AccountSelectionStatus -> {
                    // Account should be already selected in this fragment. Nothing to do until a flow change
                }
                is AsaStatusPreview.TransferStatus -> {
                    // No transfer action for collectible profile screen
                }
                is AsaStatusPreview.RemovalStatus.AssetRemovalStatus -> {
                    // No action for asset removal status case
                }
            }
        }
    }

    private fun initAsaStatusActionButton(
        asaStatusPreview: AsaStatusPreview
    ) {
        with(asaStatusPreview.peraButtonState) {
            with(binding.collectibleStatusConstraintLayout.assetStatusActionButton) {
                setIconDrawable(iconResourceId = iconDrawableResId)
                setBackgroundColor(colorResId = backgroundColorResId)
                setIconTint(iconTintResId = iconTintColorResId)
                setText(textResId = asaStatusPreview.actionButtonTextResId)
                setButtonStroke(colorResId = strokeColorResId)
                setButtonTextColor(colorResId = textColor)
                setOnClickListener { onAsaActionButtonClick(asaStatusPreview) }
            }
        }
    }

    private fun onAsaActionButtonClick(asaStatusPreview: AsaStatusPreview) {
        when (asaStatusPreview) {
            is AsaStatusPreview.AdditionStatus -> navToAssetAdditionFlow()
            is AsaStatusPreview.RemovalStatus -> navToAssetRemovalFlow()
            is AsaStatusPreview.AccountSelectionStatus -> {
                // Account should be already selected in this fragment. Nothing to do until a flow change
            }
            is AsaStatusPreview.TransferStatus -> {
                // No transfer action for collectible profile screen
            }
        }
    }

    private fun navToAssetAdditionFlow() {
        val assetAction = baseCollectibleDetailViewModel.getAssetAction()
        nav(
            CollectibleProfileFragmentDirections.actionCollectibleProfileFragmentToCollectibleOptInActionNavigation(
                assetAction = assetAction
            )
        )
    }

    private fun navToAssetRemovalFlow() {
        val assetAction = baseCollectibleDetailViewModel.getAssetAction()
        nav(
            CollectibleProfileFragmentDirections.actionCollectibleProfileFragmentToNftOptOutConfirmationNavigation(
                assetAction = assetAction
            )
        )
    }

    override fun navToVideoPlayerFragment(videoUrl: String) {
        nav(CollectibleProfileFragmentDirections.actionCollectibleProfileFragmentToVideoPlayerNavigation(videoUrl))
    }

    override fun navToAudioPlayerFragment(audioUrl: String) {
        nav(CollectibleProfileFragmentDirections.actionCollectibleProfileFragmentToAudioPlayerNavigation(audioUrl))
    }

    override fun copyOptedInAccountAddress() {
        onAccountAddressCopied(baseCollectibleDetailViewModel.accountAddress)
    }

    override fun onShareButtonClick() {
        context?.openTextShareBottomMenuChooser(
            title = baseCollectibleDetailViewModel.getNFTName()?.getName(resources).orEmpty(),
            text = baseCollectibleDetailViewModel.getNFTExplorerUrl().orEmpty()
        )
    }

    override fun navToImagePreviewFragment(
        imageUrl: String,
        view: View,
        cachedMediaUri: String
    ) {
        exitTransition = getImageDetailTransitionAnimation(isGrowing = false)
        reenterTransition = getImageDetailTransitionAnimation(isGrowing = true)
        val transitionName = view.transitionName
        nav(
            directions = CollectibleProfileFragmentDirections
                .actionCollectibleProfileFragmentToCollectibleImagePreviewNavigation(
                    transitionName = transitionName,
                    imageUri = imageUrl,
                    cachedMediaUri = cachedMediaUri
                ),
            extras = FragmentNavigatorExtras(view to transitionName)
        )
    }

    private fun setCollectibleAssetIdClickListener(collectibleAssetId: Long, address: String) {
        binding.assetIdTextView.setOnClickListener {
            nav(
                CollectibleProfileFragmentDirections.actionCollectibleProfileFragmentToAssetProfileNavigation(
                    assetId = collectibleAssetId,
                    accountAddress = address
                )
            )
        }
    }

    override fun onNavBack() {
        navBack()
    }
}
