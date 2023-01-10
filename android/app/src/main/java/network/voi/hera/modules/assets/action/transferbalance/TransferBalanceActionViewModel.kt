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

package network.voi.hera.modules.assets.action.transferbalance

import androidx.lifecycle.SavedStateHandle
import network.voi.hera.models.AssetAction
import network.voi.hera.models.BaseAccountAddress
import network.voi.hera.modules.assets.action.base.BaseAssetActionViewModel
import network.voi.hera.modules.assets.profile.about.domain.usecase.GetAssetDetailUseCase
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.AccountAddressUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransferBalanceActionViewModel @Inject constructor(
    private val accountAddressUseCase: AccountAddressUseCase,
    assetDetailUseCase: SimpleAssetDetailUseCase,
    simpleCollectibleUseCase: SimpleCollectibleUseCase,
    getAssetDetailUseCase: GetAssetDetailUseCase,
    verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    savedStateHandle: SavedStateHandle
) : BaseAssetActionViewModel(
    assetDetailUseCase,
    simpleCollectibleUseCase,
    getAssetDetailUseCase,
    verificationTierConfigurationDecider
) {

    private val assetAction: AssetAction = savedStateHandle.getOrThrow(ASSET_ACTION_KEY)
    val accountAddress: String = assetAction.publicKey.orEmpty()

    override val assetId: Long = assetAction.assetId
    val assetFullName = AssetName.create(assetAction.asset?.fullName)

    init {
        fetchAssetDescription(assetId)
    }

    // TODO: Create [AssetActionUseCase] and get the whole UI related things from there
    fun getAccountName(): BaseAccountAddress.AccountAddress {
        return accountAddressUseCase.createAccountAddress(accountAddress)
    }
}
