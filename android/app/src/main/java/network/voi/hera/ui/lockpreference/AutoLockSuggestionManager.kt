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

package network.voi.hera.ui.lockpreference

import android.content.SharedPreferences
import network.voi.hera.MainActivity
import network.voi.hera.MainNavigationDirections.Companion.actionToLockPreferenceNavigation
import network.voi.hera.core.AccountManager
import network.voi.hera.utils.preference.DEFAULT_LOCK_PREFERENCE_COUNT
import network.voi.hera.utils.preference.DONT_SHOW_AGAIN_COUNT
import network.voi.hera.utils.preference.getLockPreferenceCount
import network.voi.hera.utils.preference.isPasswordChosen
import network.voi.hera.utils.preference.setLockPreferenceCount
import javax.inject.Inject

class AutoLockSuggestionManager @Inject constructor(
    private val sharedPref: SharedPreferences,
    private val accountManager: AccountManager
) {

    private var isStarted = false
    private var lockPreferenceCount = DEFAULT_LOCK_PREFERENCE_COUNT

    init {
        initializeStartCount()
    }

    private fun initializeStartCount() {
        if (sharedPref.isPasswordChosen().not() && accountManager.isThereAnyRegisteredAccount()) {
            lockPreferenceCount = sharedPref.getLockPreferenceCount()
            if (lockPreferenceCount != DONT_SHOW_AGAIN_COUNT && lockPreferenceCount < SUGGESTION_TRIGGER_COUNT) {
                lockPreferenceCount++
                sharedPref.setLockPreferenceCount(lockPreferenceCount)
            }
        }
    }

    private fun isTriggerNeeded(): Boolean {
        return (lockPreferenceCount >= SUGGESTION_TRIGGER_COUNT) && sharedPref.isPasswordChosen().not()
    }

    fun start(mainActivity: MainActivity) {
        if (isStarted) {
            return
        }

        isStarted = true

        if (isTriggerNeeded()) {
            mainActivity.nav(actionToLockPreferenceNavigation())
            sharedPref.setLockPreferenceCount(DEFAULT_LOCK_PREFERENCE_COUNT)
        }
    }

    companion object {
        private const val SUGGESTION_TRIGGER_COUNT = 5
    }
}
