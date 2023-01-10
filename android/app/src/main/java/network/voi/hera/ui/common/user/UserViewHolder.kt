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

package network.voi.hera.ui.common.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import network.voi.hera.R
import network.voi.hera.databinding.ItemUserBinding
import network.voi.hera.models.User
import network.voi.hera.utils.extensions.setContactIconDrawable
import network.voi.hera.utils.toShortenedAddress

class UserViewHolder(
    val binding: ItemUserBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.userImageView.setContactIconDrawable(user.imageUriAsString?.toUri(), R.dimen.account_icon_size_large)
        binding.nameTextView.text = user.name
        binding.addressTextView.text = user.publicKey.toShortenedAddress()
    }

    companion object {
        fun create(parent: ViewGroup): UserViewHolder {
            val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UserViewHolder(binding)
        }
    }
}
