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

import javax.inject.Inject

class AccountHistoryFragmentEventTracker @Inject constructor(
    private val accountHistoryFilterEventTracker: AccountHistoryFilterEventTracker,
    private val accountHistoryExportCsvEventTracker: AccountHistoryExportCsvEventTracker
) {

    suspend fun logAccountHistoryFilterEvent() {
        accountHistoryFilterEventTracker.logAccountHistoryFilterEvent()
    }

    suspend fun logAccountHistoryExportCsvEvent() {
        accountHistoryExportCsvEventTracker.logAccountHistoryExportCsvEvent()
    }
}
