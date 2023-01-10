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

import android.os.Parcelable
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.Participation.Companion.DEFAULT_PARTICIPATION_KEY
import network.voi.hera.utils.AccountCacheManager
import network.voi.hera.utils.calculateMinBalance
import java.math.BigInteger
import java.math.BigInteger.ZERO
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountInformation(
    val address: String,
    val amount: BigInteger,
    val participation: Participation?,
    val rekeyAdminAddress: String?,
    private val allAssetHoldingList: MutableSet<AssetHolding>,
    val createdAtRound: Long?,
    val appsLocalState: List<CreatedAppLocalState>? = null,
    val appsTotalSchema: CreatedAppStateScheme? = null,
    val appsTotalExtraPages: Int? = null,
    val totalCreatedApps: Int = 0,
    val lastFetchedRound: Long?
) : Parcelable {

    val assetHoldingList: List<AssetHolding>
        get() = allAssetHoldingList.filterNot { it.isDeleted }

    fun isCreated(): Boolean {
        return createdAtRound != null
    }

    fun setAssetHoldingStatus(assetId: Long, status: AssetStatus) {
        allAssetHoldingList.firstOrNull { it.assetId == assetId }?.status = status
    }

    fun addPendingAssetHolding(assetHolding: AssetHolding) {
        if (!AssetStatus.isPending(assetHolding.status)) return
        allAssetHoldingList.add(assetHolding)
    }

    fun isRekeyed(): Boolean {
        return !rekeyAdminAddress.isNullOrEmpty() && rekeyAdminAddress != address
    }

    fun getAssetInformationList(accountCacheManager: AccountCacheManager): MutableList<AssetInformation> {
        val assetInformationList = mutableListOf<AssetInformation>()
        assetInformationList.add(
            AssetInformation.getAlgorandAsset(amount)
        )
        assetHoldingList.forEach { assetHolding ->
            accountCacheManager.getAssetDescription(assetHolding.assetId)?.let { assetDescription ->
                assetInformationList.add(AssetInformation.createAssetInformation(assetHolding, assetDescription))
            }
        }
        return assetInformationList
    }

    fun getAllAssetIds(): List<Long> {
        return assetHoldingList.map { it.assetId }
    }

    fun getAllAssetIdsIncludeAlgorand(): List<Long> {
        return assetHoldingList.map { it.assetId }.toMutableList().apply { add(0, ALGO_ID) }
    }

    fun getOptedInAssetsCount() = allAssetHoldingList.size

    fun getMinAlgoBalance(): BigInteger {
        return calculateMinBalance(
            this,
            isRekeyed() || isThereAnyDifferentAsset() || isThereAnOptedInApp()
        ).toBigInteger()
    }

    fun isAssetSupported(assetId: Long): Boolean {
        return assetId == ALGO_ID || assetHoldingList.any { it.assetId == assetId }
    }

    fun getBalance(assetId: Long): BigInteger {
        return if (assetId == ALGO_ID) {
            amount
        } else {
            assetHoldingList.firstOrNull { it.assetId == assetId }?.amount ?: ZERO
        }
    }

    fun doesUserHasParticipationKey() =
        !(participation == null || participation.voteParticipationKey == DEFAULT_PARTICIPATION_KEY)

    fun isThereAnyDifferentAsset() = assetHoldingList.isNotEmpty()

    fun isThereAnOptedInApp() = appsLocalState?.isNotEmpty() == true || totalCreatedApps > 0
}
