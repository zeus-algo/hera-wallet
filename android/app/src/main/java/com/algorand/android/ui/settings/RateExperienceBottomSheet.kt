package com.algorand.android.ui.settings

import android.os.Bundle
import android.view.View
import network.voi.hera.R
import com.algorand.android.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetRateExperienceBinding
import com.algorand.android.utils.browser.openApplicationPageOnStore
import com.algorand.android.utils.viewbinding.viewBinding

class RateExperienceBottomSheet : BaseBottomSheet(
    layoutResId = R.layout.bottom_sheet_rate_experience
) {

    private val binding by viewBinding(BottomSheetRateExperienceBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.likeButton.setOnClickListener { onLikeClick() }
        binding.dislikeButton.setOnClickListener { onDislikeClick() }
    }

    private fun onLikeClick() {
        navBack()
        context?.openApplicationPageOnStore()
    }

    private fun onDislikeClick() {
        navBack()
    }
}
