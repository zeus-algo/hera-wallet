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

package network.voi.hera.modules.webexport.accountconfirmation.ui.usecase

import network.voi.hera.R
import network.voi.hera.customviews.accountasseticonnameitem.mapper.AccountAssetIconNameConfigurationMapper
import network.voi.hera.models.AccountDetail
import network.voi.hera.modules.webexport.accountconfirmation.domain.model.ExportBackupResponseDTO
import network.voi.hera.modules.webexport.accountconfirmation.domain.usecase.WebExportAccountEncryptionUseCase
import network.voi.hera.modules.webexport.accountconfirmation.ui.mapper.BaseAccountConfirmationListItemMapper
import network.voi.hera.modules.webexport.accountconfirmation.ui.mapper.WebExportAccountConfirmationPreviewMapper
import network.voi.hera.modules.webexport.accountconfirmation.ui.model.BaseAccountConfirmationListItem
import network.voi.hera.modules.webexport.accountconfirmation.ui.model.WebExportAccountConfirmationPreview
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.utils.DataResource
import network.voi.hera.utils.Event
import network.voi.hera.utils.sendErrorLog
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WebExportAccountConfirmationPreviewUseCase @Inject constructor(
    private val webExportAccountConfirmationPreviewMapper: WebExportAccountConfirmationPreviewMapper,
    private val baseAccountConfirmationListItemMapper: BaseAccountConfirmationListItemMapper,
    private val accountAssetIconNameConfigurationMapper: AccountAssetIconNameConfigurationMapper,
    private val webExportAccountEncryptionUseCase: WebExportAccountEncryptionUseCase,
    private val accountDetailUseCase: AccountDetailUseCase
) {

    fun getInitialPreview(
        backupId: String,
        modificationKey: String,
        encryptionKey: String,
        accountAddresses: List<String>
    ): WebExportAccountConfirmationPreview {
        val listItems = mutableListOf<BaseAccountConfirmationListItem>().apply {
            add(getTitleItem())
            add(getDescriptionItem())
            accountAddresses.mapNotNull { key ->
                accountDetailUseCase.getCachedAccountDetail(key)
            }.forEach { cacheResult ->
                cacheResult.data?.let { accountDetail ->
                    add(getAccountItem(accountDetail = accountDetail))
                }
            }
        }
        return webExportAccountConfirmationPreviewMapper.mapTo(
            listItems = listItems,
            backupId = backupId,
            modificationKey = modificationKey,
            encryptionKey = encryptionKey,
            isLoadingVisible = false
        )
    }

    fun exportEncryptedBackup(
        previousState: WebExportAccountConfirmationPreview,
        backupId: String,
        modificationKey: String,
        encryptionKey: String,
        accountAddresses: List<String>
    ) = flow {
        emit(previousState.copy(isLoadingVisible = true))
        webExportAccountEncryptionUseCase.exportEncryptedBackup(
            backupId = backupId,
            modificationKey = modificationKey,
            encryptionKey = encryptionKey,
            accountAddresses = accountAddresses
        ).collect {
            when (it) {
                is DataResource.Success -> emit(getSuccessStateOfExportRequest(previousState, it.data))
                is DataResource.Error -> emit(getErrorStateOfExportRequest(previousState, it.exception))
                else -> {
                    sendErrorLog(
                        "Unhandled else case in" +
                        " WebExportAccountConfirmationPreviewUseCase.exportEncryptedBackup"
                    )
                }
            }
        }
    }

    private fun getTitleItem(): BaseAccountConfirmationListItem.TextItem {
        return baseAccountConfirmationListItemMapper.mapToTextItem(
            textResId = R.string.confirm_accounts,
            textAppearanceResId = R.style.TextAppearance_Title_Sans_Medium,
            topMarginResId = R.dimen.spacing_small
        )
    }

    private fun getDescriptionItem(): BaseAccountConfirmationListItem.TextItem {
        return baseAccountConfirmationListItemMapper.mapToTextItem(
            textResId = R.string.you_re_about_to_export,
            textAppearanceResId = R.style.TextAppearance_Footnote_Description,
            bottomMarginResId = R.dimen.spacing_large,
            topMarginResId = R.dimen.spacing_large
        )
    }

    private fun getAccountItem(
        accountDetail: AccountDetail
    ): BaseAccountConfirmationListItem.AccountItem {
        return baseAccountConfirmationListItemMapper.mapToAccountItem(
            accountAssetIconNameConfiguration = accountAssetIconNameConfigurationMapper.mapTo(
                accountDetail
            ),
            topMarginResId = R.dimen.spacing_xxsmall,
            accountAddress = accountDetail.account.address
        )
    }

    private fun getSuccessStateOfExportRequest(
        previewState: WebExportAccountConfirmationPreview,
        data: ExportBackupResponseDTO
    ): WebExportAccountConfirmationPreview {
        return previewState.copy(
            isLoadingVisible = false,
            requestSendSuccessEvent = Event(data)
        )
    }

    private fun getErrorStateOfExportRequest(
        previewState: WebExportAccountConfirmationPreview,
        exception: Throwable?
    ): WebExportAccountConfirmationPreview {
        return previewState.copy(
            isLoadingVisible = false,
            globalErrorEvent = if (exception?.message.isNullOrBlank()) null else Event(exception?.message.orEmpty())
        )
    }
}
