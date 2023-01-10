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

import network.voi.hera.models.NextBlockResponse
import network.voi.hera.models.Result
import network.voi.hera.models.SendTransactionResponse
import network.voi.hera.models.TrackTransactionRequest
import network.voi.hera.models.TransactionParams
import network.voi.hera.network.AlgodApi
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.getMessageAsResultError
import network.voi.hera.network.request
import network.voi.hera.network.safeApiCall
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class TransactionsRepository @Inject constructor(
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val algodApi: AlgodApi,
    private val hipoApiErrorHandler: RetrofitErrorHandler
) {

    suspend fun getTransactionParams(): Result<TransactionParams> =
        safeApiCall { requestGetTransactionParams() }

    private suspend fun requestGetTransactionParams(): Result<TransactionParams> {
        with(algodApi.getTransactionParams()) {
            return if (isSuccessful && body() != null) {
                Result.Success(body() as TransactionParams)
            } else {
                Result.Error(Exception())
            }
        }
    }

    suspend fun getWaitForBlock(waitedBlockNumber: Long): Result<NextBlockResponse> =
        safeApiCall { requestGetWaitForBlock(waitedBlockNumber) }

    private suspend fun requestGetWaitForBlock(waitedBlockNumber: Long): Result<NextBlockResponse> {
        with(algodApi.getWaitForBlock(waitedBlockNumber)) {
            return if (isSuccessful && body() != null) {
                Result.Success(body() as NextBlockResponse)
            } else {
                Result.Error(Exception())
            }
        }
    }

    suspend fun sendSignedTransaction(transactionData: ByteArray): Result<SendTransactionResponse> =
        safeApiCall { postSignedTransaction(transactionData) }

    private suspend fun postSignedTransaction(transactionData: ByteArray): Result<SendTransactionResponse> {
        val rawTransactionData = transactionData.toRequestBody("application/x-binary".toMediaTypeOrNull())
        with(algodApi.sendSignedTransaction(rawTransactionData)) {
            return if (isSuccessful && body() != null) {
                Result.Success(body() as SendTransactionResponse)
            } else {
                Result.Error(Exception(errorBody()?.charStream()?.readText()))
            }
        }
    }

    suspend fun postTrackTransaction(trackTransactionRequest: TrackTransactionRequest): Result<Unit> =
        safeApiCall { requestPostTrackTransaction(trackTransactionRequest) }

    private suspend fun requestPostTrackTransaction(trackTransactionRequest: TrackTransactionRequest) = request(
        doRequest = {
            mobileAlgorandApi.trackTransaction(trackTransactionRequest)
        },
        onFailed = { errorResponse ->
            hipoApiErrorHandler.getMessageAsResultError(errorResponse)
        }
    )

    companion object {
        const val DEFAULT_TRANSACTION_COUNT = 15
    }
}
