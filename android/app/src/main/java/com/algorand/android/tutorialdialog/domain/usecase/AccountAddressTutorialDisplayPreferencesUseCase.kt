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

package com.algorand.android.tutorialdialog.domain.usecase

import com.algorand.android.models.Account
import com.algorand.android.modules.appopencount.domain.usecase.ApplicationOpenCountPreferenceUseCase
import com.algorand.android.tutorialdialog.domain.repository.TutorialDisplayPreferencesRepository
import com.algorand.android.usecase.GetLocalAccountsUseCase
import javax.inject.Inject
import javax.inject.Named

class AccountAddressTutorialDisplayPreferencesUseCase @Inject constructor(
    @Named(TutorialDisplayPreferencesRepository.REPOSITORY_INJECTION_NAME)
    private val tutorialDisplayPreferencesRepository: TutorialDisplayPreferencesRepository,
    private val applicationOpenCountPreferenceUseCase: ApplicationOpenCountPreferenceUseCase,
    private val getLocalAccountsUseCase: GetLocalAccountsUseCase
) {

    fun setTutorialPreferences(isShown: Boolean) {
        tutorialDisplayPreferencesRepository.setTutorialPreferences(isShown)
    }

    private fun isTutorialShowed(): Boolean {
        return tutorialDisplayPreferencesRepository.isTutorialShowed()
    }

    fun shouldShowTutorialDialog(): Boolean {
        return isThereAnyNormalLocalAccount() && !isTutorialShowed()
    }

    private suspend fun increaseAndSetApplicationOpenCount(count: Int) {
        applicationOpenCountPreferenceUseCase.setApplicationOpenCount(count + 1)
    }

    private suspend fun getApplicationOpenCount(): Int {
        return applicationOpenCountPreferenceUseCase.getApplicationOpenCount()
    }

    suspend fun shouldShowDialogByApplicationOpeningCount(): Boolean {
        val isThereAnyNormalLocalAccount = isThereAnyNormalLocalAccount()
        if (!isThereAnyNormalLocalAccount) return false

        val applicationOpenCount = getApplicationOpenCount()
        increaseAndSetApplicationOpenCount(applicationOpenCount)

        return isThereAnyNormalLocalAccount &&
            applicationOpenCount < APPLICATION_OPEN_COUNT_LIMIT_FOR_TUTORIAL_DIALOG &&
            isTutorialShowed()
    }

    private fun isThereAnyNormalLocalAccount(): Boolean {
        val localAccounts = getLocalAccountsUseCase.getLocalAccountsFromAccountManagerCache()
        return localAccounts.any { account -> account.type != Account.Type.WATCH }
    }

    companion object {
        private const val APPLICATION_OPEN_COUNT_LIMIT_FOR_TUTORIAL_DIALOG = 1
    }
}
