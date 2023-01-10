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

package network.voi.hera.modules.collectibles.listingviewtype.domain.usecase

import network.voi.hera.modules.collectibles.listingviewtype.domain.repository.NFTListingViewTypeRepository
import network.voi.hera.sharedpref.SharedPrefLocalSource
import javax.inject.Inject
import javax.inject.Named

class RemoveOnListingViewTypeChangeListenerUseCase @Inject constructor(
    @Named(NFTListingViewTypeRepository.INJECTION_NAME)
    private val nftListingViewTypeRepository: NFTListingViewTypeRepository
) {

    operator fun invoke(listener: SharedPrefLocalSource.OnChangeListener<Int>) {
        nftListingViewTypeRepository.removeOnListingViewTypeChangeListener(listener)
    }
}
