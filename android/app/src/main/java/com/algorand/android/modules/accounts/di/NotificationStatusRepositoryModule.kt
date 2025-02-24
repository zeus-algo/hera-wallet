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

package com.algorand.android.modules.accounts.di

import com.algorand.android.modules.accounts.data.cache.LastSeenNotificationIdLocalSource
import com.algorand.android.modules.accounts.data.mapper.LastSeenNotificationDTOMapper
import com.algorand.android.modules.accounts.data.mapper.LastSeenNotificationRequestMapper
import com.algorand.android.modules.accounts.data.mapper.NotificationStatusDTOMapper
import com.algorand.android.modules.accounts.data.repository.NotificationStatusRepositoryImpl
import com.algorand.android.modules.accounts.domain.repository.NotificationStatusRepository
import com.algorand.android.network.MobileAlgorandApi
import com.hipo.hipoexceptionsandroid.RetrofitErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationStatusRepositoryModule {

    @Provides
    @Singleton
    @Named(NotificationStatusRepository.REPOSITORY_INJECTION_NAME)
    internal fun provideNotificationStatusRepository(
        mobileAlgorandApi: MobileAlgorandApi,
        hipoErrorHandler: RetrofitErrorHandler,
        lastSeenNotificationRequestMapper: LastSeenNotificationRequestMapper,
        notificationStatusDTOMapper: NotificationStatusDTOMapper,
        lastSeenNotificationDTOMapper: LastSeenNotificationDTOMapper,
        lastSeenNotificationIdLocalSource: LastSeenNotificationIdLocalSource
    ): NotificationStatusRepository {
        return NotificationStatusRepositoryImpl(
            mobileAlgorandApi = mobileAlgorandApi,
            hipoApiErrorHandler = hipoErrorHandler,
            lastSeenNotificationRequestMapper = lastSeenNotificationRequestMapper,
            notificationStatusDTOMapper = notificationStatusDTOMapper,
            lastSeenNotificationDTOMapper = lastSeenNotificationDTOMapper,
            lastSeenNotificationIdLocalSource = lastSeenNotificationIdLocalSource
        )
    }
}
