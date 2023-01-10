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

package network.voi.hera.modules.assets.action.base

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.buildSpannedString
import androidx.lifecycle.Observer
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.customviews.CustomToolbar
import network.voi.hera.databinding.BottomSheetAssetActionBinding
import network.voi.hera.models.AssetInformation
import network.voi.hera.utils.Resource
import network.voi.hera.utils.addUnnamedAssetName
import network.voi.hera.utils.copyToClipboard
import network.voi.hera.utils.setAssetNameTextColorByVerificationTier
import network.voi.hera.utils.setDrawable
import network.voi.hera.utils.viewbinding.viewBinding
import com.google.android.material.button.MaterialButton

// TODO Refactor this class whenever have a time
abstract class BaseAssetActionBottomSheet : BaseBottomSheet(R.layout.bottom_sheet_asset_action) {

    protected val binding by viewBinding(BottomSheetAssetActionBinding::bind)

    abstract val assetActionViewModel: BaseAssetActionViewModel

    protected var asset: AssetInformation? = null

    // region Observers

    // TODO: Replace this with flow
    // TODO: We shouldn't use [Resource] in UI layer anymore
    private val assetDescriptionObserver = Observer<Resource<AssetInformation>> { resource ->
        resource.use(
            onSuccess = { assetDescription ->
                asset = assetDescription
                setAssetDetails(assetDescription)
            },
            onLoading = { binding.loadingProgressBar.visibility = View.VISIBLE },
            onLoadingFinished = { binding.loadingProgressBar.visibility = View.GONE },
            onFailed = { showErrorAndNavBack(it) }
        )
    }

    //endregion

    abstract fun setDescriptionTextView(textView: TextView)
    abstract fun setToolbar(customToolbar: CustomToolbar)
    abstract fun setPositiveButton(materialButton: MaterialButton)
    abstract fun setNegativeButton(materialButton: MaterialButton)

    open fun setTransactionFeeTextView(textView: TextView) {}
    open fun setWarningIconImageView(imageView: ImageView) {}
    open fun setAccountNameTextView(textView: TextView) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initUi()
        initObservers()
        with(binding) {
            setDescriptionTextView(descriptionTextView)
            setToolbar(customToolbar)
            setPositiveButton(positiveButton)
            setNegativeButton(negativeButton)
            setTransactionFeeTextView(transactionFeeTextView)
            setWarningIconImageView(warningIconImageView)
            setAccountNameTextView(accountTextView)
        }
    }

    open fun initUi() {}

    open fun initArgs() {}

    open fun initObservers() {
        assetActionViewModel.assetInformationLiveData.observe(viewLifecycleOwner, assetDescriptionObserver)
    }

    private fun setAssetDetails(asset: AssetInformation) {
        with(binding) {
            with(asset) {
                assetFullNameTextView.text = fullName
                assetShortNameTextView.apply {
                    text = if (shortName.isNullOrBlank()) {
                        buildSpannedString { context?.let { addUnnamedAssetName(it) } }
                    } else {
                        shortName
                    }
                    assetActionViewModel.getVerificationTierConfiguration(verificationTier).run {
                        setAssetNameTextColorByVerificationTier(this@run)
                        if (drawableResId != null) {
                            setDrawable(end = AppCompatResources.getDrawable(context, drawableResId))
                        }
                    }
                }
                assetIdTextView.text = assetId.toString()
                copyIDButton.setOnClickListener { onCopyClick() }
            }
        }
    }

    private fun onCopyClick() {
        context?.copyToClipboard(assetActionViewModel.assetId.toString(), ASSET_ID_COPY_LABEL)
    }

    private fun showErrorAndNavBack(error: Resource.Error) {
        context?.run {
            val errorMessage = error.parse(this).toString()
            showGlobalError(errorMessage = errorMessage)
            navBack()
        }
    }

    companion object {
        private const val ASSET_ID_COPY_LABEL = "asset_id_label"
    }
}
