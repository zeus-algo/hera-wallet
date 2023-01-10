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

package network.voi.hera.modules.perawebview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import network.voi.hera.core.BaseFragment
import network.voi.hera.customviews.PeraWebView
import network.voi.hera.discover.common.ui.model.PeraWebViewClient
import network.voi.hera.utils.sendMailRequestUrl

abstract class BasePeraWebViewFragment(
    @LayoutRes private val layoutResId: Int,
) : BaseFragment(layoutResId) {

    abstract val binding: ViewBinding

    abstract val basePeraWebViewViewModel: BasePeraWebViewViewModel

    protected val peraWebViewClientListener = object : PeraWebViewClient.PeraWebViewClientListener {
        override fun onWalletConnectUrlDetected(url: String) {
            handleWalletConnectUrl(url)
        }

        override fun onEmailRequested(url: String) {
            handleMailRequestUrl(url)
        }

        override fun onPageRequested() {
            basePeraWebViewViewModel.onPageRequested()
        }

        override fun onPageFinished() {
            basePeraWebViewViewModel.onPageFinished()
        }

        override fun onError() {
            basePeraWebViewViewModel.onError()
        }

        override fun onHttpError() {
            basePeraWebViewViewModel.onHttpError()
        }

        override fun onPageUrlChanged() {
            basePeraWebViewViewModel.onPageUrlChanged()
        }
    }

    open fun onSendMailRequestFailed() {}
    abstract fun bindWebView(view: View?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        basePeraWebViewViewModel.getWebView()?.let { previousWebView ->
            // If we have a previously saved WebView, it is reloaded, bound and theming set
            reloadWebView(view, previousWebView)
            bindWebView(view)
        } ?: bindWebView(view)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getWebView(binding.root)?.let { basePeraWebViewViewModel.saveWebView(it) }
    }

    protected fun handleMailRequestUrl(url: String) {
        context?.sendMailRequestUrl(url, ::onSendMailRequestFailed)
    }

    protected fun getWebView(parent: View): PeraWebView? {
        if (parent is ViewGroup) {
            for (cx in 0 until parent.childCount) {
                val child = parent.getChildAt(cx)
                if (child is PeraWebView) {
                    return child
                }
            }
        }
        return null
    }

    private fun reloadWebView(parent: View?, webView: PeraWebView) {
        if (parent is ViewGroup) {
            for (cx in 0 until parent.childCount) {
                val child = parent.getChildAt(cx)
                if (child is PeraWebView) {
                    val index = parent.indexOfChild(child)
                    parent.removeView(child)
                    (webView.parent as ViewGroup).removeView(webView)
                    parent.addView(webView, index)
                }
            }
        }
    }
}
