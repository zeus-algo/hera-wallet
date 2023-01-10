package network.voi.hera.ui.register.registerintro

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import network.voi.hera.LoginNavigationDirections
import network.voi.hera.MainActivity
import network.voi.hera.R
import network.voi.hera.core.DaggerBaseFragment
import network.voi.hera.databinding.FragmentRegisterTypeSelectionBinding
import network.voi.hera.models.AnnotatedString
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.RegisterIntroPreview
import network.voi.hera.models.StatusBarConfiguration
import network.voi.hera.models.TextButton
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.utils.browser.openPrivacyPolicyUrl
import network.voi.hera.utils.browser.openTermsAndServicesUrl
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.getCustomClickableSpan
import network.voi.hera.utils.getXmlStyledString
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull

// TODO: 16.02.2022 login_navigation graph should be separated into multiple graphs
@AndroidEntryPoint
class RegisterIntroFragment : DaggerBaseFragment(R.layout.fragment_register_type_selection) {

    private val registerIntroViewModel: RegisterIntroViewModel by viewModels()

    private val binding by viewBinding(FragmentRegisterTypeSelectionBinding::bind)

    private val statusBarConfiguration = StatusBarConfiguration(backgroundColor = R.color.tertiary_background)

    private val toolbarConfiguration = ToolbarConfiguration(backgroundColor = R.color.primary_background)

    override val fragmentConfiguration = FragmentConfiguration(
        toolbarConfiguration = toolbarConfiguration,
        statusBarConfiguration = statusBarConfiguration
    )

    private val registerIntroPreviewCollector: suspend (RegisterIntroPreview) -> Unit = {
        binding.titleTextView.setText(it.titleRes)
        configureToolbar(it.isCloseButtonVisible, it.isSkipButtonVisible)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.isAppUnlocked = true
        initUi()
        initObservers()
    }

    private fun initUi() {
        with(binding) {
            createAccountSelectionItem.setOnClickListener { navToAddAccountTypeSelectionFragment() }
            recoveryAccountSelectionItem.setOnClickListener { navToAccountRecoveryTypeSelectionFragment() }
        }
        setupPolicyText()
    }

    fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            registerIntroViewModel.registerIntroPreviewFlow.filterNotNull(),
            registerIntroPreviewCollector
        )
    }

    private fun navToAddAccountTypeSelectionFragment() {
        registerIntroViewModel.logOnboardingWelcomeAccountCreateClickEvent()
        nav(RegisterIntroFragmentDirections.actionRegisterIntroFragmentToAddAccountTypeSelectionFragment())
    }

    private fun navToAccountRecoveryTypeSelectionFragment() {
        registerIntroViewModel.logOnboardingWelcomeAccountRecoverClickEvent()
        nav(RegisterIntroFragmentDirections.actionRegisterIntroFragmentToAccountRecoveryTypeSelectionFragment())
    }

    private fun configureToolbar(isCloseButtonVisible: Boolean, isSkipButtonVisible: Boolean) {
        getAppToolbar()?.let { toolbar ->
            if (isCloseButtonVisible) {
                toolbar.configureStartButton(R.drawable.ic_close, ::navBack)
            }
            if (isSkipButtonVisible) {
                toolbar.setEndButton(button = TextButton(R.string.skip, onClick = ::onSkipClick))
            }
        }
    }

    private fun setupPolicyText() {
        binding.policyTextView.apply {
            val linkTextColor = ContextCompat.getColor(context, R.color.link_primary)
            val termAndConditionsString = AnnotatedString(
                stringResId = R.string.by_creating_account,
                customAnnotationList = listOf(
                    "terms_click" to getCustomClickableSpan(linkTextColor) { context?.openTermsAndServicesUrl() },
                    "privacy_click" to getCustomClickableSpan(linkTextColor) { context?.openPrivacyPolicyUrl() }
                )
            )
            highlightColor = ContextCompat.getColor(context, R.color.transparent)
            movementMethod = LinkMovementMethod.getInstance()
            text = context.getXmlStyledString(termAndConditionsString)
        }
    }

    private fun onSkipClick() {
        registerIntroViewModel.logOnboardingCreateAccountSkipClickEvent()
        registerIntroViewModel.setRegisterSkip()
        nav(LoginNavigationDirections.actionGlobalToHomeNavigation())
    }
}
