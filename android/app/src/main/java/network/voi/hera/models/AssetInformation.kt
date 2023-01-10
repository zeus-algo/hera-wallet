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

package network.voi.hera.models

import android.content.res.Resources
import android.os.Parcelable
import network.voi.hera.assetsearch.domain.model.VerificationTier
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.ALGO_FULL_NAME
import network.voi.hera.utils.ALGO_SHORT_NAME
import network.voi.hera.utils.assetdrawable.BaseAssetDrawableProvider
import network.voi.hera.utils.formatAmount
import java.math.BigInteger
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssetInformation(
    val assetId: Long,
    val creatorPublicKey: String? = null,
    val shortName: String? = null,
    val fullName: String? = null,
    val amount: BigInteger? = null,
    val decimals: Int = 0,
    var assetStatus: AssetStatus = AssetStatus.OWNED_BY_ACCOUNT,
    val prismUrl: String? = null,
    val assetDrawableProvider: BaseAssetDrawableProvider? = null,
    val verificationTier: VerificationTier?
) : Parcelable {

    @IgnoredOnParcel
    val formattedAmount by lazy { amount.formatAmount(decimals) }

    fun isAlgo(): Boolean {
        return assetId == ALGO_ID
    }

    companion object {
        const val ALGO_ID = -7L

        fun getAlgorandAsset(amount: BigInteger = BigInteger.ZERO): AssetInformation {
            return AssetInformation(
                assetId = ALGO_ID,
                fullName = ALGO_FULL_NAME,
                shortName = ALGO_SHORT_NAME,
                decimals = ALGO_DECIMALS,
                amount = amount,
                verificationTier = VerificationTier.TRUSTED
            )
        }

        fun createAssetInformation(
            assetHolding: AssetHolding,
            assetParams: AssetDetail
        ): AssetInformation {
            return AssetInformation(
                assetId = assetHolding.assetId,
                creatorPublicKey = assetParams.assetCreator?.publicKey,
                shortName = assetParams.shortName,
                fullName = assetParams.fullName,
                amount = assetHolding.amount,
                decimals = assetParams.fractionDecimals ?: 0,
                verificationTier = assetParams.verificationTier
            )
        }

        // TODO Remove this function after changing RemoveAssetFlow
        fun createAssetInformation(
            removeAssetItem: BaseRemoveAssetItem.BaseRemovableItem,
            resources: Resources
        ): AssetInformation {
            return with(removeAssetItem) {
                AssetInformation(
                    assetId = id,
                    creatorPublicKey = creatorPublicKey,
                    shortName = shortName.getName(resources),
                    fullName = name.getName(resources),
                    amount = amount,
                    decimals = decimals,
                    verificationTier = (removeAssetItem as? BaseRemoveAssetItem.BaseRemovableItem.RemoveAssetItem)
                        ?.verificationTierConfiguration
                        ?.toVerificationTier()
                )
            }
        }

        // TODO: 18.03.2022 Remove this function after changing TransactionBaseFragment
        fun createAssetInformation(
            baseOwnedAssetData: BaseAccountAssetData.BaseOwnedAssetData,
            assetDrawableProvider: BaseAssetDrawableProvider?
        ): AssetInformation {
            return with(baseOwnedAssetData) {
                AssetInformation(
                    assetId = id,
                    creatorPublicKey = creatorPublicKey,
                    shortName = shortName,
                    fullName = name,
                    amount = amount,
                    decimals = decimals,
                    assetDrawableProvider = assetDrawableProvider,
                    verificationTier = verificationTier,
                    prismUrl = prismUrl
                )
            }
        }
    }
}
