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

package network.voi.hera.customviews.accountandassetitem.mapper

import network.voi.hera.models.Account
import network.voi.hera.models.AccountIconResource
import network.voi.hera.customviews.accountandassetitem.model.BaseItemConfiguration
import network.voi.hera.models.ButtonConfiguration
import network.voi.hera.utils.AccountDisplayName
import java.math.BigDecimal
import javax.inject.Inject

class AccountItemConfigurationMapper @Inject constructor() {

    @SuppressWarnings("LongParameterList")
    fun mapTo(
        accountAddress: String,
        accountName: String,
        accountIconResource: AccountIconResource? = null,
        accountType: Account.Type? = null,
        accountPrimaryValueText: String? = null,
        accountSecondaryValueText: String? = null,
        accountPrimaryValue: BigDecimal? = null,
        accountSecondaryValue: BigDecimal? = null,
        accountAssetCount: Int? = null,
        showWarningIcon: Boolean? = null,
        dragButtonConfiguration: ButtonConfiguration? = null
    ): BaseItemConfiguration.AccountItemConfiguration {
        return BaseItemConfiguration.AccountItemConfiguration(
            accountAddress = accountAddress,
            accountIconResource = accountIconResource,
            accountDisplayName = AccountDisplayName.create(accountAddress, accountName, accountType),
            primaryValueText = accountPrimaryValueText,
            secondaryValueText = accountSecondaryValueText,
            primaryValue = accountPrimaryValue,
            secondaryValue = accountSecondaryValue,
            showWarning = showWarningIcon,
            dragButtonConfiguration = dragButtonConfiguration,
            accountType = accountType,
            accountAssetCount = accountAssetCount
        )
    }
}
