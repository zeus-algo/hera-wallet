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

package network.voi.hera.modules.assets.profile.about.domain.usecase

import network.voi.hera.models.BaseAssetDetail
import network.voi.hera.models.Result
import network.voi.hera.modules.assets.profile.about.domain.repository.AssetAboutRepository
import network.voi.hera.utils.DataResource
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// TODO: No need to return flow from this.
class GetAssetDetailUseCase @Inject constructor(
    @Named(AssetAboutRepository.INJECTION_NAME)
    private val assetAboutRepository: AssetAboutRepository
) {

    suspend fun getAssetDetail(assetId: Long): Flow<DataResource<BaseAssetDetail>> {
        return assetAboutRepository.getAssetDetail(assetId).map {
            when (it) {
                is Result.Success -> DataResource.Success(data = it.data)
                is Result.Error -> DataResource.Error.Api(exception = it.exception, code = it.code)
            }
        }
    }
}
