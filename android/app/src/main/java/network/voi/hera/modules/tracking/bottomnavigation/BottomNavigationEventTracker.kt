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

package network.voi.hera.modules.tracking.bottomnavigation

import javax.inject.Inject

class BottomNavigationEventTracker @Inject constructor(
    private val bottomNavigationAlgoPriceTapEventTracker: BottomNavigationAlgoPriceTapEventTracker,
    private val bottomNavigationAccountsTapEventTracker: BottomNavigationAccountsTapEventTracker,
    private val bottomNavigationAlgoBuyTapEventTracker: BottomNavigationAlgoBuyTapEventTracker
) {

    suspend fun logAccountsTapEvent() {
        bottomNavigationAccountsTapEventTracker.logAccountTapEvent()
    }

    suspend fun logAlgoPriceTapEvent() {
        bottomNavigationAlgoPriceTapEventTracker.logAlgoPriceTapEvent()
    }

    suspend fun logBottomNavigationAlgoBuyTapEvent() {
        bottomNavigationAlgoBuyTapEventTracker.logBottomNavigationAlgoBuyTapEvent()
    }
}
