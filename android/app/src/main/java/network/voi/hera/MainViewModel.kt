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

package network.voi.hera

import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import network.voi.hera.banner.domain.usecase.BannersUseCase
import network.voi.hera.core.AccountManager
import network.voi.hera.core.BaseViewModel
import network.voi.hera.database.NodeDao
import network.voi.hera.deviceregistration.domain.usecase.DeviceIdMigrationUseCase
import network.voi.hera.deviceregistration.domain.usecase.DeviceIdUseCase
import network.voi.hera.deviceregistration.domain.usecase.DeviceRegistrationUseCase
import network.voi.hera.deviceregistration.domain.usecase.FirebasePushTokenUseCase
import network.voi.hera.deviceregistration.domain.usecase.UpdatePushTokenUseCase
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.AssetOperationResult
import network.voi.hera.models.Node
import network.voi.hera.models.SignedTransactionDetail
import network.voi.hera.models.TransactionData
import network.voi.hera.modules.appopencount.domain.usecase.IncreaseAppOpeningCountUseCase
import network.voi.hera.modules.deeplink.ui.DeeplinkHandler
import network.voi.hera.modules.swap.utils.SwapNavigationDestinationHelper
import network.voi.hera.modules.tracking.main.MainActivityEventTracker
import network.voi.hera.modules.tutorialdialog.domain.usecase.TutorialUseCase
import network.voi.hera.network.AlgodInterceptor
import network.voi.hera.network.IndexerInterceptor
import network.voi.hera.network.MobileHeaderInterceptor
import network.voi.hera.usecase.AccountCacheStatusUseCase
import network.voi.hera.usecase.EncryptedPinUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.SendSignedTransactionUseCase
import network.voi.hera.utils.AccountCacheManager
import network.voi.hera.utils.AssetName
import network.voi.hera.utils.AutoLockManager
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.Event
import network.voi.hera.utils.Resource
import network.voi.hera.utils.coremanager.AccountDetailCacheManager
import network.voi.hera.utils.exception.AccountAlreadyOptedIntoAssetException
import network.voi.hera.utils.exceptions.TransactionConfirmationAwaitException
import network.voi.hera.utils.exceptions.TransactionIdNullException
import network.voi.hera.utils.findAllNodes
import network.voi.hera.utils.sendErrorLog
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val autoLockManager: AutoLockManager,
    private val sharedPref: SharedPreferences,
    private val nodeDao: NodeDao,
    private val indexerInterceptor: IndexerInterceptor,
    private val mobileHeaderInterceptor: MobileHeaderInterceptor,
    private val algodInterceptor: AlgodInterceptor,
    private val accountCacheManager: AccountCacheManager,
    private val bannersUseCase: BannersUseCase,
    private val deviceRegistrationUseCase: DeviceRegistrationUseCase,
    private val deviceIdMigrationUseCase: DeviceIdMigrationUseCase,
    private val firebasePushTokenUseCase: FirebasePushTokenUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val deviceIdUseCase: DeviceIdUseCase,
    private val mainActivityEventTracker: MainActivityEventTracker,
    private val deepLinkHandler: DeeplinkHandler,
    private val increaseAppOpeningCountUseCase: IncreaseAppOpeningCountUseCase,
    private val tutorialUseCase: TutorialUseCase,
    private val swapNavigationDestinationHelper: SwapNavigationDestinationHelper,
    private val sendSignedTransactionUseCase: SendSignedTransactionUseCase,
    private val encryptedPinUseCase: EncryptedPinUseCase,
    private val accountManager: AccountManager,
    private val accountCacheStatusUseCase: AccountCacheStatusUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val accountDetailCacheManager: AccountDetailCacheManager
) : BaseViewModel() {

    // TODO: Replace this with Flow whenever have time
    val assetOperationResultLiveData = MutableLiveData<Event<Resource<AssetOperationResult>>>()

    // TODO I'll change after checking usage of flow in activity.
    val accountBalanceSyncStatus = accountCacheStatusUseCase.getAccountCacheStatusFlow().asLiveData()

    private val _swapNavigationResultFlow = MutableStateFlow<Event<NavDirections>?>(null)
    val swapNavigationResultFlow: StateFlow<Event<NavDirections>?>
        get() = _swapNavigationResultFlow

    val autoLockLiveData
        get() = autoLockManager.autoLockLiveData

    private var sendTransactionJob: Job? = null
    var refreshBalanceJob: Job? = null
    var registerDeviceJob: Job? = null

    private var latestFailedAddAssetTransaction: TransactionData.AddAsset? = null

    init {
        initializeAccountCacheManager()
        initializeNodeInterceptor()
        observeFirebasePushToken()
        refreshFirebasePushToken(null)
        initializeTutorial()
    }

    private fun observeFirebasePushToken() {
        viewModelScope.launch {
            firebasePushTokenUseCase.getPushTokenCacheFlow().collect {
                if (it?.data.isNullOrBlank().not()) registerFirebasePushToken(it?.data.orEmpty())
            }
        }
    }

    private fun initializeNodeInterceptor() {
        viewModelScope.launch(Dispatchers.IO) {
            if (indexerInterceptor.currentActiveNode == null) {
                val lastActivatedNode = findAllNodes(sharedPref, nodeDao).find { it.isActive }
                lastActivatedNode?.activate(indexerInterceptor, mobileHeaderInterceptor, algodInterceptor)
            }
            migrateDeviceIdIfNeed()
        }
    }

    private fun registerFirebasePushToken(token: String) {
        registerDeviceJob?.cancel()
        registerDeviceJob = viewModelScope.launch(Dispatchers.IO) {
            deviceRegistrationUseCase.registerDevice(token).collect {
                if (it is DataResource.Success) bannersUseCase.initializeBanner(deviceId = it.data)
            }
        }
    }

    fun refreshFirebasePushToken(previousNode: Node?) {
        if (previousNode != null) deletePreviousNodePushToken(previousNode)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            firebasePushTokenUseCase.setPushToken(token)
        }
    }

    private fun deletePreviousNodePushToken(previousNode: Node) {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceId = deviceIdUseCase.getNodeDeviceId(previousNode) ?: run {
                return@launch
            }
            updatePushTokenUseCase.updatePushToken(deviceId, null, previousNode.networkSlug).collect()
        }
    }

    private suspend fun migrateDeviceIdIfNeed() {
        deviceIdMigrationUseCase.migrateDeviceIdIfNeed()
    }

    private fun initializeAccountCacheManager() {
        viewModelScope.launch(Dispatchers.IO) {
            accountCacheManager.initializeAccountCacheMap()
        }
    }

    /**
     * If we are going to re-enable block polling manager again, we should enable this job here.
     */
    fun resetBlockPolling() {
        refreshBalanceJob?.cancel()
        accountDetailCacheManager.startJob()
        // blockPollingManager.startJob()
    }

    fun sendAssetOperationSignedTransaction(transaction: SignedTransactionDetail.AssetOperation) {
        if (sendTransactionJob?.isActive == true) {
            return
        }

        sendTransactionJob = viewModelScope.launch(Dispatchers.IO) {
            sendSignedTransactionUseCase.sendSignedTransaction(transaction).collectLatest { dataResource ->
                when (dataResource) {
                    is DataResource.Success -> {
                        latestFailedAddAssetTransaction = null
                        val assetActionResult = getAssetOperationResult(transaction)
                        assetOperationResultLiveData.postValue(Event(Resource.Success(assetActionResult)))
                    }
                    is DataResource.Error.Api -> {
                        assetOperationResultLiveData.postValue(Event(Resource.Error.Api(dataResource.exception)))
                    }
                    is DataResource.Error.Local -> {
                        // TODO add specific strings for exceptions
                        val errorResourceId = when (dataResource.exception) {
                            is AccountAlreadyOptedIntoAssetException -> {
                                R.string.you_are_already
                            }
                            is TransactionConfirmationAwaitException -> {
                                R.string.transaction_confirmation_timed_out
                            }
                            is TransactionIdNullException -> {
                                R.string.an_error_occured
                            }
                            else -> {
                                R.string.an_error_occured
                            }
                        }
                        val assetName = transaction.assetInformation.fullName.toString()
                        assetOperationResultLiveData.postValue(
                            Event(
                                Resource.Error.GlobalWarning(
                                    titleRes = R.string.error,
                                    annotatedString = AnnotatedString(
                                        stringResId = errorResourceId,
                                        replacementList = listOf("asset_name" to assetName)
                                    )
                                )
                            )
                        )
                    }
                    else -> {
                        sendErrorLog("Unhandled else case in MainViewModel.sendSignedTransaction")
                    }
                }
            }
        }
    }

    fun getLatestAddAssetTransaction(): TransactionData.AddAsset? {
        return latestFailedAddAssetTransaction
    }

    fun setLatestAddAssetTransaction(transactionData: TransactionData.AddAsset) {
        latestFailedAddAssetTransaction = transactionData
    }

    fun handleDeepLink(uri: String) {
        deepLinkHandler.handleDeepLink(uri)
    }

    fun setDeepLinkHandlerListener(listener: DeeplinkHandler.Listener) {
        deepLinkHandler.setListener(listener)
    }

    fun setupAutoLockManager(lifecycle: Lifecycle) {
        autoLockManager.registerAppLifecycle(lifecycle)
    }

    fun logBottomNavAlgoPriceTapEvent() {
        viewModelScope.launch {
            mainActivityEventTracker.logAlgoPriceTapEvent()
        }
    }

    fun logBottomNavAccountsTapEvent() {
        viewModelScope.launch {
            mainActivityEventTracker.logAccountsTapEvent()
        }
    }

    fun logBottomNavigationBuyAlgoEvent() {
        viewModelScope.launch {
            mainActivityEventTracker.logBottomNavigationAlgoBuyTapEvent()
        }
    }

    fun logMoonpayAlgoBuyCompletedEvent() {
        viewModelScope.launch {
            mainActivityEventTracker.logMoonpayAlgoBuyCompletedEvent()
        }
    }

    fun increseAppOpeningCount() {
        viewModelScope.launch {
            increaseAppOpeningCountUseCase.increaseAppOpeningCount()
        }
    }

    fun onSwapActionButtonClick() {
        viewModelScope.launch {
            mainActivityEventTracker.logQuickActionSwapButtonClickEvent()
            var swapNavDirection: NavDirections? = null
            swapNavigationDestinationHelper.getSwapNavigationDestination(
                onNavToIntroduction = {
                    swapNavDirection = HomeNavigationDirections.actionGlobalSwapIntroductionNavigation()
                },
                onNavToAccountSelection = {
                    swapNavDirection = HomeNavigationDirections.actionGlobalSwapAccountSelectionNavigation()
                },
                onNavToSwap = { accountAddress ->
                    swapNavDirection = HomeNavigationDirections.actionGlobalSwapNavigation(accountAddress)
                }
            )
            swapNavDirection?.let { direction ->
                _swapNavigationResultFlow.emit(Event(direction))
            }
        }
    }

    private fun initializeTutorial() {
        viewModelScope.launch {
            tutorialUseCase.initializeTutorial()
        }
    }

    fun isLockNeeded(): Boolean {
        return autoLockManager.isAutoLockNeeded() &&
            encryptedPinUseCase.isEncryptedPinSet() &&
            accountManager.isThereAnyRegisteredAccount()
    }

    fun canAccountSignTransaction(accountAddress: String?): Boolean {
        return accountAddress?.let {
            accountDetailUseCase.canAccountSignTransaction(accountAddress)
        } ?: false
    }

    private fun getAssetOperationResult(transaction: SignedTransactionDetail.AssetOperation): AssetOperationResult {
        val assetName = transaction.assetInformation.fullName ?: transaction.assetInformation.shortName
        val resultTitleResId = when (transaction) {
            is SignedTransactionDetail.AssetOperation.AssetAddition -> R.string.asset_successfully_opted_in
            is SignedTransactionDetail.AssetOperation.AssetRemoval -> R.string.asset_successfully_opted_out_from_your
        }
        return AssetOperationResult(
            resultTitleResId = resultTitleResId,
            assetName = AssetName.create(assetName),
            assetId = transaction.assetInformation.assetId
        )
    }
}
