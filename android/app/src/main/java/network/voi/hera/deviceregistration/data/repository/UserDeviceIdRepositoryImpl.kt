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

package network.voi.hera.deviceregistration.data.repository

import network.voi.hera.deviceregistration.data.localsource.MainnetDeviceIdLocalSource
import network.voi.hera.deviceregistration.data.localsource.NotificationUserIdLocalSource
import network.voi.hera.deviceregistration.data.localsource.TestnetDeviceIdLocalSource
import network.voi.hera.deviceregistration.data.mapper.DeviceRegistrationRequestMapper
import network.voi.hera.deviceregistration.data.mapper.DeviceUpdateRequestMapper
import network.voi.hera.deviceregistration.domain.model.DeviceRegistrationDTO
import network.voi.hera.deviceregistration.domain.model.DeviceUpdateDTO
import network.voi.hera.deviceregistration.domain.repository.UserDeviceIdRepository
import network.voi.hera.models.Result
import network.voi.hera.network.MobileAlgorandApi
import network.voi.hera.network.request
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserDeviceIdRepositoryImpl @Inject constructor(
    private val mainnetDeviceIdLocalSource: MainnetDeviceIdLocalSource,
    private val testnetDeviceIdLocalSource: TestnetDeviceIdLocalSource,
    private val notificationUserIdLocalSource: NotificationUserIdLocalSource,
    private val mobileAlgorandApi: MobileAlgorandApi,
    private val deviceRegistrationRequestMapper: DeviceRegistrationRequestMapper,
    private val deviceUpdateRequestMapper: DeviceUpdateRequestMapper
) : UserDeviceIdRepository {

    override fun setMainnetDeviceId(deviceId: String?) {
        mainnetDeviceIdLocalSource.saveData(deviceId)
    }

    override fun getMainnetDeviceId(): String? {
        return mainnetDeviceIdLocalSource.getDataOrNull()
    }

    override fun setTestnetDeviceId(deviceId: String?) {
        testnetDeviceIdLocalSource.saveData(deviceId)
    }

    override fun getTestnetDeviceId(): String? {
        return testnetDeviceIdLocalSource.getDataOrNull()
    }

    override fun setNotificationUserId(deviceId: String?) {
        notificationUserIdLocalSource.saveData(deviceId)
    }

    override fun getNotificationUserId(): String? {
        return notificationUserIdLocalSource.getDataOrNull()
    }

    override suspend fun registerDeviceId(deviceRegistrationDTO: DeviceRegistrationDTO): Flow<Result<String>> = flow {
        val deviceRegistrationRequest = deviceRegistrationRequestMapper
            .mapToDeviceRegistrationRequest(deviceRegistrationDTO)
        val result = request { mobileAlgorandApi.postRegisterDevice(deviceRegistrationRequest) }.map {
            it.userId.orEmpty()
        }
        emit(result)
    }

    override suspend fun updateDeviceId(deviceUpdateDTO: DeviceUpdateDTO): Flow<Result<String>> = flow {
        val deviceUpdateRequest = deviceUpdateRequestMapper.mapToDeviceUpdateRequest(deviceUpdateDTO)
        val result = request {
            with(deviceUpdateDTO) { mobileAlgorandApi.putUpdateDevice(deviceId, deviceUpdateRequest, networkSlug) }
        }.map {
            it.userId.orEmpty()
        }
        emit(result)
    }
}
