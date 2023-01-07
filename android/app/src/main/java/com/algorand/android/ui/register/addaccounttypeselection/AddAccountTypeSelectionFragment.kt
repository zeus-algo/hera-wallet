package com.algorand.android.ui.register.addaccounttypeselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import network.voi.hera.LoginNavigationDirections
import network.voi.hera.R
import com.algorand.android.core.DaggerBaseFragment
import network.voi.hera.databinding.FragmentAddAccountTypeSelectionBinding
import com.algorand.android.models.FragmentConfiguration
import com.algorand.android.models.TextButton
import com.algorand.android.models.ToolbarConfiguration
import com.algorand.android.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddAccountTypeSelectionFragment : DaggerBaseFragment(R.layout.fragment_add_account_type_selection) {

    private val binding by viewBinding(FragmentAddAccountTypeSelectionBinding::bind)

    private val addAccountTypeSelectionViewModel: AddAccountTypeSelectionViewModel by viewModels()

    private val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::navBack,
        backgroundColor = R.color.primary_background
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        with(binding) {
            createNewAccountSelectionItem.setOnClickListener { navToBackupInfoFragment() }
            watchAccountSelectionItem.setOnClickListener { navToWatchAccountInfoFragment() }
        }
    }

    private fun navToBackupInfoFragment() {
        addAccountTypeSelectionViewModel.logOnboardingCreateNewAccountClickEvent()
        nav(AddAccountTypeSelectionFragmentDirections.actionAddAccountTypeSelectionFragmentToBackupInfoFragment())
    }

    private fun navToWatchAccountInfoFragment() {
        addAccountTypeSelectionViewModel.logOnboardingCreateWatchAccountClickEvent()
        nav(
            AddAccountTypeSelectionFragmentDirections
                .actionAddAccountTypeSelectionFragmentToWatchAccountInfoFragment()
        )
    }

    private fun setupToolbar() {
        if (addAccountTypeSelectionViewModel.hasAccount().not()) {
            getAppToolbar()?.setEndButton(button = TextButton(R.string.skip, onClick = ::onSkipClick))
        }
    }

    private fun onSkipClick() {
        addAccountTypeSelectionViewModel.setRegisterSkip()
        nav(LoginNavigationDirections.actionGlobalToHomeNavigation())
    }
}
