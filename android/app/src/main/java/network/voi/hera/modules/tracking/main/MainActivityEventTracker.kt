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

package network.voi.hera.modules.tracking.main

import network.voi.hera.modules.tracking.bottomnavigation.BottomNavigationEventTracker
import network.voi.hera.modules.tracking.moonpay.MoonpayEventTracker
import network.voi.hera.modules.tracking.swap.quickaction.QuickActionSwapButtonClickEventTracker
import javax.inject.Inject

class MainActivityEventTracker @Inject constructor(
    private val quickActionSwapButtonClickEventTracker: QuickActionSwapButtonClickEventTracker,
    private val bottomNavigationEventTracker: BottomNavigationEventTracker,
    private val moonpayEventTracker: MoonpayEventTracker
) {

    suspend fun logAlgoPriceTapEvent() {
        bottomNavigationEventTracker.logAlgoPriceTapEvent()
    }

    suspend fun logAccountsTapEvent() {
        bottomNavigationEventTracker.logAccountsTapEvent()
    }

    suspend fun logBottomNavigationAlgoBuyTapEvent() {
        bottomNavigationEventTracker.logBottomNavigationAlgoBuyTapEvent()
    }

    suspend fun logMoonpayAlgoBuyCompletedEvent() {
        moonpayEventTracker.logMoonpayAlgoBuyCompletedEvent()
    }

    suspend fun logQuickActionSwapButtonClickEvent() {
        quickActionSwapButtonClickEventTracker.logSwapButtonClickEvent()
    }
}
