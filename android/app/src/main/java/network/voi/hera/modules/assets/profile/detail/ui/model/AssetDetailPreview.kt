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

package network.voi.hera.modules.assets.profile.detail.ui.model

import androidx.navigation.NavDirections
import network.voi.hera.assetsearch.ui.model.VerificationTierConfiguration
import network.voi.hera.discover.home.domain.model.TokenDetailInfo
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AnnotatedString
import network.voi.hera.utils.AccountDisplayName
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.Event
import network.voi.hera.utils.assetdrawable.BaseAssetDrawableProvider
import java.math.BigDecimal

data class AssetDetailPreview(
    val assetFullName: AssetName,
    val assetId: Long,
    val formattedPrimaryValue: String,
    val formattedSecondaryValue: String,
    val isAlgo: Boolean,
    val verificationTierConfiguration: VerificationTierConfiguration,
    val accountIconResource: AccountIconResource,
    val accountDisplayName: AccountDisplayName,
    val baseAssetDrawableProvider: BaseAssetDrawableProvider,
    val assetPrismUrl: String?,
    val isQuickActionButtonsVisible: Boolean,
    val isSwapButtonSelected: Boolean,
    val onShowGlobalErrorEvent: Event<Pair<Int, AnnotatedString>>? = null,
    val swapNavigationDirectionEvent: Event<NavDirections>? = null,
    val isMarketInformationVisible: Boolean,
    val formattedAssetPrice: String,
    val isChangePercentageVisible: Boolean,
    val changePercentage: BigDecimal?,
    val changePercentageIcon: Int?,
    val changePercentageTextColor: Int?,
    val navigateToDiscoverMarket: Event<TokenDetailInfo>? = null
)
