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

package network.voi.hera.modules.swap.utils

import network.voi.hera.models.Account
import network.voi.hera.modules.swap.introduction.domain.usecase.IsSwapFeatureIntroductionPageShownUseCase
import network.voi.hera.modules.swap.reddot.domain.usecase.SetSwapFeatureRedDotVisibilityUseCase
import network.voi.hera.usecase.GetLocalAccountsUseCase
import javax.inject.Inject

class SwapNavigationDestinationHelper @Inject constructor(
    private val accountsUseCase: GetLocalAccountsUseCase,
    isSwapFeatureIntroductionPageShownUseCase: IsSwapFeatureIntroductionPageShownUseCase,
    setSwapFeatureRedDotVisibilityUseCase: SetSwapFeatureRedDotVisibilityUseCase
) : BaseSwapNavigationDestinationHelper(
    isSwapFeatureIntroductionPageShownUseCase,
    setSwapFeatureRedDotVisibilityUseCase
) {

    suspend fun getSwapNavigationDestination(
        accountAddress: String? = null,
        onNavToIntroduction: () -> Unit,
        onNavToSwap: (accountAddress: String) -> Unit,
        onNavToAccountSelection: (() -> Unit)? = null
    ) {
        handleNavigationDestination(
            navToIntroduction = { onNavToIntroduction() },
            handleDestinationWithAccount = {
                handleDestinationWithAccount(accountAddress, onNavToSwap, onNavToAccountSelection)
            }
        )
    }

    private fun handleDestinationWithAccount(
        accountAddress: String?,
        onNavToSwap: (accountAddress: String) -> Unit,
        onNavToAccountSelection: (() -> Unit)?
    ) {
        if (accountAddress != null) {
            onNavToSwap(accountAddress)
        } else {
            val authorizedAccounts = getAccountsThatCanSignTransaction()
            if (authorizedAccounts.size == 1) {
                onNavToSwap(authorizedAccounts.first().address)
            } else {
                onNavToAccountSelection?.invoke()
            }
        }
    }

    private fun getAccountsThatCanSignTransaction(): List<Account> {
        return accountsUseCase.getLocalAccountsFromAccountManagerCache().filter {
            it.canSignTransaction()
        }
    }
}
