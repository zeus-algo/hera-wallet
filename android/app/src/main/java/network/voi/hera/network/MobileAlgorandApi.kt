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

package network.voi.hera.network

import network.voi.hera.banner.data.model.BannerListResponse
import network.voi.hera.deviceregistration.data.model.DeviceRegistrationRequest
import network.voi.hera.deviceregistration.data.model.DeviceRegistrationResponse
import network.voi.hera.deviceregistration.data.model.DeviceUpdateRequest
import network.voi.hera.models.AssetDetailResponse
import network.voi.hera.models.AssetSearchResponse
import network.voi.hera.models.AssetSupportRequest
import network.voi.hera.models.Feedback
import network.voi.hera.models.FeedbackCategory
import network.voi.hera.models.NotificationFilterRequest
import network.voi.hera.models.NotificationItem
import network.voi.hera.models.Pagination
import network.voi.hera.models.PushTokenDeleteRequest
import network.voi.hera.modules.accountblockpolling.data.model.ShouldRefreshRequestBody
import network.voi.hera.modules.accountblockpolling.data.model.ShouldRefreshResponse
import network.voi.hera.models.TrackTransactionRequest
import network.voi.hera.models.VerifiedAssetDetail
import network.voi.hera.modules.walletconnect.subscription.data.model.WalletConnectSessionSubscriptionBody
import network.voi.hera.modules.assets.addition.base.ui.BaseAddAssetViewModel.Companion.SEARCH_RESULT_LIMIT
import network.voi.hera.modules.accounts.data.model.LastSeenNotificationRequest
import network.voi.hera.modules.accounts.data.model.LastSeenNotificationResponse
import network.voi.hera.modules.accounts.data.model.NotificationStatusResponse
import network.voi.hera.modules.currency.data.model.CurrencyOptionResponse
import network.voi.hera.modules.nftdomain.data.model.NftDomainSearchResponse
import network.voi.hera.modules.parity.data.model.CurrencyDetailResponse
import network.voi.hera.modules.swap.assetselection.toasset.data.model.AvailableSwapAssetListResponse
import network.voi.hera.modules.swap.confirmswap.data.model.CreateSwapQuoteTransactionsRequestBody
import network.voi.hera.modules.swap.confirmswap.data.model.CreateSwapQuoteTransactionsResponse
import network.voi.hera.modules.swap.assetswap.data.model.PeraFeeRequestBody
import network.voi.hera.modules.swap.assetswap.data.model.PeraFeeResponse
import network.voi.hera.modules.swap.assetswap.data.model.SwapQuoteRequestBody
import network.voi.hera.modules.swap.assetswap.data.model.SwapQuoteResultResponse
import network.voi.hera.modules.webexport.model.WebBackupRequestBody
import network.voi.hera.modules.webexport.accountconfirmation.data.model.ExportBackupResponse
import network.voi.hera.network.MobileHeaderInterceptor.Companion.ALGORAND_NETWORK_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MobileAlgorandApi {

    @POST("v1/feedback/")
    suspend fun postFeedback(@Body feedback: Feedback): Response<Unit>

    @GET("v1/feedback/categories/")
    suspend fun getFeedbackCategories(): Response<List<FeedbackCategory>>

    @POST("v1/devices/")
    suspend fun postRegisterDevice(
        @Body deviceRegistrationRequest: DeviceRegistrationRequest
    ): Response<DeviceRegistrationResponse>

    @PUT("v1/devices/{device_id}/")
    suspend fun putUpdateDevice(
        @Path("device_id") deviceId: String,
        @Body deviceUpdateRequest: DeviceUpdateRequest,
        @Header(ALGORAND_NETWORK_KEY) networkSlug: String?
    ): Response<DeviceRegistrationResponse>

    @HTTP(method = "DELETE", path = "v1/devices/", hasBody = true)
    suspend fun deletePushToken(
        @Header(ALGORAND_NETWORK_KEY) networkSlug: String,
        @Body pushTokenDeleteRequest: PushTokenDeleteRequest
    ): Response<Unit>

    @POST("v1/transactions/")
    suspend fun trackTransaction(@Body trackTransactionRequest: TrackTransactionRequest): Response<Unit>

    @POST("v1/asset-requests/")
    suspend fun postAssetSupportRequest(@Body assetSupportRequest: AssetSupportRequest): Response<Unit>

    @GET("v1/verified-assets/?limit=all")
    suspend fun getVerifiedAssets(): Response<Pagination<VerifiedAssetDetail>>

    @GET("v2/devices/{device_id}/notifications/")
    suspend fun getNotifications(
        @Path("device_id") deviceId: String
    ): Response<Pagination<NotificationItem>>

    @GET("v1/devices/{device_id}/notification-status/")
    suspend fun getNotificationStatus(
        @Path("device_id") deviceId: String
    ): Response<NotificationStatusResponse>

    @PUT("v1/devices/{device_id}/update-last-seen-notification/")
    suspend fun putLastSeenNotification(
        @Path("device_id") deviceId: String,
        @Body lastSeenRequest: LastSeenNotificationRequest,
        @Header(ALGORAND_NETWORK_KEY) networkSlug: String?
    ): Response<LastSeenNotificationResponse>

    @GET
    suspend fun getNotificationsMore(@Url url: String): Response<Pagination<NotificationItem>>

    @GET("v1/assets/search/")
    suspend fun getAssets(
        @Query("paginator") paginator: String? = "cursor",
        @Query("q") assetQuery: String?,
        @Query("offset") offset: Long = 0,
        @Query("limit") limit: Int = SEARCH_RESULT_LIMIT,
        @Query("has_collectible") hasCollectible: Boolean? = null,
        @Query("available_on_discover_mobile") availableOnDiscoverMobile: Boolean? = null
    ): Response<Pagination<AssetSearchResponse>>

    @GET("v1/assets/")
    suspend fun getAssetsByIds(
        @Query("asset_ids", encoded = true) assetIdsList: String,
        @Query("include_deleted") includeDeleted: Boolean? = null
    ): Response<Pagination<AssetDetailResponse>>

    @GET("v1/assets/{asset_id}/")
    suspend fun getAssetDetail(
        @Path("asset_id") nftAssetId: Long
    ): Response<AssetDetailResponse>

    @GET
    suspend fun getAssetsMore(@Url url: String): Response<Pagination<AssetSearchResponse>>

    @GET("v1/currencies/")
    suspend fun getCurrencies(): Response<List<CurrencyOptionResponse>>

    @GET("v1/currencies/{currency_id}/")
    suspend fun getCurrencyDetail(
        @Path("currency_id") currencyId: String
    ): Response<CurrencyDetailResponse>

    @PATCH("v1/devices/{device_id}/accounts/{address}/")
    suspend fun putNotificationFilter(
        @Path("device_id") deviceId: String,
        @Path("address") address: String,
        @Body notificationFilterRequest: NotificationFilterRequest
    ): Response<Unit>

    @GET("v1/devices/{device_id}/banners/")
    suspend fun getDeviceBanners(
        @Path("device_id") deviceId: String
    ): Response<BannerListResponse>

    @GET("v1/name-services/search/")
    suspend fun getNftDomainAccountAddresses(
        @Query("name") name: String
    ): Response<NftDomainSearchResponse>

    @PUT("v1/backups/{id}/")
    suspend fun putBackup(
        @Path("id") id: String,
        @Body body: WebBackupRequestBody,
        @Header("X-Modification-Key") modificationKey: String
    ): Response<ExportBackupResponse>

    @Streaming
    @GET("v1/accounts/{address}/export-history/")
    suspend fun getExportHistory(
        @Path("address") address: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): Response<ResponseBody>

    @GET("v1/dex-swap/available-assets/")
    suspend fun getAvailableSwapAssetList(
        @Query("asset_in_id") assetId: Long,
        @Query("providers") providersAsCsv: String,
        @Query("q") query: String?
    ): Response<AvailableSwapAssetListResponse>

    @POST("v1/dex-swap/quotes/")
    suspend fun getSwapQuote(
        @Body requestBody: SwapQuoteRequestBody
    ): Response<SwapQuoteResultResponse>

    @POST("v1/dex-swap/calculate-pera-fee/")
    suspend fun getPeraFee(
        @Body requestBody: PeraFeeRequestBody
    ): Response<PeraFeeResponse>

    @POST("v1/dex-swap/prepare-transactions/")
    suspend fun getQuoteTransactions(
        @Body requestBody: CreateSwapQuoteTransactionsRequestBody
    ): Response<CreateSwapQuoteTransactionsResponse>

    @POST("v1/devices/wallet-connect-subscriptions/")
    suspend fun subscribeWalletConnectSession(
        @Body body: WalletConnectSessionSubscriptionBody
    ): Response<Void>

    @POST("v1/algorand-indexer/should-refresh/")
    suspend fun shouldRefresh(
        @Body shouldRefreshAccountInformationRequestBody: ShouldRefreshRequestBody
    ): Response<ShouldRefreshResponse>

    @GET("v1/discover/assets/trending/")
    suspend fun getTrendingAssets(): Response<List<AssetSearchResponse>>
}
