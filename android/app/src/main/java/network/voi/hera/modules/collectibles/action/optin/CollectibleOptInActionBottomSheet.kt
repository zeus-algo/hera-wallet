package network.voi.hera.modules.collectibles.action.optin

import android.widget.TextView
import androidx.fragment.app.viewModels
import network.voi.hera.MainActivity
import network.voi.hera.R
import network.voi.hera.customviews.CustomToolbar
import network.voi.hera.models.AccountIconResource
import network.voi.hera.models.AssetActionResult
import network.voi.hera.models.ToolbarConfiguration
import network.voi.hera.modules.assets.action.base.BaseAssetActionBottomSheet
import network.voi.hera.utils.AccountIconDrawable
import network.voi.hera.utils.extensions.show
import network.voi.hera.utils.setDrawable
import network.voi.hera.utils.setFragmentNavigationResult
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectibleOptInActionBottomSheet : BaseAssetActionBottomSheet() {

    private val toolbarConfiguration = ToolbarConfiguration(titleResId = R.string.opt_in_to_nft)

    override val assetActionViewModel: CollectibleOptInActionViewModel by viewModels()

    override fun initUi() {
        with(binding) {
            transactionFeeGroup.show()
            accountGroup.show()
        }
    }

    override fun setDescriptionTextView(textView: TextView) {
        textView.setText(R.string.opting_in_to_an_nft)
    }

    override fun setToolbar(customToolbar: CustomToolbar) {
        customToolbar.configure(toolbarConfiguration)
    }

    override fun setPositiveButton(materialButton: MaterialButton) {
        materialButton.apply {
            setText(R.string.approve)
            setOnClickListener {
                asset?.let { assetDescription ->
                    val assetActionResult = AssetActionResult(
                        asset = assetDescription,
                        publicKey = assetActionViewModel.accountAddress
                    )
                    (activity as? MainActivity)?.signAddAssetTransaction(assetActionResult)
                    setFragmentNavigationResult(OPT_IN_COLLECTIBLE_ACTION_RESULT_KEY, true)
                }
                navBack()
            }
        }
    }

    override fun setNegativeButton(materialButton: MaterialButton) {
        materialButton.apply {
            setText(R.string.close)
            setOnClickListener { navBack() }
        }
    }

    override fun setTransactionFeeTextView(textView: TextView) {
        textView.text = assetActionViewModel.getTransactionFee()
    }

    override fun setAccountNameTextView(textView: TextView) {
        textView.apply {
            with(assetActionViewModel.getAccountName()) {
                text = getDisplayAddress()
                setDrawable(
                    start = AccountIconDrawable.create(
                        context = context,
                        accountIconResource = accountIconResource ?: AccountIconResource.DEFAULT_ACCOUNT_ICON_RESOURCE,
                        size = resources.getDimension(R.dimen.account_icon_size_normal).toInt()
                    )
                )
                setOnLongClickListener { onAccountAddressCopied(publicKey); true }
            }
        }
    }

    companion object {
        const val OPT_IN_COLLECTIBLE_ACTION_RESULT_KEY = "opt_in_collectible_action_result_key"
    }
}
