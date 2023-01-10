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

package network.voi.hera.banner.data.repository

import network.voi.hera.banner.data.cache.BannerIdsLocalSource
import network.voi.hera.banner.data.cache.BannerLocalCache
import network.voi.hera.banner.data.mapper.BannerDetailDTOMapper
import network.voi.hera.banner.domain.model.BannerDetailDTO
import network.voi.hera.banner.domain.repository.BannerRepository
import network.voi.hera.models.Result
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.requestWithHipoErrorHandler
import network.voi.hera.utils.CacheResult
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BannerRepositoryImpl @Inject constructor(
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val hipoApiErrorHandler: RetrofitErrorHandler,
    private val bannerDetailDTOMapper: BannerDetailDTOMapper,
    private val bannerLocalCache: BannerLocalCache,
    private val bannerIdsLocalSource: BannerIdsLocalSource
) : BannerRepository {

    override suspend fun cacheBanner(bannerDetailDto: BannerDetailDTO) {
        bannerLocalCache.put(CacheResult.Success.create(bannerDetailDto))
    }

    override suspend fun getBanners(deviceId: String): Result<List<BannerDetailDTO>> {
        return requestWithHipoErrorHandler(hipoApiErrorHandler) { mobileAlgorandApi.getDeviceBanners(deviceId) }
            .map { bannerListResponse ->
                bannerListResponse.bannerDetailResponseList?.mapNotNull { bannerDetailResponse ->
                    bannerDetailDTOMapper.mapToBannerDetailDTO(bannerDetailResponse)
                } ?: emptyList()
            }
    }

    override suspend fun setBannerDismissed(bannerId: Long) {
        bannerIdsLocalSource.saveData(listOf(bannerId))
    }

    override suspend fun removeDismissedBannerFromCache(bannerId: Long) {
        bannerLocalCache.remove(bannerId)
    }

    override suspend fun getDismissedBannerIdList(): List<Long> {
        return bannerIdsLocalSource.getDataOrNull() ?: emptyList()
    }

    override suspend fun clearBannerCache() {
        bannerLocalCache.clear()
    }

    override suspend fun clearDismissedBannerIds() {
        bannerIdsLocalSource.clear()
    }

    override suspend fun getCachedBanner(): Flow<BannerDetailDTO?> {
        return bannerLocalCache.cacheMapFlow.map { cacheResultHashMap ->
            cacheResultHashMap.mapNotNull { it.value.data }.firstOrNull()
        }
    }
}
