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

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.room.Room
import network.voi.hera.core.AccountManager
import network.voi.hera.database.AlgorandDatabase
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_10_11
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_3_4
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_4_5
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_5_6
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_6_7
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_7_8
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_8_9
import network.voi.hera.database.AlgorandDatabase.Companion.MIGRATION_9_10
import network.voi.hera.database.ContactDao
import network.voi.hera.database.NodeDao
import network.voi.hera.database.NotificationFilterDao
import network.voi.hera.database.WalletConnectDao
import network.voi.hera.database.WalletConnectTypeConverters
import network.voi.hera.ledger.LedgerBleConnectionManager
import network.voi.hera.ledger.LedgerBleSearchManager
import network.voi.hera.notification.PeraNotificationManager
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.GetLocalAccountsFromSharedPrefUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.ALGORAND_KEYSTORE_URI
import network.voi.hera.utils.AccountCacheManager
import network.voi.hera.utils.AutoLockManager
import network.voi.hera.utils.ENCRYPTED_SHARED_PREF_NAME
import network.voi.hera.utils.KEYSET_HANDLE
import network.voi.hera.utils.KEY_TEMPLATE_AES256_GCM
import network.voi.hera.utils.preference.SETTINGS
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext appContext: Context,
        walletConnectTypeConverters: WalletConnectTypeConverters
    ): AlgorandDatabase {
        return Room
            .databaseBuilder(appContext, AlgorandDatabase::class.java, AlgorandDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .addMigrations(
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11
            )
            .addTypeConverter(walletConnectTypeConverters)
            .build()
    }

    @Singleton
    @Provides
    fun getEncryptionAead(@ApplicationContext appContext: Context): Aead {
        AeadConfig.register()

        val algorandKeysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(appContext, KEYSET_HANDLE, ENCRYPTED_SHARED_PREF_NAME)
            .withKeyTemplate(KeyTemplates.get(KEY_TEMPLATE_AES256_GCM))
            .withMasterKeyUri(ALGORAND_KEYSTORE_URI)
            .build()
            .keysetHandle

        return algorandKeysetHandle.getPrimitive(Aead::class.java)
    }

    @Singleton
    @Provides
    fun provideSettingsSharedPref(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideNodeDao(database: AlgorandDatabase): NodeDao {
        return database.nodeDao()
    }

    @Singleton
    @Provides
    fun provideNotificationFilterDao(database: AlgorandDatabase): NotificationFilterDao {
        return database.notificationFilterDao()
    }

    @Singleton
    @Provides
    fun provideContactDao(database: AlgorandDatabase): ContactDao {
        return database.contactDao()
    }

    @Singleton
    @Provides
    fun provideWalletConnectDao(database: AlgorandDatabase): WalletConnectDao {
        return database.walletConnect()
    }

    @Singleton
    @Provides
    fun provideAlgorandNotificationManager(): PeraNotificationManager {
        return PeraNotificationManager()
    }

    @Singleton
    @Provides
    fun provideAccountCacheManager(
        accountManager: AccountManager,
        accountDetailUseCase: AccountDetailUseCase,
        assetDetailUseCase: SimpleAssetDetailUseCase
    ): AccountCacheManager {
        return AccountCacheManager(accountManager, accountDetailUseCase, assetDetailUseCase)
    }

    @Singleton
    @Provides
    fun provideAccountManager(
        aead: Aead,
        gson: Gson,
        sharedPref: SharedPreferences,
        getLocalAccountsFromSharedPrefUseCase: GetLocalAccountsFromSharedPrefUseCase
    ): AccountManager {
        return AccountManager(aead, gson, sharedPref, getLocalAccountsFromSharedPrefUseCase)
    }

    @Singleton
    @Provides
    fun provideLedgerBleConnectionManager(@ApplicationContext appContext: Context): LedgerBleConnectionManager {
        return LedgerBleConnectionManager(appContext)
    }

    @Singleton
    @Provides
    fun provideLedgerBleSearchManager(
        @ApplicationContext appContext: Context,
        bluetoothManager: BluetoothManager?,
        ledgerBleConnectionManager: LedgerBleConnectionManager
    ): LedgerBleSearchManager {
        return LedgerBleSearchManager(appContext, bluetoothManager, ledgerBleConnectionManager)
    }

    @Singleton
    @Provides
    fun provideBluetoothManager(@ApplicationContext appContext: Context): BluetoothManager? {
        return ContextCompat.getSystemService<BluetoothManager>(appContext, BluetoothManager::class.java)
    }

    @Singleton
    @Provides
    fun provideAutoLockManager(): AutoLockManager {
        return AutoLockManager()
    }

    // TODO Move this into tracking di module
    @Singleton
    @Provides
    fun provideFirebaseAnalytics(@ApplicationContext appContext: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(appContext)
    }
}
