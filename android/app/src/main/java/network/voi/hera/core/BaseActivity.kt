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

package network.voi.hera.core

import com.akexorcist.localizationactivity.ui.LocalizationActivity
import network.voi.hera.customviews.TopToast

abstract class BaseActivity : LocalizationActivity() {

    protected val activityTag: String = this::class.simpleName.orEmpty()

    private var topToast: TopToast? = null

    fun showTopToast(title: String? = null, description: String? = null) {
        if (topToast == null) {
            topToast = TopToast(this)
        }
        topToast?.show(title, description)
    }

    override fun onStop() {
        topToast?.dismissAnimated()
        super.onStop()
    }
}
