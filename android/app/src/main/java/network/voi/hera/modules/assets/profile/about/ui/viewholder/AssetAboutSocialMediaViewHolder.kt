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
import network.voi.hera.databinding.ItemAssetAboutSocialMediaBinding
import network.voi.hera.models.BaseViewHolder
import network.voi.hera.modules.assets.profile.about.ui.model.BaseAssetAboutListItem

class AssetAboutSocialMediaViewHolder(
    private val binding: ItemAssetAboutSocialMediaBinding,
    private val listener: AssetAboutSocialMediaListener
) : BaseViewHolder<BaseAssetAboutListItem>(binding.root) {

    override fun bind(item: BaseAssetAboutListItem) {
        if (item !is BaseAssetAboutListItem.SocialMediaItem) return
        bindDiscordItem(item.discordUrl)
        bindTelegramItem(item.telegramUrl)
        bindTwitterItem(item.twitterUrl)
    }

    private fun bindDiscordItem(url: String?) {
        binding.discordTextView.apply {
            isVisible = !url.isNullOrBlank()
            setOnClickListener { listener.onSocialMediaClick(url.orEmpty()) }
        }
    }

    private fun bindTelegramItem(url: String?) {
        binding.telegramTextView.apply {
            isVisible = !url.isNullOrBlank()
            setOnClickListener { listener.onSocialMediaClick(url.orEmpty()) }
        }
    }

    private fun bindTwitterItem(url: String?) {
        binding.twitterTextView.apply {
            isVisible = !url.isNullOrBlank()
            setOnClickListener { listener.onSocialMediaClick(url.orEmpty()) }
        }
    }

    fun interface AssetAboutSocialMediaListener {
        fun onSocialMediaClick(url: String)
    }

    companion object {
        fun create(parent: ViewGroup, listener: AssetAboutSocialMediaListener): AssetAboutSocialMediaViewHolder {
            val binding = ItemAssetAboutSocialMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AssetAboutSocialMediaViewHolder(binding, listener)
        }
    }
}
