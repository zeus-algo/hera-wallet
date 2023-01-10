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

package network.voi.hera.dependencyinjection

import android.content.Context
import network.voi.hera.R
import network.voi.hera.models.BaseWalletConnectErrorProvider
import network.voi.hera.network.AlgodInterceptor
import network.voi.hera.utils.walletconnect.WCWalletConnectClient
import network.voi.hera.utils.walletconnect.WCWalletConnectMapper
import network.voi.hera.utils.walletconnect.WalletConnectClient
import network.voi.hera.utils.walletconnect.WalletConnectCustomTransactionHandler.Companion.MAX_TRANSACTION_COUNT
import network.voi.hera.utils.walletconnect.WalletConnectEventLogger
import network.voi.hera.utils.walletconnect.WalletConnectFirebaseEventLogger
import network.voi.hera.utils.walletconnect.WalletConnectSessionBuilder
import network.voi.hera.utils.walletconnect.WalletConnectSessionCachedDataHandler
import network.voi.hera.utils.walletconnect.WalletConnectTransactionErrorProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.walletconnect.impls.FileWCSessionStore
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WalletConnectModule {

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    @Named("wcWalletClient")
    fun provideWalletConnectClient(
        walletConnectSessionBuilder: WalletConnectSessionBuilder,
        walletConnectMapper: WCWalletConnectMapper,
        cachedDataHandler: WalletConnectSessionCachedDataHandler
    ): WalletConnectClient {
        return WCWalletConnectClient(
            sessionBuilder = walletConnectSessionBuilder,
            walletConnectMapper = walletConnectMapper,
            sessionCachedDataHandler = cachedDataHandler
        )
    }

    @Singleton
    @Provides
    fun provideWalletConnectSessionBuilder(
        @Named("walletConnectHttpClient") okHttpClient: OkHttpClient,
        @ApplicationContext appContext: Context,
        gson: Gson,
        moshi: Moshi,
        walletConnectMapper: WCWalletConnectMapper
    ): WalletConnectSessionBuilder {
        val storageFile = File(appContext.cacheDir, WCWalletConnectClient.CACHE_STORAGE_NAME).apply { createNewFile() }
        return WalletConnectSessionBuilder(
            gson,
            moshi,
            okHttpClient,
            FileWCSessionStore(storageFile, moshi),
            walletConnectMapper
        )
    }

    @Provides
    fun provideWalletConnectEventLogger(
        firebaseAnalytics: FirebaseAnalytics,
        algodInterceptor: AlgodInterceptor
    ): WalletConnectEventLogger {
        return WalletConnectFirebaseEventLogger(firebaseAnalytics, algodInterceptor)
    }

    @Singleton
    @Provides
    fun provideWalletConnectTransactionErrorProvider(
        @ApplicationContext appContext: Context
    ): WalletConnectTransactionErrorProvider {
        return with(appContext) {
            val rejectedErrorProvider = BaseWalletConnectErrorProvider.RequestRejectedErrorProvider(
                userRejectionErrorMessage = getString(R.string.transaction_request_rejected_user_rejected),
                failedGroupTransactionErrorMessage = getString(R.string.it_looks_like),
                pendingTransactionErrorMessage = getString(R.string.transaction_request_rejected_user_currently)
            )

            val unauthorizedRequestErrorProvider = BaseWalletConnectErrorProvider.UnauthorizedRequestErrorProvider(
                mismatchingNodesErrorMessage = getString(R.string.signing_error_network_mismatch),
                missingSignerErrorMessage = getString(R.string.signing_error_transaction_in_request)
            )

            val unsupportedErrorProvider = BaseWalletConnectErrorProvider.UnsupportedRequestErrorProvider(
                unknownTransactionTypeErrorMessage = getString(R.string.transaction_request_contains_unsupported),
                multisigTransactionErrorMessage = getString(R.string.transaction_request_contains_unsupported_multisig)
            )

            val invalidInputErrorProvider = BaseWalletConnectErrorProvider.InvalidInputErrorProvider(
                maxTransactionLimitErrorMessage = getString(
                    R.string.invalid_input_transaction_request,
                    MAX_TRANSACTION_COUNT
                ),
                unableToParseErrorMessage = getString(R.string.invalid_input_unable_to_parse),
                invalidPublicKeyErrorMessage = getString(R.string.invalid_input_invalid_public_key),
                invalidAssetErrorMessage = getString(R.string.invalid_input_invalid_asset),
                unableToSignErrorMessage = getString(R.string.invalid_input_unable_to_be),
                atomicTxnNoNeedToBeSignedErrorMessage = getString(R.string.invalid_input_group_transaction),
                invalidSignerErrorMessage = getString(R.string.invalid_input_requested_signer),
            )

            WalletConnectTransactionErrorProvider(
                rejected = rejectedErrorProvider,
                unauthorized = unauthorizedRequestErrorProvider,
                unsupported = unsupportedErrorProvider,
                invalidInput = invalidInputErrorProvider
            )
        }
    }
}
