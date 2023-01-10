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

package network.voi.hera.assetsearch.domain.repository

import network.voi.hera.assetsearch.domain.model.AssetSearchDTO
import network.voi.hera.models.Pagination
import network.voi.hera.models.Result

interface AssetSearchRepository {
    suspend fun searchAsset(
        queryText: String,
        hasCollectible: Boolean?,
        availableOnDiscoverMobile: Boolean?
    ): Result<Pagination<AssetSearchDTO>>

    suspend fun getTrendingAssets(): Result<Pagination<AssetSearchDTO>>

    suspend fun getAssetsByUrl(url: String): Result<Pagination<AssetSearchDTO>>

    companion object {
        const val REPOSITORY_INJECTION_NAME = "assetSearchRepositoryInjection"
    }
}
