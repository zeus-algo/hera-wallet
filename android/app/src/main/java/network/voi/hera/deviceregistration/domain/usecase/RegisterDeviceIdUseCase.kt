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

package network.voi.hera.deviceregistration.domain.usecase

import network.voi.hera.core.AccountManager
import network.voi.hera.deviceregistration.domain.mapper.DeviceRegistrationDTOMapper
import network.voi.hera.deviceregistration.domain.model.DeviceRegistrationDTO
import network.voi.hera.deviceregistration.domain.repository.UserDeviceIdRepository
import network.voi.hera.models.Result
import network.voi.hera.utils.DataResource
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class RegisterDeviceIdUseCase @Inject constructor(
    @Named(UserDeviceIdRepository.USER_DEVICE_ID_REPOSITORY_INJECTION_NAME)
    private val userDeviceIdRepository: UserDeviceIdRepository,
    private val deviceRegisterDTOMapper: DeviceRegistrationDTOMapper,
    private val deviceIdUseCase: DeviceIdUseCase,
    accountManager: AccountManager
) : BaseDeviceIdOperationUseCase(accountManager) {

    fun registerDevice(token: String): Flow<DataResource<String>> = flow<DataResource<String>> {
        val deviceRegistrationDTO = getDeviceRegistrationDTO(token)
        userDeviceIdRepository.registerDeviceId(deviceRegistrationDTO).collect {
            when (it) {
                is Result.Success -> {
                    val deviceId = it.data
                    deviceIdUseCase.setSelectedNodeDeviceId(deviceId)
                    emit(DataResource.Success(deviceId))
                }
                is Result.Error -> {
                    delay(REGISTER_DEVICE_FAIL_DELAY)
                    registerDevice(token)
                }
            }
        }
    }

    private fun getDeviceRegistrationDTO(token: String): DeviceRegistrationDTO {
        return deviceRegisterDTOMapper.mapToDeviceRegistrationDTO(
            token = token,
            accountPublicKeyList = getAccountPublicKeys(),
            application = getApplicationName(),
            platform = PLATFORM_NAME,
            locale = getLocaleLanguageCode()
        )
    }
}
