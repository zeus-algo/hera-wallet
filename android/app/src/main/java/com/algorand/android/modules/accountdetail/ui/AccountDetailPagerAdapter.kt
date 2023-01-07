/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 *
 */

package com.algorand.android.modules.accountdetail.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import network.voi.hera.R
import com.algorand.android.modules.accountdetail.assets.ui.AccountAssetsFragment
import com.algorand.android.modules.accountdetail.collectibles.ui.AccountCollectiblesFragment
import com.algorand.android.modules.accountdetail.history.ui.AccountHistoryFragment
import com.algorand.android.modules.accountdetail.ui.model.AccountDetailPagerAdapterItem

class AccountDetailPagerAdapter(
    fragment: Fragment,
    address: String
) : FragmentStateAdapter(fragment) {

    // TODO: 9.08.2022 Since all fragment instance references are kept inside the list, they may cause memory leak
    // TODO: 9.08.2022 Since this issue is not observed through Profiler or LeakCanary, I will leave this to investigate
    private val pagerItemList = listOf(
        AccountDetailPagerAdapterItem(
            fragmentInstance = AccountAssetsFragment.newInstance(address),
            titleResId = R.string.overview
        ),
        AccountDetailPagerAdapterItem(
            fragmentInstance = AccountCollectiblesFragment.newInstance(address),
            titleResId = R.string.nfts
        ),
        AccountDetailPagerAdapterItem(
            fragmentInstance = AccountHistoryFragment.newInstance(address),
            titleResId = R.string.history
        )
    )

    override fun getItemCount() = pagerItemList.size

    override fun createFragment(position: Int): Fragment {
        return pagerItemList.getOrNull(position)?.fragmentInstance ?: throw Exception("Unknown Account Detail Tab")
    }

    fun getItem(position: Int): AccountDetailPagerAdapterItem? {
        return pagerItemList.getOrNull(position)
    }
}
