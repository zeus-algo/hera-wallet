package network.voi.hera.modules.collectibles.action.optin

import androidx.lifecycle.SavedStateHandle
import network.voi.hera.models.AssetAction
import network.voi.hera.models.BaseAccountAddress
import network.voi.hera.modules.assets.action.base.BaseAssetActionViewModel
import network.voi.hera.modules.assets.profile.about.domain.usecase.GetAssetDetailUseCase
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.AccountAddressUseCase
import network.voi.hera.usecase.GetFormattedTransactionFeeAmountUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.getOrThrow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CollectibleOptInActionViewModel @Inject constructor(
    private val accountAddressUseCase: AccountAddressUseCase,
    private val getFormattedTransactionFeeAmountUseCase: GetFormattedTransactionFeeAmountUseCase,
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
    val assetName: AssetName = AssetName.create(assetAction.asset?.fullName)
    override val assetId: Long = assetAction.assetId

    init {
        fetchAssetDescription(assetId)
    }

    // TODO: Create [AssetActionUseCase] and get the whole UI related things from there
    fun getAccountName(): BaseAccountAddress.AccountAddress {
        return accountAddressUseCase.createAccountAddress(accountAddress)
    }

    fun getTransactionFee(): String {
        return getFormattedTransactionFeeAmountUseCase.getTransactionFee()
    }
}
