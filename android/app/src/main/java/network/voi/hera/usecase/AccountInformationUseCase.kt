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

package network.voi.hera.usecase

import network.voi.hera.mapper.AccountInformationMapper
import network.voi.hera.models.AccountInformation
import network.voi.hera.models.Result
import network.voi.hera.Repository.AccountRepository
import network.voi.hera.Repository.AccountRepository.Companion.ACCOUNT_NOT_FOUND_ERROR_CODE
import network.voi.hera.utils.DataResource
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow

class AccountInformationUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountInformationMapper: AccountInformationMapper,
    private val assetDetailUseCase: SimpleAssetDetailUseCase
) {

    suspend fun getAccountInformation(publicKey: String, includeClosedAccounts: Boolean = false) = flow {
        accountRepository.getAccountInformation(publicKey, includeClosedAccounts).use(
            onSuccess = {
                val accountInformation = accountInformationMapper.mapToAccountInformation(
                    accountInformationPayload = it.accountInformation,
                    currentRound = it.currentRound
                )
                emit(DataResource.Success(accountInformation))
            },
            onFailed = { exception, code ->
                val dataResource = if (code == ACCOUNT_NOT_FOUND_ERROR_CODE) {
                    DataResource.Success(accountInformationMapper.createEmptyAccountInformation(publicKey))
                } else {
                    DataResource.Error.Api(exception, code)
                }
                emit(dataResource)
            }
        )
    }

    // TODO Return flow and remove accountNotFoundErrorCode duplication
    suspend fun getAccountInformationAndFetchAssets(
        publicKey: String,
        coroutineScope: CoroutineScope,
        includeClosedAccounts: Boolean = false
    ): Result<AccountInformation> {
        lateinit var accountInformationResult: Result<AccountInformation>
        accountRepository.getAccountInformation(publicKey, includeClosedAccounts).use(
            onSuccess = {
                val accountInformationResponse = it.accountInformation
                val assetIds = accountInformationResponse?.allAssetHoldingList
                    ?.mapNotNull { it.assetId }
                    ?.toSet()
                    .orEmpty()
                assetDetailUseCase.cacheIfThereIsNonCachedAsset(assetIds, coroutineScope)
                val accountInformation = accountInformationMapper.mapToAccountInformation(
                    accountInformationPayload = accountInformationResponse,
                    currentRound = it.currentRound
                )
                accountInformationResult = Result.Success(accountInformation)
            },
            onFailed = { exception, code ->
                accountInformationResult = if (code == ACCOUNT_NOT_FOUND_ERROR_CODE) {
                    Result.Success(accountInformationMapper.createEmptyAccountInformation(publicKey))
                } else {
                    Result.Error(exception, code)
                }
            }
        )
        return accountInformationResult
    }
}
