//  *
//  * Copyright 2022 Pera Wallet, LDA
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License
//  */
//
//  ackage network.voi.hera.ui.wcconnection
//
//  mport android.content.Context
//  mport android.os.Bundle
//  mport android.view.View
//  mport androidx.fragment.app.viewModels
//  mport androidx.navigation.fragment.navArgs
//  mport network.voi.hera.R
//  mport network.voi.hera.core.BaseBottomSheet
//  mport network.voi.hera.databinding.BottomSheetWalletConnectConnectionBinding
//  mport network.voi.hera.models.AccountSelection
//  mport network.voi.hera.models.AnnotatedString
//  mport network.voi.hera.models.AssetInformation.Companion.ALGO_ID
//  mport network.voi.hera.models.WCSessionRequestResult
//  mport network.voi.hera.models.WCSessionRequestResult.ApproveRequest
//  mport network.voi.hera.ui.common.accountselector.AccountSelectionBottomSheet.Companion.ACCOUNT_SELECTION_KEY
//  mport network.voi.hera.utils.SingleButtonBottomSheet
//  mport network.voi.hera.utils.browser.openUrl
//  mport network.voi.hera.utils.extensions.collectOnLifecycle
//  mport network.voi.hera.utils.extensions.hide
//  mport network.voi.hera.utils.extensions.setAccountIconDrawable
//  mport network.voi.hera.utils.extensions.setTextAndVisibility
//  mport network.voi.hera.utils.getXmlStyledString
//  mport network.voi.hera.utils.loadPeerMetaIcon
//  mport network.voi.hera.utils.startSavedStateListener
//  mport network.voi.hera.utils.useSavedStateValue
//  mport network.voi.hera.utils.viewbinding.viewBinding
//  mport dagger.hilt.android.AndroidEntryPoint
//
//  AndroidEntryPoint
//  lass WalletConnectConnectionBottomSheet :
//     BaseBottomSheet(layoutResId = R.layout.bottom_sheet_wallet_connect_connection) {
//
//     private val binding by viewBinding(BottomSheetWalletConnectConnectionBinding::bind)
//     private val args: WalletConnectConnectionBottomSheetArgs by navArgs()
//     private val walletConnectConnectionViewModel by viewModels<WalletConnectConnectionViewModel>()
//
//     private val selectedAccountCollector: suspend (AccountSelection?) -> Unit = { accountCacheData ->
//         if (accountCacheData != null) initSelectedAccountUi(accountCacheData)
//     }
//
//     private var listener: Callback? = null
//
//     override fun onAttach(context: Context) {
//         super.onAttach(context)
//         listener = activity as? Callback
//     }
//
//     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//         super.onViewCreated(view, savedInstanceState)
//         setDraggableEnabled(isEnabled = false)
//         isCancelable = false
//         initUi()
//         initObservers()
//     }
//
//     private fun initUi() {
//         with(binding) {
//             cancelButton.setOnClickListener { onCancelClick() }
//             connectButton.setOnClickListener { onConnectClick() }
//             accountContainer.setOnClickListener { onAccountClick() }
//             initSessionPeerMetaUi()
//         }
//     }
//
//     private fun initObservers() {
//         viewLifecycleOwner.collectOnLifecycle(
//             walletConnectConnectionViewModel.selectedAccountFlow,
//             selectedAccountCollector
//         )
//     }
//
//     override fun onResume() {
//         super.onResume()
//         startSavedStateListener(R.id.walletConnectConnectionBottomSheet) {
//             useSavedStateValue<AccountSelection>(ACCOUNT_SELECTION_KEY) { result ->
//                 walletConnectConnectionViewModel.setSelectedAccount(result)
//             }
//             useSavedStateValue<Boolean>(SingleButtonBottomSheet.CLOSE_KEY) { isBrowserBottomSheetClosed ->
//                 if (isBrowserBottomSheetClosed) navBack()
//             }
//         }
//     }
//
//     private fun initSessionPeerMetaUi() {
//         val peerMeta = args.sessionRequest.peerMeta
//         with(binding) {
//             appIconImageView.loadPeerMetaIcon(peerMeta.peerIconUri.toString())
//             appUrlTextView.apply {
//                 text = peerMeta.url
//                 if (peerMeta.url.isNotBlank()) {
//                     setOnClickListener { context?.openUrl(peerMeta.url) }
//                 }
//             }
//             descriptionTextView.text = context?.getXmlStyledString(
//                 AnnotatedString(R.string.wallet_wants_to_connect, listOf("app_name" to peerMeta.name))
//             )
//         }
//     }
//
//     private fun onCancelClick() {
//         listener?.onSessionRequestResult(WCSessionRequestResult.RejectRequest(args.sessionRequest))
//         navBack()
//     }
//
//     private fun onConnectClick() {
//         walletConnectConnectionViewModel.getSelectedAccount()?.run {
//             listener?.onSessionRequestResult(
//                 ApproveRequest(
//                     address = accountAddress,
//                     wcSessionRequest = args.sessionRequest
//                 )
//             )
//             args.sessionRequest.fallbackBrowserGroupResponse?.let {
//                 navigateToFallbackBrowserSelectionBottomSheet(it, args.sessionRequest.peerMeta.name)
//             } ?: showConnectedDappInfoBottomSheet()
//         }
//     }
//
//     private fun navigateToFallbackBrowserSelectionBottomSheet(
//         fallbackBrowserGroupResponse: String,
//         peerMetaName: String
//     ) {
//         nav(
//             WalletConnectConnectionBottomSheetDirections
//                 .actionWalletConnectConnectionBottomSheetToWalletConnectConnectedFallbackBrowserSelectionBottomSheet(
//                     browserGroup = fallbackBrowserGroupResponse,
//                     peerMetaName = peerMetaName
//                 )
//         )
//     }
//
//     private fun showConnectedDappInfoBottomSheet() {
//         nav(
//             WalletConnectConnectionBottomSheetDirections
//                 .actionWalletConnectConnectionBottomSheetToSingleButtonBottomSheetNavigation(
//                     titleAnnotatedString = AnnotatedString(
//                         stringResId = R.string.you_are_connected,
//                         replacementList = listOf("peer_name" to args.sessionRequest.peerMeta.name)
//                     ),
//                     descriptionAnnotatedString = AnnotatedString(
//                         stringResId = R.string.please_return_to,
//                         replacementList = listOf("peer_name" to args.sessionRequest.peerMeta.name)
//                     ),
//                     drawableResId = R.drawable.ic_check_72dp
//                 )
//         )
//     }
//
//     private fun onAccountClick() {
//         val selectedAccount = walletConnectConnectionViewModel.getSelectedAccount()
//         nav(
//             WalletConnectConnectionBottomSheetDirections
//                 .actionWalletConnectConnectionBottomSheetToAccountSelectionBottomSheet(
//                     assetId = ALGO_ID,
//                     titleResId = R.string.accounts,
//                     selectedAccountAddress = selectedAccount?.accountAddress,
//                     showBackButton = true,
//                     showBalance = false
//                 )
//         )
//     }
//
//     private fun initSelectedAccountUi(accountSelection: AccountSelection) {
//         with(binding) {
//             with(accountSelection) {
//                 if (accountIconResource != null) {
//                     accountIconImageView.setAccountIconDrawable(
//                         accountIconResource = accountIconResource,
//                         iconSize = R.dimen.account_icon_size_large
//                     )
//                 }
//                 accountNameTextView.setTextAndVisibility(
//                     accountDisplayName?.getDisplayTextOrAccountShortenedAddress()
//                 )
//                 accountAssetCountTextView.setTextAndVisibility(setupAssetCount(accountAssetCount ?: 0))
//                 selectAccountTextView.hide()
//             }
//         }
//     }
//
//     private fun setupAssetCount(assetCount: Int): String {
//         return if (assetCount > 0) {
//             resources.getQuantityString(R.plurals.account_asset_count, assetCount, assetCount)
//         } else {
//             getString(R.string.account_asset_count_zero)
//         }
//     }
//
//     interface Callback {
//         fun onSessionRequestResult(wCSessionRequestResult: WCSessionRequestResult)
//     }
//
