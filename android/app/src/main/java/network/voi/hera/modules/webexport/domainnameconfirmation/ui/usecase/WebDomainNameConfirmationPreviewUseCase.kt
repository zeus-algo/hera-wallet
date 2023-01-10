/*
 *  Copyright 2022 Pera Wallet, LDA
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package network.voi.hera.modules.webexport.domainnameconfirmation.ui.usecase

import network.voi.hera.modules.webexport.domainnameconfirmation.domain.usecase.WebExportDomainNameConfirmationUseCase
import network.voi.hera.modules.webexport.domainnameconfirmation.ui.mapper.WebExportDomainNameConfirmationPreviewMapper
import network.voi.hera.modules.webexport.domainnameconfirmation.ui.model.WebExportDomainNameConfirmationPreview
import network.voi.hera.usecase.SecurityUseCase
import network.voi.hera.utils.Event
import javax.inject.Inject

class WebDomainNameConfirmationPreviewUseCase @Inject constructor(
    private val securityUseCase: SecurityUseCase,
    private val webExportDomainNameConfirmationUseCase: WebExportDomainNameConfirmationUseCase,
    private val webExportDomainNameConfirmationPreviewMapper: WebExportDomainNameConfirmationPreviewMapper
) {

    fun getInitialPreview(
        backupId: String,
        modificationKey: String,
        encryptionKey: String,
        accountList: List<String>
    ): WebExportDomainNameConfirmationPreview {
        return webExportDomainNameConfirmationPreviewMapper.mapTo(
            backupId = backupId,
            modificationKey = modificationKey,
            encryptionKey = encryptionKey,
            accountList = accountList,
            isEnabled = false,
            navigateToShowAuthenticationEvent = null,
            navigateToAccountConfirmationEvent = null
        )
    }

    fun getUpdatedPreviewWithInputUrl(
        previousPreview: WebExportDomainNameConfirmationPreview,
        inputUrl: String
    ): WebExportDomainNameConfirmationPreview {
        return previousPreview.copy(
            isContinueButtonEnabled = webExportDomainNameConfirmationUseCase.isInpUtUrlFromValidDomain(inputUrl)
        )
    }

    fun getUpdatedPreviewWithClickDestination(
        previousPreview: WebExportDomainNameConfirmationPreview
    ): WebExportDomainNameConfirmationPreview {
        val pinEnabled = securityUseCase.isPinCodeEnabled()
        val navigateEvent = Event(Unit)
        return previousPreview.copy(
            navigateToShowAuthenticationEvent = navigateEvent.takeIf { pinEnabled },
            navigateToAccountConfirmationEvent = navigateEvent.takeIf { !pinEnabled }
        )
    }

    fun getUpdatedPreviewAfterPasscodeVerified(
        previousPreview: WebExportDomainNameConfirmationPreview
    ): WebExportDomainNameConfirmationPreview {
        return previousPreview.copy(
            navigateToAccountConfirmationEvent = Event(Unit)
        )
    }
}
