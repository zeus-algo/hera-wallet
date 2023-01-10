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

package network.voi.hera.ui.settings.selection.languageselection

import javax.inject.Inject
import network.voi.hera.core.BaseViewModel
import network.voi.hera.utils.analytics.logLanguageChange
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : BaseViewModel() {

    fun logLanguageChange(newLanguageId: String) {
        firebaseAnalytics.logLanguageChange(newLanguageId)
    }
}
