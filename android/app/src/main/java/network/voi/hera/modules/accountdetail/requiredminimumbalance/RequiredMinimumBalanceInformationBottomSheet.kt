package network.voi.hera.modules.accountdetail.requiredminimumbalance

import network.voi.hera.R
import network.voi.hera.modules.informationbottomsheet.ui.BaseInformationBottomSheet

class RequiredMinimumBalanceInformationBottomSheet : BaseInformationBottomSheet() {

    override val titleTextResId: Int
        get() = R.string.minimum_balance

    override val descriptionTextResId: Int
        get() = R.string.minimum_balance_is_the_minimum

    override val neutralButtonTextResId: Int
        get() = R.string.close

    override fun onNeutralButtonClick() {
        navBack()
    }
}
