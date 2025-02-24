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

package com.algorand.android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// this user is targetted to be sent.
@Parcelize
data class TargetUser(
    val contact: User? = null,
    val publicKey: String,
    val account: AccountCacheData? = null,
    val nftDomainAddress: String? = null,
    val nftDomainServiceLogoUrl: String? = null
) : Parcelable
