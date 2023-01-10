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

package network.voi.hera.discover.common.ui.model

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import network.voi.hera.BuildConfig
import network.voi.hera.utils.EMAIL_APPS_URI_SCHEME
import network.voi.hera.utils.walletconnect.isValidWalletConnectUrl
import java.net.HttpURLConnection

class PeraWebViewClient(val listener: PeraWebViewClientListener?) : WebViewClient() {
    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        listener?.onPageUrlChanged()
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        with(request.url.toString()) {
            return when {
                isValidWalletConnectUrl(this) -> {
                    listener?.onWalletConnectUrlDetected(this)
                    true
                }
                startsWith(EMAIL_APPS_URI_SCHEME) -> {
                    listener?.onEmailRequested(this)
                    true
                }
                startsWith(BuildConfig.DEEPLINK_PREFIX) -> {
                    true
                }
                request.isForMainFrame -> {
                    listener?.onPageRequested()
                    super.shouldOverrideUrlLoading(view, request)
                }
                else -> {
                    super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
    }
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        listener?.onPageFinished()
    }
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            listener?.onError()
        }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        if (request?.isForMainFrame == true && errorResponse?.statusCode != HttpURLConnection.HTTP_NOT_FOUND) {
            listener?.onHttpError()
        }
    }

    interface PeraWebViewClientListener {
        fun onWalletConnectUrlDetected(url: String)
        fun onEmailRequested(url: String)
        fun onPageRequested()
        fun onPageFinished()
        fun onError()
        fun onHttpError()
        fun onPageUrlChanged()
    }
}
