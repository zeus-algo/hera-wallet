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

import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.AccountManager
import network.voi.hera.core.BaseViewModel
import network.voi.hera.models.Account
import network.voi.hera.utils.PassphraseKeywordUtils
import network.voi.hera.utils.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class RecoverWithPassphraseViewModel @Inject constructor(
    private val accountManager: AccountManager,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val passphraseKeywordUtils = PassphraseKeywordUtils()

    var mnemonic: String = savedStateHandle.getOrElse(MNEMONIC_KEY, "")
    val newUpdateFlow = MutableSharedFlow<Pair<Int, String>>()
    val validationFlow = MutableSharedFlow<Pair<Int, Boolean>>()
    val suggestionWordsFlow = MutableStateFlow<Pair<Int, List<String>>>(Pair(0, listOf()))

    init {
        viewModelScope.launch(Dispatchers.Default) {
            newUpdateFlow.collect { (index, word) ->
                onNewUpdate(index, word)
            }
        }
    }

    private suspend fun onNewUpdate(index: Int, word: String) {
        checkValidation(index, word)
        handleKeywordSuggestor(index, word)
    }

    private fun handleKeywordSuggestor(index: Int, word: String) {
        val suggestedWords = passphraseKeywordUtils.getSuggestedWords(SUGGESTED_WORD_COUNT, word)
        suggestionWordsFlow.value = Pair(index, suggestedWords)
    }

    private suspend fun checkValidation(index: Int, word: String) {
        validationFlow.emit(Pair(index, passphraseKeywordUtils.isWordInKeywords(word)))
    }

    fun getAccountIfExist(publicKey: String): Account? {
        return accountManager.getAccounts().find { account -> account.address == publicKey }
    }

    companion object {
        private const val SUGGESTED_WORD_COUNT = 3
        private const val MNEMONIC_KEY = "mnemonic"
    }
}
