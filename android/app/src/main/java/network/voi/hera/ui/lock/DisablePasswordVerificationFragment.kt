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

package network.voi.hera.ui.lock

import network.voi.hera.R
import network.voi.hera.ui.password.BasePasswordFragment
import network.voi.hera.ui.password.model.PasswordScreenType
import network.voi.hera.ui.password.model.PasswordScreenType.VerificationScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisablePasswordVerificationFragment : BasePasswordFragment() {

    override val titleResId: Int = R.string.enter_your_six_digit_pin

    override val screenType: PasswordScreenType = VerificationScreenType(
        navigationResultKey = DISABLE_PASSWORD_VERIFICATION_RESULT_KEY
    )

    companion object {
        const val DISABLE_PASSWORD_VERIFICATION_RESULT_KEY = "disable_password_verification_result"
    }
}
