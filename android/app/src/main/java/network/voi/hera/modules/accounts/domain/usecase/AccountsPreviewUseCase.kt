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

package network.voi.hera.modules.accounts.domain.usecase

import androidx.navigation.NavDirections
import network.voi.hera.R
import network.voi.hera.banner.domain.model.BaseBanner
import network.voi.hera.banner.domain.model.BaseBanner.GenericBanner
import network.voi.hera.banner.domain.model.BaseBanner.GovernanceBanner
import network.voi.hera.banner.domain.usecase.BannersUseCase
import network.voi.hera.banner.ui.mapper.BaseBannerItemMapper
import network.voi.hera.core.AccountManager
import network.voi.hera.customviews.accountandassetitem.mapper.AccountItemConfigurationMapper
import network.voi.hera.mapper.AccountPreviewMapper
import network.voi.hera.models.Account
import network.voi.hera.models.AccountDetail
import network.voi.hera.models.AccountIconResource
import network.voi.hera.modules.accounts.domain.mapper.AccountListItemMapper
import network.voi.hera.modules.accounts.domain.mapper.PortfolioValueItemMapper
import network.voi.hera.modules.accounts.domain.model.AccountPreview
import network.voi.hera.modules.accounts.domain.model.AccountValue
import network.voi.hera.modules.accounts.domain.model.BaseAccountListItem
import network.voi.hera.modules.accounts.domain.model.BaseAccountListItem.HeaderItem
import network.voi.hera.modules.accounts.domain.model.BasePortfolioValueItem
import network.voi.hera.modules.accounts.ui.AccountsFragmentDirections
import network.voi.hera.modules.currency.domain.usecase.CurrencyUseCase
import network.voi.hera.modules.parity.domain.model.SelectedCurrencyDetail
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.AccountSortPreferenceUseCase
import network.voi.hera.modules.sorting.accountsorting.domain.usecase.GetSortedAccountsByPreferenceUseCase
import network.voi.hera.modules.swap.reddot.domain.usecase.GetSwapFeatureRedDotVisibilityUseCase
import network.voi.hera.modules.swap.utils.SwapNavigationDestinationHelper
import network.voi.hera.modules.tutorialdialog.data.model.Tutorial
import network.voi.hera.modules.tutorialdialog.data.model.Tutorial.ACCOUNT_ADDRESS_COPY
import network.voi.hera.modules.tutorialdialog.data.model.Tutorial.SWAP
import network.voi.hera.modules.tutorialdialog.domain.usecase.TutorialUseCase
import network.voi.hera.nft.domain.usecase.SimpleCollectibleUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.NodeSettingsUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.CacheResult
import network.voi.hera.utils.Event
import network.voi.hera.utils.combine
import network.voi.hera.utils.formatAsCurrency
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// TODO Refactor this class for performance and code quality
@Suppress("LongParameterList")
class AccountsPreviewUseCase @Inject constructor(
    private val parityUseCase: ParityUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val assetDetailUseCase: SimpleAssetDetailUseCase,
    private val accountManager: AccountManager,
    private val accountPreviewMapper: AccountPreviewMapper,
    private val accountListItemMapper: AccountListItemMapper,
    private val simpleCollectibleUseCase: SimpleCollectibleUseCase,
    private val bannersUseCase: BannersUseCase,
    private val baseBannerItemMapper: BaseBannerItemMapper,
    private val nodeSettingsUseCase: NodeSettingsUseCase,
    private val portfolioValueItemMapper: PortfolioValueItemMapper,
    private val accountItemConfigurationMapper: AccountItemConfigurationMapper,
    private val getSortedAccountsByPreferenceUseCase: GetSortedAccountsByPreferenceUseCase,
    private val getAccountValueUseCase: GetAccountValueUseCase,
    private val accountSortPreferenceUseCase: AccountSortPreferenceUseCase,
    private val currencyUseCase: CurrencyUseCase,
    private val notificationStatusUseCase: NotificationStatusUseCase,
    private val getSwapFeatureRedDotVisibilityUseCase: GetSwapFeatureRedDotVisibilityUseCase,
    private val tutorialUseCase: TutorialUseCase,
    private val swapNavigationDestinationHelper: SwapNavigationDestinationHelper
) {

    suspend fun dismissTutorial(tutorialId: Int) {
        tutorialUseCase.dismissTutorial(tutorialId)
    }

    suspend fun getInitialAccountPreview(): AccountPreview {
        val isTestnetBadgeVisible = nodeSettingsUseCase.isSelectedNodeTestnet()
        return accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
    }

    suspend fun getAccountsPreview(previousState: AccountPreview): Flow<AccountPreview> {
        return combine(
            parityUseCase.getSelectedCurrencyDetailCacheFlow(),
            accountDetailUseCase.getAccountDetailCacheFlow(),
            bannersUseCase.getBanner(),
            tutorialUseCase.getTutorial(),
            nodeSettingsUseCase.getAllNodeAsFlow(),
            assetDetailUseCase.getCachedAssetsFlow()
        ) { selectedCurrencyParityCache, accountDetailCache, banner, tutorial, _, _ ->
            val isTestnetBadgeVisible = nodeSettingsUseCase.isSelectedNodeTestnet()
            val localAccounts = accountManager.getAccounts()
            if (localAccounts.isEmpty()) {
                return@combine accountPreviewMapper.getEmptyAccountListState(isTestnetBadgeVisible)
            }
            when (selectedCurrencyParityCache) {
                is CacheResult.Success -> {
                    processAccountsAndAssets(
                        accountDetailCache = accountDetailCache,
                        banner = banner,
                        isTestnetBadgeVisible = isTestnetBadgeVisible,
                        tutorial = tutorial
                    )
                }
                is CacheResult.Error -> getAlgoPriceErrorState(
                    selectedCurrencyDetailCache = selectedCurrencyParityCache,
                    previousState = previousState,
                    isTestnetBadgeVisible = isTestnetBadgeVisible
                )
                else -> accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
            }
        }
    }

    suspend fun onCloseBannerClick(bannerId: Long) {
        bannersUseCase.dismissBanner(bannerId)
    }

    suspend fun getSwapNavigationUpdatedPreview(previousState: AccountPreview): AccountPreview {
        var swapNavDirection: NavDirections? = null
        swapNavigationDestinationHelper.getSwapNavigationDestination(
            onNavToIntroduction = {
                swapNavDirection = AccountsFragmentDirections.actionAccountsFragmentToSwapIntroductionNavigation()
            },
            onNavToAccountSelection = {
                swapNavDirection = AccountsFragmentDirections.actionAccountsFragmentToSwapAccountSelectionNavigation()
            },
            onNavToSwap = { accountAddress ->
                swapNavDirection = AccountsFragmentDirections.actionAccountsFragmentToSwapNavigation(accountAddress)
            }
        )
        return swapNavDirection?.let { direction ->
            previousState.copy(swapNavigationDestinationEvent = Event(direction))
        } ?: previousState
    }

    private suspend fun getAlgoPriceErrorState(
        selectedCurrencyDetailCache: CacheResult.Error<SelectedCurrencyDetail>?,
        previousState: AccountPreview,
        isTestnetBadgeVisible: Boolean
    ): AccountPreview {
        val hasPreviousCachedValue = selectedCurrencyDetailCache?.data != null
        if (hasPreviousCachedValue) return previousState
        val accountErrorListItems = createAccountErrorItemList()
        val portfolioValuesError = portfolioValueItemMapper.mapToPortfolioValuesErrorItem()
        return accountPreviewMapper.getAlgoPriceInitialErrorState(
            accountListItems = accountErrorListItems,
            errorCode = selectedCurrencyDetailCache?.code,
            isTestnetBadgeVisible = isTestnetBadgeVisible,
            errorPortfolioValueItem = portfolioValuesError
        )
    }

    private suspend fun processAccountsAndAssets(
        accountDetailCache: HashMap<String, CacheResult<AccountDetail>>,
        banner: BaseBanner?,
        isTestnetBadgeVisible: Boolean,
        tutorial: Tutorial?
    ): AccountPreview {
        val areAllAccountsAreCached = accountDetailUseCase.areAllAccountsCached()
        return if (areAllAccountsAreCached) {
            processSuccessAccountCacheAndOthers(
                accountDetailCache = accountDetailCache,
                banner = banner,
                isTestnetBadgeVisible = isTestnetBadgeVisible,
                tutorial = tutorial
            )
        } else {
            accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
        }
    }

    private suspend fun processSuccessAccountCacheAndOthers(
        accountDetailCache: HashMap<String, CacheResult<AccountDetail>>,
        banner: BaseBanner?,
        isTestnetBadgeVisible: Boolean,
        tutorial: Tutorial?
    ): AccountPreview {
        val isThereAnyAssetNeedsToBeCached = accountDetailCache.values.any {
            !it.data?.accountInformation?.assetHoldingList.isNullOrEmpty()
        }
        return if (
            assetDetailUseCase.getCachedAssetList().isEmpty() &&
            simpleCollectibleUseCase.getCachedCollectibleList().isEmpty() &&
            isThereAnyAssetNeedsToBeCached
        ) {
            accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
        } else {
            prepareAccountPreview(
                banner = banner,
                isTestnetBadgeVisible = isTestnetBadgeVisible,
                tutorial = tutorial
            )
        }
    }

    private suspend fun prepareAccountPreview(
        banner: BaseBanner?,
        isTestnetBadgeVisible: Boolean,
        tutorial: Tutorial?
    ): AccountPreview {
        return withContext(Dispatchers.Default) {
            var primaryAccountValue = BigDecimal.ZERO
            var secondaryAccountValue = BigDecimal.ZERO

            val baseAccountListItems = getBaseAccountListItems(onAccountValueCalculated = {
                primaryAccountValue += it.primaryAccountValue
                secondaryAccountValue += it.secondaryAccountValue
            }).apply {
                val bannerItem = getBannerItemOrNull(baseBanner = banner)
                // TODO: Remove test banner
                insertQuickActionsItem(this)
                if (bannerItem != null) add(BANNER_ITEM_INDEX, bannerItem)
            }
            val isThereAnyErrorInAccountCache =
                accountDetailUseCase.isThereAnyCachedErrorAccount(excludeWatchAccounts = true)
            val isThereAnySuccessInAccountCache =
                accountDetailUseCase.isThereAnyCachedSuccessAccount(excludeWatchAccounts = true)
            val portfolioValueItem = if (!isThereAnyErrorInAccountCache) {
                getPortfolioValueSuccessItem(primaryAccountValue, secondaryAccountValue)
            } else if (isThereAnySuccessInAccountCache) {
                getPortfolioValuePartialErrorItem(primaryAccountValue, secondaryAccountValue)
            } else {
                portfolioValueItemMapper.mapToPortfolioValuesErrorItem()
            }

            val swapTutorialDisplayEvent = with(tutorial) {
                if (this == SWAP) Event(id) else null
            }
            val accountAddressCopyDisplayEvent = with(tutorial) {
                if (this == ACCOUNT_ADDRESS_COPY) Event(id) else null
            }
            val hasNewNotification = notificationStatusUseCase.hasNewNotification()
            accountPreviewMapper.getSuccessAccountPreview(
                accountListItems = baseAccountListItems,
                isTestnetBadgeVisible = isTestnetBadgeVisible,
                portfolioValueItem = portfolioValueItem,
                hasNewNotification = hasNewNotification,
                onSwapTutorialDisplayEvent = swapTutorialDisplayEvent,
                onAccountAddressCopyTutorialDisplayEvent = accountAddressCopyDisplayEvent,
            )
        }
    }

    private suspend fun insertQuickActionsItem(accountsList: MutableList<BaseAccountListItem>) {
        accountsList.add(
            index = QUICK_ACTIONS_ITEM_INDEX,
            element = accountListItemMapper.mapToQuickActionsItem(
                isSwapButtonSelected = getSwapFeatureRedDotVisibilityUseCase.getSwapFeatureRedDotVisibility()
            )
        )
    }

    private fun getBannerItemOrNull(baseBanner: BaseBanner?): BaseAccountListItem.BaseBannerItem? {
        return baseBanner?.let { banner ->
            val isButtonVisible = !banner.buttonTitle.isNullOrBlank() && !banner.buttonUrl.isNullOrBlank()
            val isTitleVisible = !banner.title.isNullOrBlank()
            val isDescriptionVisible = !banner.description.isNullOrBlank()
            with(baseBannerItemMapper) {
                when (banner) {
                    is GovernanceBanner -> {
                        mapToGovernanceBannerItem(banner, isButtonVisible, isTitleVisible, isDescriptionVisible)
                    }
                    is GenericBanner -> {
                        mapToGenericBannerItem(banner, isButtonVisible, isTitleVisible, isDescriptionVisible)
                    }
                }
            }
        }
    }

    @SuppressWarnings("LongMethod")
    private suspend fun getBaseAccountListItems(
        onAccountValueCalculated: (AccountValue) -> Unit
    ): MutableList<BaseAccountListItem> {
        val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrEmpty()
        val secondaryCurrencySymbol = parityUseCase.getSecondaryCurrencySymbol()
        val isPrimaryCurrencyAlgo = currencyUseCase.isPrimaryCurrencyAlgo()
        val isSecondaryCurrencyAlgo = !isPrimaryCurrencyAlgo
        val sortedAccountListItems = getSortedAccountsByPreferenceUseCase.getSortedAccountListItems(
            sortingPreferences = accountSortPreferenceUseCase.getAccountSortPreference(),
            onLoadedAccountConfiguration = {
                val accountBalance = getAccountValueUseCase.getAccountValue(this).also { accountValue ->
                    if (account.type != Account.Type.WATCH) {
                        onAccountValueCalculated.invoke(accountValue)
                    }
                }
                accountItemConfigurationMapper.mapTo(
                    accountAddress = account.address,
                    accountName = account.name,
                    accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                    accountType = account.type,
                    accountPrimaryValueText = accountBalance.primaryAccountValue.formatAsCurrency(
                        symbol = selectedCurrencySymbol,
                        isCompact = true,
                        isFiat = !isPrimaryCurrencyAlgo
                    ),
                    accountSecondaryValueText = accountBalance.secondaryAccountValue.formatAsCurrency(
                        symbol = secondaryCurrencySymbol,
                        isCompact = true,
                        isFiat = !isSecondaryCurrencyAlgo
                    ),
                    accountPrimaryValue = accountBalance.primaryAccountValue,
                    accountSecondaryValue = accountBalance.secondaryAccountValue
                )
            }, onFailedAccountConfiguration = {
                this?.run {
                    accountItemConfigurationMapper.mapTo(
                        accountAddress = address,
                        accountName = name,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type),
                        showWarningIcon = true
                    )
                }
            }
        )
        val baseAccountList = sortedAccountListItems.map { accountListItem ->
            if (accountListItem.itemConfiguration.showWarning == true) {
                accountListItemMapper.mapToErrorAccountItem(
                    accountListItem = accountListItem,
                    canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                )
            } else {
                accountListItemMapper.mapToAccountItem(
                    accountListItem = accountListItem,
                    canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                )
            }
        }
        return mutableListOf<BaseAccountListItem>().apply {
            if (baseAccountList.isNotEmpty()) {
                add(HeaderItem(R.string.accounts))
                addAll(baseAccountList)
            }
        }
    }

    private fun getPortfolioValueSuccessItem(
        primaryAccountValue: BigDecimal,
        secondaryAccountValue: BigDecimal
    ): BasePortfolioValueItem.SuccessPortfolioValueItem {
        val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrName()
        val secondaryCurrencySymbol = parityUseCase.getSecondaryCurrencySymbol()
        return portfolioValueItemMapper.mapToPortfolioValuesSuccessItem(
            formattedPrimaryAccountValue = primaryAccountValue.formatAsCurrency(selectedCurrencySymbol),
            formattedSecondaryAccountValue = secondaryAccountValue.formatAsCurrency(secondaryCurrencySymbol)
        )
    }

    private fun getPortfolioValuePartialErrorItem(
        primaryAccountValue: BigDecimal,
        secondaryAccountValue: BigDecimal
    ): BasePortfolioValueItem.PartialErrorPortfolioValueItem {
        val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrName()
        val secondaryCurrencySymbol = parityUseCase.getSecondaryCurrencySymbol()
        return portfolioValueItemMapper.mapToPortfolioValuesPartialErrorItem(
            formattedPrimaryAccountValue = primaryAccountValue.formatAsCurrency(selectedCurrencySymbol),
            formattedSecondaryAccountValue = secondaryAccountValue.formatAsCurrency(secondaryCurrencySymbol)
        )
    }

    private suspend fun createAccountErrorItemList(): List<BaseAccountListItem> {
        val sortedAccountListItems = getSortedAccountsByPreferenceUseCase.getSortedAccountListItems(
            sortingPreferences = accountSortPreferenceUseCase.getAccountSortPreference(),
            onLoadedAccountConfiguration = {
                accountItemConfigurationMapper.mapTo(
                    accountAddress = account.address,
                    accountName = account.name,
                    accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                    accountType = account.type,
                    showWarningIcon = true
                )
            },
            onFailedAccountConfiguration = {
                this?.run {
                    accountItemConfigurationMapper.mapTo(
                        accountAddress = address,
                        accountName = name,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type),
                        accountType = type,
                        showWarningIcon = true
                    )
                }
            }
        )
        if (sortedAccountListItems.isEmpty()) return emptyList()
        return mutableListOf<BaseAccountListItem>().apply {
            add(HeaderItem(R.string.accounts))
            val baseAccountList = sortedAccountListItems.map { accountListItem ->
                if (accountListItem.itemConfiguration.showWarning == true) {
                    accountListItemMapper.mapToErrorAccountItem(
                        accountListItem = accountListItem,
                        canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                    )
                } else {
                    accountListItemMapper.mapToAccountItem(
                        accountListItem = accountListItem,
                        canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                    )
                }
            }
            addAll(baseAccountList)
            insertQuickActionsItem(this)
        }
    }

    companion object {
        private const val QUICK_ACTIONS_ITEM_INDEX = 0
        private const val BANNER_ITEM_INDEX = 1
    }
}
