package network.voi.hera.ui.settings

import android.os.Bundle
import android.view.View
import network.voi.hera.R
import network.voi.hera.core.BaseBottomSheet
import network.voi.hera.databinding.BottomSheetRateExperienceBinding
import network.voi.hera.utils.browser.openApplicationPageOnStore
import network.voi.hera.utils.viewbinding.viewBinding

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
