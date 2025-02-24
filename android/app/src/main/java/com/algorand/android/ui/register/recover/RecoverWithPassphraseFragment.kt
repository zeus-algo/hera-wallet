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

package com.algorand.android.ui.register.recover

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.algorand.algosdk.mobile.Mobile
import com.algorand.android.MainNavigationDirections
import com.algorand.android.R
import com.algorand.android.core.DaggerBaseFragment
import com.algorand.android.customviews.PassphraseInput
import com.algorand.android.customviews.PassphraseInputGroup
import com.algorand.android.customviews.PassphraseInputGroup.Companion.WORD_COUNT
import com.algorand.android.customviews.PassphraseWordSuggestor
import com.algorand.android.databinding.FragmentRecoverWithPassphraseBinding
import com.algorand.android.models.Account
import com.algorand.android.models.AccountCreation
import com.algorand.android.models.AnnotatedString
import com.algorand.android.models.FragmentConfiguration
import com.algorand.android.models.IconButton
import com.algorand.android.models.ToolbarConfiguration
import com.algorand.android.ui.register.recover.RecoverOptionsBottomSheet.Companion.RESULT_KEY
import com.algorand.android.ui.register.recover.RecoverWithPassphraseQrScannerFragment.Companion.MNEMONIC_QR_SCAN_RESULT_KEY
import com.algorand.android.utils.KeyboardToggleListener
import com.algorand.android.utils.addKeyboardToggleListener
import com.algorand.android.utils.analytics.CreationType
import com.algorand.android.utils.extensions.collectLatestOnLifecycle
import com.algorand.android.utils.getTextFromClipboard
import com.algorand.android.utils.hideKeyboard
import com.algorand.android.utils.removeKeyboardToggleListener
import com.algorand.android.utils.splitMnemonic
import com.algorand.android.utils.startSavedStateListener
import com.algorand.android.utils.toShortenedAddress
import com.algorand.android.utils.useSavedStateValue
import com.algorand.android.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecoverWithPassphraseFragment : DaggerBaseFragment(R.layout.fragment_recover_with_passphrase) {

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    private val recoverWithPassphraseViewModel: RecoverWithPassphraseViewModel by viewModels()

    private val binding by viewBinding(FragmentRecoverWithPassphraseBinding::bind)

    private var keyboardToggleListener: KeyboardToggleListener? = null

    private var passphraseInput: PassphraseInput? = null

    private lateinit var accountCreation: AccountCreation

    private val recoverPassphraseTitleHeight: Int by lazy { binding.recoverPassphraseTitle.height }

    private val onKeyboardToggleAction: (shown: Boolean) -> Unit = { keyboardShown ->
        if (keyboardShown && passphraseInput != null) {
            scrollToPassphraseInput(passphraseInput!!)
        }
    }

    private val suggestionWordsCollector: suspend (Pair<Int, List<String>>) -> Unit = { (index, suggestedWords) ->
        binding.passphraseWordSuggestor.setSuggestedWords(index, suggestedWords)
    }

    private val validationCollector: suspend (Pair<Int, Boolean>) -> Unit = { (index, isValidated) ->
        binding.passphraseInputGroup.setValidation(index, isValidated)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        customizeToolbar()
        recoverMnemonicFromViewModel()
        binding.recoverButton.setOnClickListener { onRecoverClick() }
    }

    private fun initSavedStateListener() {
        startSavedStateListener(R.id.recoverWithPassphraseFragment) {
            useSavedStateValue<String>(MNEMONIC_QR_SCAN_RESULT_KEY) { mnemonic ->
                binding.passphraseInputGroup.setMnemonic(mnemonic)
            }
            useSavedStateValue<RecoverOptionsBottomSheet.OptionResult>(RESULT_KEY) { optionResult ->
                when (optionResult) {
                    RecoverOptionsBottomSheet.OptionResult.PASTE -> pasteClipboard()
                    RecoverOptionsBottomSheet.OptionResult.SCAN_QR -> navToScanQr()
                }
            }
        }
    }

    private fun navToScanQr() {
        recoverWithPassphraseViewModel.mnemonic = binding.passphraseInputGroup.getMnemonicResponse().mnemonic
        nav(
            RecoverWithPassphraseFragmentDirections
                .actionRecoverWithPassphraseFragmentToRecoverWithPassphraseQrScannerFragment()
        )
    }

    private fun recoverMnemonicFromViewModel() {
        val mnemonic = recoverWithPassphraseViewModel.mnemonic.takeIf { it.isNotEmpty() }
        binding.passphraseInputGroup.run {
            if (mnemonic != null) {
                doOnLayout { setMnemonic(mnemonic) }
            } else {
                focusTo(0, shouldShowKeyboard = true)
            }
        }
    }

    private fun initObservers() {
        initWordSuggestorListener()

        initInputGroupListener()

        viewLifecycleOwner.collectLatestOnLifecycle(
            recoverWithPassphraseViewModel.suggestionWordsFlow,
            suggestionWordsCollector
        )

        viewLifecycleOwner.collectLatestOnLifecycle(
            recoverWithPassphraseViewModel.validationFlow,
            validationCollector
        )
    }

    override fun onResume() {
        super.onResume()
        initSavedStateListener()
        keyboardToggleListener = addKeyboardToggleListener(binding.root, onKeyboardToggleAction)
    }

    override fun onPause() {
        super.onPause()
        view?.hideKeyboard()
        keyboardToggleListener?.removeKeyboardToggleListener(binding.root)
    }

    private fun customizeToolbar() {
        getAppToolbar()?.setEndButton(button = IconButton(R.drawable.ic_more, onClick = ::onOptionsClick))
    }

    private fun initWordSuggestorListener() {
        binding.passphraseWordSuggestor.listener = object : PassphraseWordSuggestor.Listener {
            override fun onSuggestedWordSelected(index: Int, word: String) {
                binding.passphraseInputGroup.setSuggestedWord(index, word)
            }
        }
    }

    private fun initInputGroupListener() {
        binding.passphraseInputGroup.listener = object : PassphraseInputGroup.Listener {
            override fun onInputFocus(passphraseInput: PassphraseInput) {
                if (keyboardToggleListener?.isKeyboardShown == true) {
                    scrollToPassphraseInput(passphraseInput)
                } else {
                    this@RecoverWithPassphraseFragment.passphraseInput = passphraseInput
                }
            }

            override fun onNewUpdate(index: Int, word: String) {
                lifecycleScope.launch {
                    recoverWithPassphraseViewModel.newUpdateFlow.emit(Pair(index, word))
                }
            }

            override fun onDoneClick(isAllValidationDone: Boolean) {
                if (isAllValidationDone) {
                    verifyMnemonic()
                }
                view?.hideKeyboard()
            }

            override fun onMnemonicReady(isReady: Boolean) {
                binding.recoverButton.isEnabled = isReady
            }

            override fun onError(errorResId: Int) {
                showGlobalError(getString(errorResId))
            }
        }
    }

    private fun scrollToPassphraseInput(passphraseInput: PassphraseInput) {
        binding.scrollView.smoothScrollTo(
            0,
            (passphraseInput.y - passphraseInput.height + recoverPassphraseTitleHeight).toInt()
        )
    }

    private fun verifyMnemonic() {
        try {
            val mnemonicResponse = binding.passphraseInputGroup.getMnemonicResponse()
            if (mnemonicResponse is PassphraseInputGroup.MnemonicResponse.Error) {
                showErrorBottomSheet(descriptionString = mnemonicResponse.error)
                return
            }
            val privateKey = Mobile.mnemonicToPrivateKey(mnemonicResponse.mnemonic.lowercase(Locale.ENGLISH))
            if (privateKey != null) {
                val publicKey = Mobile.generateAddressFromSK(privateKey)
                val sameAccount = recoverWithPassphraseViewModel.getAccountIfExist(publicKey)
                if (sameAccount != null &&
                    sameAccount.type != Account.Type.REKEYED &&
                    sameAccount.type != Account.Type.WATCH
                ) {
                    showGlobalError(getString(R.string.this_account_already_exists))
                    return
                }
                val recoveredAccount = Account.create(
                    publicKey,
                    Account.Detail.Standard(privateKey),
                    publicKey.toShortenedAddress()
                )
                accountCreation = AccountCreation(recoveredAccount, CreationType.RECOVER)
                navigateToSuccess()
            }
        } catch (exception: Exception) {
            showErrorBottomSheet(descriptionString = AnnotatedString(R.string.account_not_found_please_try))
        }
    }

    private fun pasteClipboard() {
        val pastedPassphrase = context?.getTextFromClipboard().toString()
        val keywords = pastedPassphrase.splitMnemonic()
        if (keywords.count() == WORD_COUNT) {
            binding.passphraseInputGroup.setMnemonic(pastedPassphrase)
        } else {
            showGlobalError(getString(R.string.the_last_copied_text))
        }
    }

    private fun navigateToSuccess() {
        nav(
            RecoverWithPassphraseFragmentDirections
                .actionRecoverWithPassphraseFragmentToRecoverAccountNameRegistrationFragment(accountCreation)
        )
    }

    private fun showErrorBottomSheet(descriptionString: AnnotatedString) {
        nav(
            MainNavigationDirections.actionGlobalSingleButtonBottomSheet(
                titleAnnotatedString = AnnotatedString(R.string.wrong_passphrase),
                drawableResId = R.drawable.ic_error,
                drawableTintResId = R.color.error_tint_color,
                descriptionAnnotatedString = descriptionString,
                isResultNeeded = false,
            )
        )
    }

    private fun onOptionsClick() {
        nav(RecoverWithPassphraseFragmentDirections.actionRecoverWithPassphraseFragmentToRecoverOptionsBottomSheet())
    }

    private fun onRecoverClick() {
        verifyMnemonic()
    }
}
