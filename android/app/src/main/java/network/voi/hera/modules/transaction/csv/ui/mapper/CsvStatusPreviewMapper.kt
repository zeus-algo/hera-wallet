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

package network.voi.hera.modules.transaction.csv.ui.mapper

import network.voi.hera.decider.CsvErrorResIdDecider
import network.voi.hera.modules.transaction.csv.ui.model.CsvStatusPreview
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.Event
import java.io.File
import javax.inject.Inject

class CsvStatusPreviewMapper @Inject constructor(
    private val csvErrorResIdDecider: CsvErrorResIdDecider
) {
    fun mapToCsvStatus(dataResource: DataResource<File>): CsvStatusPreview {
        return CsvStatusPreview(
            isCsvProgressBarVisible = (dataResource is DataResource.Loading),
            csvFile = (dataResource as? DataResource.Success)?.let { Event(it.data) },
            isErrorShown = (dataResource is DataResource.Error),
            errorResource = csvErrorResIdDecider.decideCsvErrorResId(dataResource)
        )
    }
}
