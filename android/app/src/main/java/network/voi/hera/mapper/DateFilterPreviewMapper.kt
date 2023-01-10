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
 *
 */

package network.voi.hera.mapper

import network.voi.hera.decider.DateFilterImageResourceDecider
import network.voi.hera.decider.DateFilterTitleDecider
import network.voi.hera.decider.DateFilterTitleResIdDecider
import network.voi.hera.models.DateFilter
import network.voi.hera.models.ui.DateFilterPreview
import javax.inject.Inject

class DateFilterPreviewMapper @Inject constructor(
    private val dateFilterImageResourceDecider: DateFilterImageResourceDecider,
    private val dateFilterTitleDecider: DateFilterTitleDecider,
    private val dateFilterTitleResIdDecider: DateFilterTitleResIdDecider
) {

    fun mapToDateFilterPreview(dateFilter: DateFilter): DateFilterPreview {
        return DateFilterPreview(
            filterButtonIconResId = dateFilterImageResourceDecider.decideDateFilterImageRes(dateFilter),
            title = dateFilterTitleDecider.decideDateFilterTitle(dateFilter),
            titleResId = dateFilterTitleResIdDecider.decideDateFilterTitleResId(dateFilter),
            useFilterIconsOwnTint = dateFilter != DateFilter.AllTime
        )
    }
}
