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

package network.voi.hera.ui.register.recover

import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.modules.qrscanning.BaseQrScannerFragment
import network.voi.hera.utils.setNavigationResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecoverWithPassphraseQrScannerFragment : BaseQrScannerFragment(R.id.recoverWithPassphraseQrScannerFragment) {

    private val recoverWithPassphraseQrScannerViewModel: RecoverWithPassphraseQrScannerViewModel by viewModels()

    override fun onImportAccountDeepLink(mnemonic: String): Boolean {
        if (recoverWithPassphraseQrScannerViewModel.isAccountLimitExceed()) {
            showMaxAccountLimitExceededError()
        } else {
            setNavigationResult(MNEMONIC_QR_SCAN_RESULT_KEY, mnemonic)
        }
        return true.also { navBack() }
    }

    companion object {
        const val MNEMONIC_QR_SCAN_RESULT_KEY = "mnemonic_qr_scan_result"
    }
}
