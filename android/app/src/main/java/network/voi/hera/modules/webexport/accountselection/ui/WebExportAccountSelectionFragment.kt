package network.voi.hera.modules.webexport.accountselection.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import network.voi.hera.R
import network.voi.hera.WebExportNavigationDirections
import network.voi.hera.core.BaseFragment
import network.voi.hera.customviews.TriStatesCheckBox
import network.voi.hera.databinding.FragmentWebExportAccountSelectionBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.webexport.accountselection.ui.adapter.WebExportAccountSelectionAdapter
import network.voi.hera.modules.webexport.accountselection.ui.model.WebExportAccountSelectionPreview
import network.voi.hera.utils.extensions.collectLatestOnLifecycle
import network.voi.hera.utils.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebExportAccountSelectionFragment : BaseFragment(R.layout.fragment_web_export_account_selection) {

    val toolbarConfiguration = ToolbarConfiguration(
        startIconResId = R.drawable.ic_left_arrow,
        startIconClick = ::handleNavBack
    )

    override val fragmentConfiguration = FragmentConfiguration(toolbarConfiguration = toolbarConfiguration)

    val binding by viewBinding(FragmentWebExportAccountSelectionBinding::bind)

    private val webExportAccountSelectionAdapterListener = object : WebExportAccountSelectionAdapter.Listener {
        override fun onCheckBoxClicked(currentCheckBoxState: TriStatesCheckBox.CheckBoxState) {
            webExportAccountSelectionViewModel.updatePreviewWithCheckBoxClickEvent(currentCheckBoxState)
        }

        override fun onAccountItemClicked(accountAddress: String) {
            webExportAccountSelectionViewModel.updatePreviewWithAccountClicked(accountAddress)
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleNavBack()
        }
    }

    private val webExportAccountSelectionViewModel: WebExportAccountSelectionViewModel by viewModels()
    private val webExportAccountSelectionAdapter =
        WebExportAccountSelectionAdapter(webExportAccountSelectionAdapterListener)

    private val webExportAccountSelectionPreviewCollector:
        suspend (preview: WebExportAccountSelectionPreview) -> Unit = { updateUiWithPreview(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        initUi()
        initObservers()
    }

    private fun handleNavBack() {
        nav(WebExportNavigationDirections.actionWebExportNavigationPop())
    }

    private fun initUi() {
        with(binding) {
            accountSelectionRecyclerview.adapter = webExportAccountSelectionAdapter
            continueButton.setOnClickListener { onContinueButtonClicked() }
            closeButton.setOnClickListener {
                handleNavBack()
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.collectLatestOnLifecycle(
            flow = webExportAccountSelectionViewModel.webExportAccountSelectionPreviewFlow,
            collection = webExportAccountSelectionPreviewCollector
        )
    }

    private fun updateUiWithPreview(preview: WebExportAccountSelectionPreview) {
        binding.continueButton.isEnabled = preview.isContinueButtonEnabled
        webExportAccountSelectionAdapter.submitList(preview.listItems)
        binding.successStateGroup.isVisible = preview.isEmptyStateVisible.not() && preview.isLoadingStateVisible.not()
        binding.emptyStateGroup.isVisible = preview.isEmptyStateVisible
    }

    private fun onContinueButtonClicked() {
        val selectedAccountAddressList = webExportAccountSelectionViewModel.getAllSelectedAccountAddressList()
        val qrCodeData = webExportAccountSelectionViewModel.getQRCodeData()
        nav(
                WebExportAccountSelectionFragmentDirections
                    .actionWebExportAccountSelectionFragmentToWebExportDomainNameConfirmationFragment(
                backupId = qrCodeData.backupId,
                encryptionKey = qrCodeData.encryptionKey,
                modificationKey = qrCodeData.modificationKey,
                accountList = selectedAccountAddressList.toTypedArray()
            )
        )
    }
}
