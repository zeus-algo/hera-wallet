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

package network.voi.hera.Repository

import network.voi.hera.cache.SimpleAssetLocalCache
import network.voi.hera.models.Asset
import network.voi.hera.models.AssetDetail
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.AssetSupportRequest
import network.voi.hera.models.Result
import network.voi.hera.network.IndexerApi
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.requestWithHipoErrorHandler
import network.voi.hera.network.safeApiCall
import network.voi.hera.utils.AlgoAssetInformationProvider
import network.voi.hera.utils.CacheResult
import network.voi.hera.utils.toQueryString
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import javax.inject.Inject

class AssetRepository @Inject constructor(
    private val indexerApi: IndexerApi,
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val hipoApiErrorHandler: RetrofitErrorHandler,
    private val simpleAssetLocalCache: SimpleAssetLocalCache,
    private val algoAssetInformationProvider: AlgoAssetInformationProvider
) {
    suspend fun fetchAssetsById(assetIdList: List<Long>, includeDeleted: Boolean? = null) =
        requestWithHipoErrorHandler(hipoApiErrorHandler) {
            mobileAlgorandApi.getAssetsByIds(assetIdList.toQueryString(), includeDeleted)
        }

    suspend fun postAssetSupportRequest(assetSupportRequest: AssetSupportRequest): Result<Unit> {
        return safeApiCall { requestPostAssetSupportRequest(assetSupportRequest) }
    }

    private suspend fun requestPostAssetSupportRequest(assetSupportRequest: AssetSupportRequest) =
        requestWithHipoErrorHandler(hipoApiErrorHandler) {
            mobileAlgorandApi.postAssetSupportRequest(assetSupportRequest)
        }

    suspend fun getAssetDescription(assetId: Long): Result<Asset> =
        safeApiCall { requestGetAssetDescription(assetId) }

    private suspend fun requestGetAssetDescription(assetId: Long): Result<Asset> {
        with(indexerApi.getAssetDescription(assetId)) {
            return if (isSuccessful && this.body() != null) {
                val response = body()?.asset
                if (response != null) {
                    Result.Success(response)
                } else {
                    Result.Error(
                        Exception(
                            "Api response returned empty body while trying to fetch asset description, assetId $assetId"
                        )
                    )
                }
            } else {
                Result.Error(Exception())
            }
        }
    }

    suspend fun cacheAsset(asset: CacheResult.Success<AssetDetail>) {
        simpleAssetLocalCache.put(asset)
    }

    suspend fun cacheAsset(assetId: Long, asset: CacheResult.Error<AssetDetail>) {
        simpleAssetLocalCache.put(assetId, asset)
    }

    suspend fun cacheAssets(assetList: List<CacheResult.Success<AssetDetail>>) {
        simpleAssetLocalCache.put(assetList)
    }

    suspend fun cacheAllAssets(assetKeyValuePairList: List<Pair<Long, CacheResult<AssetDetail>>>) {
        simpleAssetLocalCache.putAll(assetKeyValuePairList)
    }

    fun getAssetCacheFlow() = simpleAssetLocalCache.cacheMapFlow

    fun getCachedAssetById(assetId: Long): CacheResult<AssetDetail>? {
        return if (assetId == ALGO_ID) {
            algoAssetInformationProvider.getAlgoAssetInformation()
        } else {
            simpleAssetLocalCache.getOrNull(assetId)
        }
    }

    suspend fun clearAssetCache() {
        simpleAssetLocalCache.clear()
    }

    suspend fun clearAssetCache(assetId: Long) {
        simpleAssetLocalCache.remove(assetId)
    }
}
