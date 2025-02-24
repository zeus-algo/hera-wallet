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

package com.algorand.android.customviews.accountasseticonnameitem.model

import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.algorand.android.models.AccountIconResource

data class AccountAssetIconNameConfiguration(
    val startAccountIconResource: AccountIconResource? = null,
    @DrawableRes val setStartIconResId: Int? = null,
    val startSmallIconDrawable: Drawable? = null,
    @DrawableRes val startSmallIconResId: Int? = null,
    val titleEndIconDrawable: Drawable? = null,
    @DrawableRes val titleEndIconResId: Int? = null,
    val title: String? = null,
    @StringRes val titleResId: Int? = null,
    @ColorRes val titleTextColorResId: Int? = null,
    val description: String? = null,
    @StringRes val descriptionResId: Int? = null
)
