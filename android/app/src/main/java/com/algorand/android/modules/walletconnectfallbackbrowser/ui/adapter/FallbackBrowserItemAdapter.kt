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

package com.algorand.android.modules.walletconnectfallbackbrowser.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.algorand.android.models.BaseDiffUtil
import com.algorand.android.modules.walletconnectfallbackbrowser.ui.model.FallbackBrowserListItem
import com.algorand.android.modules.walletconnectfallbackbrowser.ui.viewholder.FallbackBrowserItemViewHolder

class FallbackBrowserItemAdapter(
    private val listener: Listener
) : ListAdapter<FallbackBrowserListItem, FallbackBrowserItemViewHolder>(BaseDiffUtil()) {

    private val fallbackBrowserItemViewHolderListener = FallbackBrowserItemViewHolder.Listener { browserItem ->
        listener.onBrowserSelected(browserItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FallbackBrowserItemViewHolder {
        return FallbackBrowserItemViewHolder.create(parent, fallbackBrowserItemViewHolderListener)
    }

    override fun onBindViewHolder(holder: FallbackBrowserItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun interface Listener {
        fun onBrowserSelected(browserListItem: FallbackBrowserListItem)
    }
}
