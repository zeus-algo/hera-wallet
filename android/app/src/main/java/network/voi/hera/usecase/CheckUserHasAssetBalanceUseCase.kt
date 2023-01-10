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

package network.voi.hera.usecase

import network.voi.hera.models.BaseAccountAssetData
import network.voi.hera.utils.isGreaterThan
import java.math.BigInteger
import javax.inject.Inject

class CheckUserHasAssetBalanceUseCase @Inject constructor(
    private val accountAssetDataUseCase: AccountAssetDataUseCase
) {

    fun hasUserAssetBalance(accountAddress: String, assetId: Long): Boolean {
        return accountAssetDataUseCase.getAccountOwnedAssetData(accountAddress, includeAlgo = true).any {
            it.id == assetId && it.amount isGreaterThan BigInteger.ZERO
        }
    }

    fun hasUserAssetBalance(ownedAssetData: BaseAccountAssetData.BaseOwnedAssetData.OwnedAssetData): Boolean {
        return ownedAssetData.amount isGreaterThan BigInteger.ZERO
    }
}
