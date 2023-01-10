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

package network.voi.hera.modules.tracking.accountdetail.accounthistory

import network.voi.hera.modules.tracking.core.BaseEventTracker
import network.voi.hera.modules.tracking.core.PeraEventTracker
import javax.inject.Inject

class AccountHistoryFilterEventTracker @Inject constructor(
    peraEventTracker: PeraEventTracker
) : BaseEventTracker(peraEventTracker) {

    suspend fun logAccountHistoryFilterEvent() {
        logEvent(ACCOUNT_HISTORY_FILTER_EVENT_KEY)
    }

    companion object {
        private const val ACCOUNT_HISTORY_FILTER_EVENT_KEY = "historyscr_transactions_filter"
    }
}
