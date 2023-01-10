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

package network.voi.hera.modules.verificationtier.ui.decider

import network.voi.hera.assetsearch.domain.model.VerificationTier
import network.voi.hera.assetsearch.domain.model.VerificationTier.SUSPICIOUS
import network.voi.hera.assetsearch.domain.model.VerificationTier.TRUSTED
import network.voi.hera.assetsearch.domain.model.VerificationTier.UNVERIFIED
import network.voi.hera.assetsearch.domain.model.VerificationTier.VERIFIED
import network.voi.hera.assetsearch.ui.model.VerificationTierConfiguration
import javax.inject.Inject

class VerificationTierConfigurationDecider @Inject constructor() {

    fun decideVerificationTierConfiguration(verificationTier: VerificationTier?): VerificationTierConfiguration {
        return when (verificationTier) {
            VERIFIED -> VerificationTierConfiguration.VERIFIED
            TRUSTED -> VerificationTierConfiguration.TRUSTED
            SUSPICIOUS -> VerificationTierConfiguration.SUSPICIOUS
            UNVERIFIED, null -> VerificationTierConfiguration.UNVERIFIED
        }
    }
}
