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

import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.ALGO_FULL_NAME
import com.google.gson.annotations.SerializedName

// this data class will be only used in notification. (HIPO back-end stores asset like this)
data class AssetNotificationDescription(
    @SerializedName("asset_id")
    val assetId: Long,
    @SerializedName("asset_name")
    val fullName: String? = null,
    @SerializedName("unit_name")
    val shortName: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("fraction_decimals")
    val decimals: Int?,
    @SerializedName("logo")
    val logoUri: String? = null
) {

    fun convertToAssetInformation(): AssetInformation {
        return AssetInformation(
            assetId = assetId,
            shortName = shortName,
            fullName = fullName,
            decimals = decimals ?: 0,
            verificationTier = null
        )
    }

    companion object {
        fun getAlgorandNotificationDescription(): AssetNotificationDescription {
            return AssetNotificationDescription(
                assetId = AssetInformation.ALGO_ID,
                fullName = ALGO_FULL_NAME,
                decimals = ALGO_DECIMALS
            )
        }
    }
}
