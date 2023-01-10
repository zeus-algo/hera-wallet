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

package network.voi.hera.modules.assets.action.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import network.voi.hera.R
import network.voi.hera.assetsearch.domain.model.VerificationTier
import network.voi.hera.assetsearch.ui.model.VerificationTierConfiguration
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.AssetInformation
import network.voi.hera.modules.assets.profile.about.domain.usecase.GetAssetDetailUseCase
import network.voi.hera.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.Resource
import network.voi.hera.utils.exception.AssetNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseAssetActionViewModel constructor(
    private val assetDetailUseCase: SimpleAssetDetailUseCase,
    private val simpleCollectibleUseCase: SimpleCollectibleUseCase,
    private val getAssetDetailUseCase: GetAssetDetailUseCase,
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider
) : BaseViewModel() {

    abstract val assetId: Long

    val assetInformationLiveData = MutableLiveData<Resource<AssetInformation>>()

    // TODO: Move this into UseCase
    protected fun fetchAssetDescription(assetId: Long) {
        assetInformationLiveData.value = Resource.Loading
        val isInAssetCache = assetDetailUseCase.isAssetCached(assetId)
        val isInCollectibleCache = simpleCollectibleUseCase.isCollectibleCached(assetId)
        viewModelScope.launch(Dispatchers.IO) {
            when {
                isInAssetCache -> {
                    val assetDetail = assetDetailUseCase.getCachedAssetDetail(assetId)?.data
                    assetInformationLiveData.postValue(Resource.Success(assetDetail?.convertToAssetInformation()))
                }
                isInCollectibleCache -> {
                    val collectibleDetail = simpleCollectibleUseCase.getCachedCollectibleById(assetId)?.data
                    assetInformationLiveData.postValue(Resource.Success(collectibleDetail?.convertToAssetInformation()))
                }
                else -> {
                    getAssetDetailUseCase.getAssetDetail(assetId).collect { dataResource ->
                        dataResource.useSuspended(
                            onSuccess = { assetDetail ->
                                val assetInformation = assetDetail.convertToAssetInformation()
                                assetInformationLiveData.postValue(Resource.Success(assetInformation))
                            },
                            onFailed = {
                                val errorResourceId = if (it.exception is AssetNotFoundException) {
                                    R.string.asset_not_found_please_make
                                } else {
                                    R.string.an_error_occured
                                }
                                val annotatedErrorString = AnnotatedString(errorResourceId)
                                assetInformationLiveData.postValue(Resource.Error.Annotated(annotatedErrorString))
                            }
                        )
                    }
                }
            }
        }
    }

    fun getVerificationTierConfiguration(verificationTier: VerificationTier?): VerificationTierConfiguration {
        return verificationTierConfigurationDecider.decideVerificationTierConfiguration(verificationTier)
    }

    protected companion object {
        const val ASSET_ACTION_KEY = "assetAction"
        const val SHOULD_WAIT_FOR_CONFIRMATION_KEY = "shouldWaitForConfirmation"
        const val DEFAULT_WAIT_FOR_CONFIRMATION_PARAM = false
    }
}
