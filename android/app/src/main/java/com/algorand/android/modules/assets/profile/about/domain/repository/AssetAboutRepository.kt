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

package com.algorand.android.modules.assets.profile.about.domain.repository

import com.algorand.android.models.BaseAssetDetail
import com.algorand.android.models.Result
import com.algorand.android.utils.CacheResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AssetAboutRepository {

    // TODO: Remove this function here and create another repository and use case
    suspend fun getAssetDetail(assetId: Long): Flow<Result<BaseAssetDetail>>
    suspend fun cacheAssetDetailToAsaProfileLocalCache(assetId: Long)
    fun getAssetDetailFlowFromAsaProfileLocalCache(): StateFlow<CacheResult<BaseAssetDetail>?>
    fun clearAsaProfileLocalCache()

    companion object {
        const val INJECTION_NAME = "assetAboutRepository"
    }
}
