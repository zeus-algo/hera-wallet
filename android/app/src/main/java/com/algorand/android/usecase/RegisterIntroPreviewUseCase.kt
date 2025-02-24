/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 */

package com.algorand.android.usecase

import com.algorand.android.core.AccountManager
import com.algorand.android.mapper.RegisterIntroPreviewMapper
import com.algorand.android.modules.tracking.onboarding.register.registerintro.RegisterIntroFragmentEventTracker
import javax.inject.Inject
import kotlinx.coroutines.flow.flow

class RegisterIntroPreviewUseCase @Inject constructor(
    private val accountManager: AccountManager,
    private val registerIntroPreviewMapper: RegisterIntroPreviewMapper,
    private val registerIntroFragmentEventTracker: RegisterIntroFragmentEventTracker
) {

    fun getRegisterIntroPreview(isShowingCloseButton: Boolean) = flow {
        val hasAccount = accountManager.accounts.value.isNotEmpty()
        val isSkipButtonVisible = !isShowingCloseButton
        val registerIntroPreview = registerIntroPreviewMapper.mapTo(
            isSkipButtonVisible = isSkipButtonVisible,
            isCloseButtonVisible = isShowingCloseButton,
            hasAccount = hasAccount
        )
        emit(registerIntroPreview)
    }

    suspend fun logOnboardingWelcomeAccountCreateClickEvent() {
        registerIntroFragmentEventTracker.logOnboardingWelcomeAccountCreateEvent()
    }

    suspend fun logOnboardingWelcomeAccountRecoverClickEvent() {
        registerIntroFragmentEventTracker.logOnboardingWelcomeAccountRecoverEvent()
    }

    suspend fun logOnboardingCreateAccountSkipClickEvent() {
        registerIntroFragmentEventTracker.logOnboardingWelcomeAccountSkipEvent()
    }
}
