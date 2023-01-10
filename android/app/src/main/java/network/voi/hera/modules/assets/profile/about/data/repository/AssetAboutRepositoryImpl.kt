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

package network.voi.hera.modules.assets.profile.about.data.repository

import network.voi.hera.mapper.AssetDetailMapper
import network.voi.hera.models.BaseAssetDetail
import network.voi.hera.models.Result
import network.voi.hera.modules.assets.profile.about.data.local.AsaProfileDetailSingleLocalCache
import network.voi.hera.modules.assets.profile.about.domain.repository.AssetAboutRepository
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.request
import network.voi.hera.nft.domain.mapper.SimpleCollectibleDetailMapper
import network.voi.hera.utils.CacheResult
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

class AssetAboutRepositoryImpl constructor(
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val assetDetailMapper: AssetDetailMapper,
    private val simpleCollectibleDetailMapper: SimpleCollectibleDetailMapper,
    private val asaProfileDetailSingleLocalCache: AsaProfileDetailSingleLocalCache
) : AssetAboutRepository {

    override suspend fun getAssetDetail(assetId: Long) = flow {
        request { mobileAlgorandApi.getAssetDetail(assetId) }.use(
            onSuccess = { assetDetailResponse ->
                val assetDetail = assetDetailMapper.mapToAssetDetail(assetDetailResponse)
                emit(Result.Success(assetDetail))
            },
            onFailed = { exception, code ->
                emit(Result.Error(exception, code))
            }
        )
    }

    override suspend fun cacheAssetDetailToAsaProfileLocalCache(assetId: Long) {
        request { mobileAlgorandApi.getAssetDetail(assetId) }.use(
            onSuccess = { assetDetailResponse ->
                val baseAssetDetail = if (assetDetailResponse.collectible != null) {
                    simpleCollectibleDetailMapper.mapToCollectibleDetail(assetDetailResponse)
                } else {
                    assetDetailMapper.mapToAssetDetail(assetDetailResponse)
                }
                asaProfileDetailSingleLocalCache.put(CacheResult.Success.create(baseAssetDetail))
            },
            onFailed = { exception, code ->
                asaProfileDetailSingleLocalCache.put(CacheResult.Error.create(exception, code))
            }
        )
    }

    override fun getAssetDetailFlowFromAsaProfileLocalCache(): StateFlow<CacheResult<BaseAssetDetail>?> {
        return asaProfileDetailSingleLocalCache.cacheFlow
    }

    override fun clearAsaProfileLocalCache() {
        asaProfileDetailSingleLocalCache.clear()
    }
}
