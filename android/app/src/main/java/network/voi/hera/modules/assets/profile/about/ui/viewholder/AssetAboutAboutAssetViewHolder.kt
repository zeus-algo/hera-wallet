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

package network.voi.hera.modules.assets.profile.about.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import network.voi.hera.R
import network.voi.hera.databinding.ItemAssetAboutAboutAssetBinding
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.modules.assets.profile.about.ui.model.BaseAssetAboutListItem
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.copyToClipboard
import network.voi.hera.utils.toShortenedAddress

class AssetAboutAboutAssetViewHolder(
    private val binding: ItemAssetAboutAboutAssetBinding,
    private val listener: AssetAboutAboutAssetListener
) : BaseViewHolder<BaseAssetAboutListItem>(binding.root) {

    override fun bind(item: BaseAssetAboutListItem) {
        if (item !is BaseAssetAboutListItem.AboutAssetItem) return
        with(item) {
            bindTitle(assetName)
            bindAssetIdGroup(assetId)
            bindCreatorGroup(assetCreatorAddress)
            bindAsaUrlGroup(displayAsaUrl, asaUrl)
            bindShowOnPeraExplorerGroup(peraExplorerUrl)
            bindProjectWebsiteGroup(projectWebsiteUrl)
        }
    }

    private fun bindTitle(assetName: AssetName) {
        binding.aboutTitleTextView.text = binding.root.resources.getString(
            R.string.about_asset_name,
            assetName.getName(binding.root.resources)
        )
    }

    private fun bindAssetIdGroup(assetId: Long?) {
        with(binding) {
            asaIdGroup.isVisible = assetId != null
            asaIdTextView.apply {
                text = assetId.toString()
                assetId?.let { safeAssetId ->
                    setOnLongClickListener { context.copyToClipboard(safeAssetId.toString()); true }
                }
            }
        }
    }

    private fun bindCreatorGroup(creatorAddress: String?) {
        with(binding) {
            creatorGroup.isVisible = !creatorAddress.isNullOrBlank()
            creatorTextview.apply {
                text = creatorAddress.toShortenedAddress()
                creatorAddress?.let { safeCreatorAddress ->
                    setOnClickListener { listener.onCreatorAddressClick(safeCreatorAddress) }
                    setOnLongClickListener { listener.onCreatorAddressLongClick(safeCreatorAddress); true }
                }
            }
        }
    }

    private fun bindAsaUrlGroup(displayAsaUrl: String?, asaUrl: String?) {
        with(binding) {
            asaUrlGroup.isVisible = !asaUrl.isNullOrBlank()
            asaUrlTextview.apply {
                text = displayAsaUrl
                asaUrl?.let { safeAsaUrl ->
                    setOnClickListener { listener.onUrlClick(safeAsaUrl) }
                }
            }
        }
    }

    private fun bindShowOnPeraExplorerGroup(peraExplorerUrl: String?) {
        with(binding) {
            showOnPeraExplorerGroup.isVisible = !peraExplorerUrl.isNullOrBlank()
            peraExplorerUrl?.let { safePeraExplorerUrl ->
                showOnPeraExplorerTextview.setOnClickListener { listener.onUrlClick(safePeraExplorerUrl) }
            }
        }
    }

    private fun bindProjectWebsiteGroup(projectWebsiteUrl: String?) {
        with(binding) {
            projectWebsiteGroup.isVisible = !projectWebsiteUrl.isNullOrBlank()
            projectWebsiteUrl?.let { safeProjectWebsiteUrl ->
                projectWebsiteTextview.setOnClickListener { listener.onUrlClick(safeProjectWebsiteUrl) }
            }
        }
    }

    interface AssetAboutAboutAssetListener {
        fun onUrlClick(url: String)
        fun onCreatorAddressClick(creatorAddress: String)
        fun onCreatorAddressLongClick(creatorAddress: String)
    }

    companion object {
        fun create(parent: ViewGroup, listener: AssetAboutAboutAssetListener): AssetAboutAboutAssetViewHolder {
            val binding = ItemAssetAboutAboutAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AssetAboutAboutAssetViewHolder(binding, listener)
        }
    }
}
