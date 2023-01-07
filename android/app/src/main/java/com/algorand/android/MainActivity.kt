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

@file:Suppress("TooManyFunctions") // TODO: We should remove this after function count decrease under 25

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

package com.algorand.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import com.algorand.android.core.TransactionManager
import com.algorand.android.customviews.AlertView
import com.algorand.android.customviews.CoreActionsTabBarView
import com.algorand.android.customviews.LedgerLoadingDialog
import com.algorand.android.customviews.customsnackbar.CustomSnackbar
import com.algorand.android.models.AccountCacheStatus
import com.algorand.android.models.AnnotatedString
import com.algorand.android.models.AssetAction
import com.algorand.android.models.AssetActionResult
import com.algorand.android.models.AssetOperationResult
import com.algorand.android.models.AssetTransaction
import com.algorand.android.models.Node
import com.algorand.android.models.NotificationMetadata
import com.algorand.android.models.NotificationType
import com.algorand.android.models.SignedTransactionDetail
import com.algorand.android.models.TransactionData
import com.algorand.android.models.TransactionManagerResult
import com.algorand.android.models.WalletConnectSession
import com.algorand.android.models.WalletConnectTransaction
import com.algorand.android.modules.assets.action.optin.UnsupportedAssetNotificationRequestActionBottomSheet
import com.algorand.android.modules.dapp.moonpay.domain.model.MoonpayTransactionStatus
import com.algorand.android.modules.deeplink.domain.model.BaseDeepLink
import com.algorand.android.modules.deeplink.ui.DeeplinkHandler
import com.algorand.android.modules.perawebview.ui.BasePeraWebViewFragment
import com.algorand.android.modules.qrscanning.QrScannerViewModel
import com.algorand.android.modules.walletconnect.connectionrequest.ui.WalletConnectConnectionBottomSheet
import com.algorand.android.modules.walletconnect.connectionrequest.ui.model.WCSessionRequestResult
import com.algorand.android.modules.webexport.model.WebExportQrCode
import com.algorand.android.ui.accountselection.receive.ReceiveAccountSelectionFragment
import com.algorand.android.ui.lockpreference.AutoLockSuggestionManager
import com.algorand.android.usecase.IsAccountLimitExceedUseCase.Companion.MAX_NUMBER_OF_ACCOUNTS
import com.algorand.android.utils.DEEPLINK_AND_NAVIGATION_INTENT
import com.algorand.android.utils.Event
import com.algorand.android.utils.Resource
import com.algorand.android.utils.WC_TRANSACTION_ID_INTENT_KEY
import com.algorand.android.utils.analytics.logTapReceive
import com.algorand.android.utils.analytics.logTapSend
import com.algorand.android.utils.extensions.collectLatestOnLifecycle
import com.algorand.android.utils.handleIntentWithBundle
import com.algorand.android.utils.inappreview.InAppReviewManager
import com.algorand.android.utils.isNotificationCanBeShown
import com.algorand.android.utils.navigateSafe
import com.algorand.android.utils.preference.isPasswordChosen
import com.algorand.android.utils.sendErrorLog
import com.algorand.android.utils.showWithStateCheck
import com.algorand.android.utils.walletconnect.WalletConnectTransactionErrorProvider
import com.algorand.android.utils.walletconnect.WalletConnectUrlHandler
import com.algorand.android.utils.walletconnect.WalletConnectViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import network.voi.hera.HomeNavigationDirections
import network.voi.hera.MainNavigationDirections
import network.voi.hera.R

@AndroidEntryPoint
class MainActivity :
    CoreMainActivity(),
    AlertView.AlertViewListener,
    UnsupportedAssetNotificationRequestActionBottomSheet.RequestAssetConfirmationListener,
    WalletConnectConnectionBottomSheet.Callback,
    ReceiveAccountSelectionFragment.ReceiveAccountSelectionFragmentListener {

    val mainViewModel: MainViewModel by viewModels()
    private val walletConnectViewModel: WalletConnectViewModel by viewModels()
    private val qrScannerViewModel: QrScannerViewModel by viewModels()

    private var pendingIntent: Intent? = null

    private var ledgerLoadingDialog: LedgerLoadingDialog? = null

    @Inject
    lateinit var transactionManager: TransactionManager

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var inAppReviewManager: InAppReviewManager

    @Inject
    lateinit var autoLockSuggestionManager: AutoLockSuggestionManager

    @Inject
    lateinit var errorProvider: WalletConnectTransactionErrorProvider

    var isAppUnlocked: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue && isAssetSetupCompleted) {
            handleRedirection()
        }
    }

    private var isAssetSetupCompleted: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue && newValue && isAppUnlocked) {
            handleRedirection()
        }
    }

    private val addAssetResultObserver = Observer<Event<Resource<AssetOperationResult>>> {
        it.consume()?.use(
            onSuccess = { assetOperationResult -> showAssetOperationForegroundNotification(assetOperationResult) },
            onFailed = { error -> showGlobalError(errorMessage = error.parse(this), tag = activityTag) }
        )
    }

    private val assetSetupCompletedObserver = Observer<AccountCacheStatus> {
        isAssetSetupCompleted = it == AccountCacheStatus.DONE
        binding.coreActionsTabBarView.setCoreActionButtonEnabled(it == AccountCacheStatus.DONE)
    }

    private val autoLockManagerObserver = Observer<Event<Any>> {
        it.consume()?.let {
            if (accountManager.isThereAnyRegisteredAccount() && isAppUnlocked && sharedPref.isPasswordChosen()) {
                isAppUnlocked = false
                nav(MainNavigationDirections.actionGlobalLockFragment())
            }
        }
    }

    private val newNotificationObserver = Observer<Event<NotificationMetadata>> {
        it?.consume()?.let { newNotificationData ->
            with(newNotificationData) {
                val notificationType = getNotificationType()
                if (navController.isNotificationCanBeShown(notificationType, isAppUnlocked).not()) {
                    return@let
                }
                when (notificationType) {
                    NotificationType.ASSET_SUPPORT_REQUEST -> handleAssetSupportRequest(this)
                    else -> binding.alertView.addAlertNotification(this)
                }
            }
        }
    }

    private val invalidTransactionCauseObserver = Observer<Event<Resource.Error.Local>> { cause ->
        cause.consume()?.let { onInvalidWalletConnectTransacitonReceived(it) }
    }

    private val swapNavigationDirectionCollector: suspend (Event<NavDirections>?) -> Unit = {
        it?.consume()?.let { navDirection -> nav(navDirection) }
    }

    private val walletConnectUrlHandlerListener = object : WalletConnectUrlHandler.Listener {
        override fun onValidWalletConnectUrl(url: String) {
            showProgress()
            walletConnectViewModel.connectToSessionByUrl(url)
        }

        override fun onInvalidWalletConnectUrl(errorResId: Int) {
            qrScannerViewModel.setQrCodeInProgress(false)
            showGlobalError(errorMessage = getString(errorResId), tag = activityTag)
        }
    }

    private val deepLinkHandlerListener = object : DeeplinkHandler.Listener {

        override fun onAssetTransferDeepLink(assetTransaction: AssetTransaction): Boolean {
            return true.also {
                navController.navigateSafe(HomeNavigationDirections.actionGlobalSendAlgoNavigation(assetTransaction))
            }
        }

        override fun onAccountAddressDeeplink(accountAddress: String, label: String?): Boolean {
            return true.also {
                navController.navigateSafe(
                    HomeNavigationDirections.actionGlobalAccountsAddressScanActionBottomSheet(accountAddress, label)
                )
            }
        }

        override fun onWalletConnectConnectionDeeplink(wcUrl: String): Boolean {
            return true.also {
                handleWalletConnectUrl(wcUrl)
            }
        }

        override fun onAssetTransferWithNotOptInDeepLink(assetId: Long): Boolean {
            return true.also {
                val assetAction = AssetAction(assetId = assetId)
                navController.navigateSafe(
                    HomeNavigationDirections.actionGlobalUnsupportedAddAssetTryLaterBottomSheet(assetAction)
                )
            }
        }

        override fun onMoonpayResultDeepLink(accountAddress: String, txnStatus: String, txnId: String?): Boolean {
            mainViewModel.logMoonpayAlgoBuyCompletedEvent()
            return true.also {
                navController.navigateSafe(
                    HomeNavigationDirections.actionGlobalMoonpayResultNavigation(
                        walletAddress = accountAddress,
                        transactionStatus = MoonpayTransactionStatus.getByValueOrDefault(txnStatus)
                    )
                )
            }
        }

        override fun onWebExportQrCodeDeepLink(webExportQrCode: WebExportQrCode): Boolean {
            return true.also {
                nav(
                    HomeNavigationDirections.actionGlobalWebExportNavigation(
                        backupId = webExportQrCode.backupId,
                        modificationKey = webExportQrCode.modificationKey,
                        encryptionKey = webExportQrCode.encryptionKey
                    )
                )
            }
        }

        override fun onAssetOptInDeepLink(assetAction: AssetAction): Boolean {
            return true.also {
                navController.navigateSafe(
                    HomeNavigationDirections.actionGlobalAddAssetAccountSelectionFragment(assetAction.assetId)
                )
            }
        }

        override fun onUndefinedDeepLink(undefinedDeeplink: BaseDeepLink.UndefinedDeepLink) {
            // TODO show error after discussing with the team
        }

        override fun onDeepLinkNotHandled(deepLink: BaseDeepLink) {
            // TODO show error after discussing with the team
        }
    }

    private val transactionManagerResultObserver = Observer<Event<TransactionManagerResult>?> {
        it?.consume()?.let { result ->
            when (result) {
                is TransactionManagerResult.Success -> {
                    hideLedgerLoadingDialog()
                    val signedTransactionDetail = result.signedTransactionDetail
                    if (signedTransactionDetail is SignedTransactionDetail.AssetOperation) {
                        mainViewModel.sendAssetOperationSignedTransaction(signedTransactionDetail)
                    }
                }
                is TransactionManagerResult.Error.GlobalWarningError -> {
                    hideLedgerLoadingDialog()
                    val (title, errorMessage) = result.getMessage(this)
                    showGlobalError(title = title, errorMessage = errorMessage, tag = activityTag)
                }
                is TransactionManagerResult.Error.SnackbarError -> {
                    hideLedgerLoadingDialog()
                    CustomSnackbar.Builder()
                        .setTitleTextResId(result.titleResId)
                        .setDescriptionTextResId(result.descriptionResId)
                        .setActionButtonTextResId(result.buttonTextResId)
                        .setActionButtonClickListener {
                            retryLatestAssetAdditionTransaction().also { dismiss() }
                        }
                        .build()
                        .show(binding.root)
                }
                is TransactionManagerResult.LedgerWaitingForApproval -> showLedgerLoadingDialog(result.bluetoothName)
                is TransactionManagerResult.Loading -> showProgress()
                is TransactionManagerResult.LedgerScanFailed -> {
                    hideLedgerLoadingDialog()
                    navigateToConnectionIssueBottomSheet()
                }
                else -> {
                    sendErrorLog("Unhandled else case in transactionManagerResultLiveData")
                }
            }
        }
    }

    private fun retryLatestAssetAdditionTransaction() {
        mainViewModel.getLatestAddAssetTransaction()?.let { transactionData ->
            sendAssetOperationTransaction(transactionData)
        }
    }

    private fun onNewSessionEvent(sessionEvent: Event<Resource<WalletConnectSession>>) {
        sessionEvent.consume()?.use(
            onSuccess = ::onSessionConnected,
            onFailed = ::onSessionFailed,
            onLoading = ::showProgress,
            onLoadingFinished = ::hideProgress
        )
    }

    private fun onSessionConnected(wcSessionRequest: WalletConnectSession) {
        nav(HomeNavigationDirections.actionGlobalWalletConnectConnectionNavigation(wcSessionRequest))
    }

    private fun onSessionFailed(error: Resource.Error) {
        qrScannerViewModel.setQrCodeInProgress(false)
        val errorMessage = error.parse(this)
        showGlobalError(errorMessage = errorMessage, tag = activityTag)
    }

    private fun handleAssetSupportRequest(notificationMetadata: NotificationMetadata) {
        if (mainViewModel.canAccountSignTransaction(notificationMetadata.receiverPublicKey)) {
            val assetInformation = notificationMetadata.getAssetDescription().convertToAssetInformation()
            val assetAction = AssetAction(
                assetId = assetInformation.assetId,
                publicKey = notificationMetadata.receiverPublicKey,
                asset = assetInformation
            )
            nav(HomeNavigationDirections.actionGlobalUnsupportedAssetNotificationRequestActionBottomSheet(assetAction))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        with(mainViewModel) {
            setupAutoLockManager(lifecycle)
            setDeepLinkHandlerListener(deepLinkHandlerListener)
        }
        setupCoreActionsTabBarView()

        binding.alertView.apply {
            setScope(lifecycle.coroutineScope)
            setListener(this@MainActivity)
            accounts = accountManager.getAccounts()
        }

        initObservers()

        if (savedInstanceState == null) {
            handleDeeplinkAndNotificationNavigation()
        }

        mainViewModel.increseAppOpeningCount()
    }

    override fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.algoPriceFragment -> {
                mainViewModel.logBottomNavAlgoPriceTapEvent()
            }
            R.id.accountsFragment -> {
                mainViewModel.logBottomNavAccountsTapEvent()
            }
        }
    }

    override fun onAlertNotificationClick(publicKeyToActivate: String?, assetIdToActivate: Long?) {
        if (publicKeyToActivate != null && assetIdToActivate != null) {
            val accountCacheData = accountCacheManager.getCacheData(publicKeyToActivate)
            val assetInformation = accountCacheManager.getAssetInformation(publicKeyToActivate, assetIdToActivate)
            if (accountCacheData != null && assetInformation != null) {
                nav(
                    HomeNavigationDirections
                        .actionGlobalAssetProfileNavigation(assetInformation.assetId, accountCacheData.account.address)
                )
            }
        }
    }

    private fun showAssetOperationForegroundNotification(assetOperationResult: AssetOperationResult) {
        val safeAssetName = assetOperationResult.assetName.getName(resources)
        val messageDescription = getString(assetOperationResult.resultTitleResId, safeAssetName)
        showAlertSuccess(title = messageDescription, successMessage = null, tag = activityTag)
    }

    override fun onAccountSelected(publicKey: String) {
        val qrCodeTitle = getString(R.string.qr_code)
        nav(HomeNavigationDirections.actionGlobalShowQrNavigation(qrCodeTitle, publicKey))
    }

    private fun initObservers() {
        peraNotificationManager.newNotificationLiveData.observe(this, newNotificationObserver)

        mainViewModel.assetOperationResultLiveData.observe(this, addAssetResultObserver)

        lifecycleScope.launch {
            // Drop 1 added to get any list changes.
            accountManager.accounts.drop(1).collect { accounts ->
                mainViewModel.refreshFirebasePushToken(null)
                binding.alertView.accounts = accounts
            }
        }

        contactsDao.getAllLiveData().observe(
            this,
            Observer { contactList ->
                binding.alertView.contacts = contactList
            }
        )

        transactionManager.transactionManagerResultLiveData.observe(this, transactionManagerResultObserver)

        mainViewModel.autoLockLiveData.observe(this, autoLockManagerObserver)

        mainViewModel.accountBalanceSyncStatus.observe(this, assetSetupCompletedObserver)

        walletConnectViewModel.requestLiveData.observe(this, ::handleWalletConnectTransactionRequest)

        walletConnectViewModel.invalidTransactionCauseLiveData.observe(this, invalidTransactionCauseObserver)

        collectLatestOnLifecycle(
            mainViewModel.swapNavigationResultFlow,
            swapNavigationDirectionCollector
        )

        lifecycleScope.launch {
            walletConnectViewModel.sessionResultFlow.collectLatest(::onNewSessionEvent)
        }

        walletConnectViewModel.setWalletConnectSessionTimeoutListener(::onWalletConnectSessionTimedOut)
    }

    private fun navigateToConnectionIssueBottomSheet() {
        nav(HomeNavigationDirections.actionGlobalLedgerConnectionIssueBottomSheet())
    }

    private fun onInvalidWalletConnectTransacitonReceived(error: Resource.Error) {
        val annotatedDescriptionErrorString = AnnotatedString(
            stringResId = R.string.your_walletconnect_request_failed,
            replacementList = listOf("error_message" to error.parse(this).toString())
        )
        nav(
            MainNavigationDirections.actionGlobalSingleButtonBottomSheet(
                titleAnnotatedString = AnnotatedString(R.string.uh_oh_something),
                drawableResId = R.drawable.ic_error,
                drawableTintResId = R.color.error_tint_color,
                descriptionAnnotatedString = annotatedDescriptionErrorString,
                isDraggable = false
            )
        )
    }

    private fun handleWalletConnectTransactionRequest(requestEvent: Event<Resource<WalletConnectTransaction>>?) {
        requestEvent?.consume()?.use(onSuccess = ::onNewWalletConnectTransactionRequest)
    }

    private fun onNewWalletConnectTransactionRequest(transaction: WalletConnectTransaction) {
        if (isAppUnlocked) {
            nav(
                directions = MainNavigationDirections.actionGlobalWalletConnectRequestNavigation(
                    shouldSkipConfirmation = isBasePeraWebViewFragmentActive()
                ),
                onError = { saveWcTransactionToPendingIntent(transaction.requestId) }
            )
        } else {
            saveWcTransactionToPendingIntent(transaction.requestId)
        }
    }

    private fun saveWcTransactionToPendingIntent(transactionRequestId: Long) {
        pendingIntent = Intent().apply {
            putExtra(WC_TRANSACTION_ID_INTENT_KEY, transactionRequestId)
        }
    }

    private fun handleDeeplinkAndNotificationNavigation() {
        intent.getParcelableExtra<Intent?>(DEEPLINK_AND_NAVIGATION_INTENT)?.apply {
            pendingIntent = this
            handlePendingIntent()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        pendingIntent = intent?.getParcelableExtra(DEEPLINK_AND_NAVIGATION_INTENT)
        handlePendingIntent()
    }

    private fun handlePendingIntent(): Boolean {
        return pendingIntent?.run {
            val canPendingBeHandled = isAssetSetupCompleted && isAppUnlocked && mainViewModel.isLockNeeded().not()
            if (canPendingBeHandled) {
                if (dataString != null) {
                    mainViewModel.handleDeepLink(dataString.orEmpty())
                } else {
                    navController.handleIntentWithBundle(
                        intentToHandle = this,
                        accountCacheManager = accountCacheManager,
                        onIntentHandlingFailed = ::onIntentHandlingFailed
                    )
                }
                pendingIntent = null
            }
            canPendingBeHandled
        } ?: false
    }

    fun handleWalletConnectUrl(walletConnectUrl: String) {
        walletConnectViewModel.handleWalletConnectUrl(
            url = walletConnectUrl,
            listener = walletConnectUrlHandlerListener
        )
    }

    fun isBasePeraWebViewFragmentActive(): Boolean {
        return (supportFragmentManager.findFragmentById(binding.navigationHostFragment.id) as NavHostFragment)
            .childFragmentManager.fragments[0] is BasePeraWebViewFragment
    }

    private fun onIntentHandlingFailed(@StringRes errorMessageResId: Int) {
        showGlobalError(errorMessage = getString(errorMessageResId))
    }

    private fun setupCoreActionsTabBarView() {
        binding.coreActionsTabBarView.setListener(object : CoreActionsTabBarView.Listener {
            override fun onSendClick() {
                firebaseAnalytics.logTapSend()
                nav(HomeNavigationDirections.actionGlobalSendAlgoNavigation(null))
            }

            override fun onReceiveClick() {
                firebaseAnalytics.logTapReceive()
                nav(HomeNavigationDirections.actionGlobalReceiveAccountSelectionFragment())
            }

            override fun onBuyAlgoClick() {
                mainViewModel.logBottomNavigationBuyAlgoEvent()
                navToMoonpayIntroFragment()
            }

            override fun onScanQRClick() {
                navToQRCodeScannerNavigation()
            }

            override fun onCoreActionsClick(isCoreActionsOpen: Boolean) {
                binding.bottomNavigationView.menu.forEach { menuItem ->
                    menuItem.isEnabled = isCoreActionsOpen.not()
                }
                handleBottomBarNavigationForChosenNetwork()
            }

            override fun onSwapClick() {
                mainViewModel.onSwapActionButtonClick()
            }
        })
    }

    private fun handleRedirection() {
        val isPendingIntentHandled = handlePendingIntent()
        if (isPendingIntentHandled) {
            return
        }
        val isInAppReviewStarted = inAppReviewManager.start(this@MainActivity)
        if (isInAppReviewStarted) {
            return
        }
        if (accountManager.isThereAnyRegisteredAccount()) {
            autoLockSuggestionManager.start(this@MainActivity)
        }
    }

    fun onNewNodeActivated(previousNode: Node, activatedNode: Node) {
        mainViewModel.refreshFirebasePushToken(previousNode)
        mainViewModel.resetBlockPolling()
        checkIfConnectedToTestNet()
    }

    override fun onSessionRequestResult(wCSessionRequestResult: WCSessionRequestResult) {
        handleSessionConnectionResult(wCSessionRequestResult)
    }

    private fun handleSessionConnectionResult(result: WCSessionRequestResult) {
        with(walletConnectViewModel) {
            when (result) {
                is WCSessionRequestResult.ApproveRequest -> approveSession(result)
                is WCSessionRequestResult.RejectRequest -> rejectSession(result.wcSessionRequest)
            }
        }
    }

    override fun onUnsupportedAssetRequest(assetActionResult: AssetActionResult) {
        signAddAssetTransaction(assetActionResult)
    }

    fun signAddAssetTransaction(assetActionResult: AssetActionResult) {
        if (!assetActionResult.publicKey.isNullOrBlank()) {
            val accountCacheData = accountCacheManager.getCacheData(assetActionResult.publicKey) ?: return
            val transactionData = TransactionData.AddAsset(accountCacheData, assetActionResult.asset)
            mainViewModel.setLatestAddAssetTransaction(transactionData)
            sendAssetOperationTransaction(transactionData)
        }
    }

    fun signRemoveAssetTransaction(assetActionResult: AssetActionResult) {
        if (!assetActionResult.publicKey.isNullOrBlank()) {
            val accountCacheData = accountCacheManager.getCacheData(assetActionResult.publicKey) ?: return
            val transactionData = with(assetActionResult) {
                TransactionData.RemoveAsset(accountCacheData, asset, asset.creatorPublicKey.orEmpty())
            }
            sendAssetOperationTransaction(transactionData)
        }
    }

    private fun sendAssetOperationTransaction(transactionData: TransactionData) {
        transactionManager.setup(lifecycle)
        transactionManager.initSigningTransactions(
            isGroupTransaction = false,
            transactionData
        )
    }

    private fun onWalletConnectSessionTimedOut() {
        navToWalletConnectSessionTimeoutDialog()
    }

    private fun navToWalletConnectSessionTimeoutDialog() {
        hideProgress()
        nav(
            MainNavigationDirections.actionGlobalSingleButtonBottomSheet(
                titleAnnotatedString = AnnotatedString(R.string.connection_failed),
                drawableResId = R.drawable.ic_error,
                drawableTintResId = R.color.error_tint_color,
                descriptionAnnotatedString = AnnotatedString(R.string.we_are_sorry_but_the),
            )
        )
    }

    private fun navToMoonpayIntroFragment() {
        nav(HomeNavigationDirections.actionGlobalMoonpayNavigation())
    }

    private fun navToQRCodeScannerNavigation() {
        nav(HomeNavigationDirections.actionGlobalAccountsQrScannerFragment())
    }

    fun showMaxAccountLimitExceededError() {
        showGlobalError(
            title = getString(R.string.too_many_accounts),
            errorMessage = getString(R.string.looks_like_already_have_accounts, MAX_NUMBER_OF_ACCOUNTS),
            tag = activityTag
        )
    }

    private fun hideLedgerLoadingDialog() {
        hideProgress()
        ledgerLoadingDialog?.dismissAllowingStateLoss()
        ledgerLoadingDialog = null
    }

    private fun showLedgerLoadingDialog(ledgerName: String?) {
        if (ledgerLoadingDialog == null) {
            ledgerLoadingDialog = LedgerLoadingDialog.createLedgerLoadingDialog(ledgerName)
            ledgerLoadingDialog?.showWithStateCheck(supportFragmentManager)
        }
    }

    companion object {
        fun newIntentWithDeeplinkOrNavigation(
            context: Context,
            deepLinkIntent: Intent
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(DEEPLINK_AND_NAVIGATION_INTENT, deepLinkIntent)
            }
        }
    }
}
