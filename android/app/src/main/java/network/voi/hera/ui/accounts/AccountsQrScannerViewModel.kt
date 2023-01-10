package network.voi.hera.ui.accounts

import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import network.voi.hera.core.BaseViewModel
import network.voi.hera.modules.tracking.accounts.AccountsEventTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import network.voi.hera.usecase.IsAccountLimitExceedUseCase
import kotlinx.coroutines.launch

@HiltViewModel
class AccountsQrScannerViewModel @Inject constructor(
    private val accountsEventTracker: AccountsEventTracker,
    private val isAccountLimitExceedUseCase: IsAccountLimitExceedUseCase
) : BaseViewModel() {

    fun logAccountsQrConnectEvent() {
        viewModelScope.launch {
            accountsEventTracker.logAccountsQrConnectEvent()
        }
    }

    fun isAccountLimitExceed(): Boolean {
        return isAccountLimitExceedUseCase.isAccountLimitExceed()
    }
}
