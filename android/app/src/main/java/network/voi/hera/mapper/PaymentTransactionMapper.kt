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

package network.voi.hera.mapper

import network.voi.hera.models.AssetDetail
import network.voi.hera.models.AssetInformation.Companion.ALGO_ID
import network.voi.hera.models.BasePaymentTransaction
import network.voi.hera.models.BaseWalletConnectTransaction
import network.voi.hera.models.WCAlgoTransactionRequest
import network.voi.hera.models.WalletConnectAccount
import network.voi.hera.models.WalletConnectAssetInformation
import network.voi.hera.models.WalletConnectPeerMeta
import network.voi.hera.models.WalletConnectSigner
import network.voi.hera.models.WalletConnectTransactionRequest
import network.voi.hera.modules.currency.domain.usecase.CurrencyUseCase
import network.voi.hera.modules.parity.domain.usecase.ParityUseCase
import network.voi.hera.usecase.AccountDetailUseCase
import network.voi.hera.usecase.SimpleAssetDetailUseCase
import network.voi.hera.utils.toAlgoDisplayValue
import network.voi.hera.utils.walletconnect.WalletConnectTransactionErrorProvider
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

// TODO: 19.01.2022 Mappers shouldn't inject use case
@SuppressWarnings("ReturnCount")
class PaymentTransactionMapper @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val errorProvider: WalletConnectTransactionErrorProvider,
    private val parityUseCase: ParityUseCase,
    private val currencyUseCase: CurrencyUseCase,
    private val walletConnectAssetInformationMapper: WalletConnectAssetInformationMapper
) : BaseWalletConnectTransactionMapper() {

    override fun createTransaction(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTxn: WCAlgoTransactionRequest
    ): BaseWalletConnectTransaction? {
        return with(transactionRequest) {
            when {
                !rekeyAddress.isNullOrBlank() && !closeToAddress.isNullOrBlank() -> {
                    createPaymentTransactionWithCloseToAndRekey(peerMeta, transactionRequest, rawTxn)
                }
                !rekeyAddress.isNullOrBlank() -> {
                    createPaymentTransactionWithRekey(peerMeta, transactionRequest, rawTxn)
                }
                !closeToAddress.isNullOrBlank() -> {
                    createPaymentTransactionWithClose(peerMeta, transactionRequest, rawTxn)
                }
                else -> createPaymentTransaction(peerMeta, transactionRequest, rawTxn)
            }
        }
    }

    private fun createPaymentTransactionWithCloseToAndRekey(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTransaction: WCAlgoTransactionRequest
    ): BasePaymentTransaction.PaymentTransactionWithRekeyAndClose? {
        return with(transactionRequest) {
            val senderWCAddress = createWalletConnectAddress(senderAddress)
            val senderAccountData =
                accountDetailUseCase.getCachedAccountDetail(senderWCAddress?.decodedAddress.toString())?.data
            val amount = amount ?: BigInteger.ZERO
            val assetDetail = simpleAssetDetailUseCase.getCachedAssetDetail(ALGO_ID)?.data
            val walletConnectAssetInformation = createWalletConnectAssetInformation(assetDetail, amount)

            BasePaymentTransaction.PaymentTransactionWithRekeyAndClose(
                rawTransactionPayload = rawTransaction,
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                note = decodedNote,
                amount = amount,
                senderAddress = senderWCAddress ?: return null,
                receiverAddress = createWalletConnectAddress(receiverAddress) ?: return null,
                peerMeta = peerMeta,
                closeToAddress = createWalletConnectAddress(closeToAddress) ?: return null,
                rekeyToAddress = createWalletConnectAddress(rekeyAddress) ?: return null,
                signer = WalletConnectSigner.create(rawTransaction, senderWCAddress, errorProvider),
                authAddress = senderAccountData?.accountInformation?.rekeyAdminAddress,
                fromAccount = WalletConnectAccount.create(senderAccountData?.account),
                assetInformation = walletConnectAssetInformation,
                groupId = groupId
            )
        }
    }

    private fun createPaymentTransactionWithRekey(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTransaction: WCAlgoTransactionRequest
    ): BasePaymentTransaction.PaymentTransactionWithRekey? {
        return with(transactionRequest) {
            val senderWCAddress = createWalletConnectAddress(senderAddress)
            val senderAccountData =
                accountDetailUseCase.getCachedAccountDetail(senderWCAddress?.decodedAddress.toString())?.data
            val amount = amount ?: BigInteger.ZERO
            val assetDetail = simpleAssetDetailUseCase.getCachedAssetDetail(ALGO_ID)?.data
            val walletConnectAssetInformation = createWalletConnectAssetInformation(assetDetail, amount)

            BasePaymentTransaction.PaymentTransactionWithRekey(
                rawTransactionPayload = rawTransaction,
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                note = decodedNote,
                amount = amount,
                senderAddress = senderWCAddress ?: return null,
                receiverAddress = createWalletConnectAddress(receiverAddress) ?: return null,
                peerMeta = peerMeta,
                rekeyToAddress = createWalletConnectAddress(rekeyAddress) ?: return null,
                signer = WalletConnectSigner.create(rawTransaction, senderWCAddress, errorProvider),
                authAddress = senderAccountData?.accountInformation?.rekeyAdminAddress,
                fromAccount = WalletConnectAccount.create(senderAccountData?.account),
                assetInformation = walletConnectAssetInformation,
                groupId = groupId
            )
        }
    }

    private fun createPaymentTransactionWithClose(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTransaction: WCAlgoTransactionRequest
    ): BasePaymentTransaction.PaymentTransactionWithClose? {
        return with(transactionRequest) {
            val senderWCAddress = createWalletConnectAddress(senderAddress)
            val senderAccountData =
                accountDetailUseCase.getCachedAccountDetail(senderWCAddress?.decodedAddress.toString())?.data
            val amount = amount ?: BigInteger.ZERO
            val assetDetail = simpleAssetDetailUseCase.getCachedAssetDetail(ALGO_ID)?.data
            val walletConnectAssetInformation = createWalletConnectAssetInformation(assetDetail, amount)

            BasePaymentTransaction.PaymentTransactionWithClose(
                rawTransactionPayload = rawTransaction,
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                note = decodedNote,
                amount = amount,
                senderAddress = senderWCAddress ?: return null,
                receiverAddress = createWalletConnectAddress(receiverAddress) ?: return null,
                peerMeta = peerMeta,
                closeToAddress = createWalletConnectAddress(closeToAddress) ?: return null,
                signer = WalletConnectSigner.create(rawTransaction, senderWCAddress, errorProvider),
                authAddress = senderAccountData?.accountInformation?.rekeyAdminAddress,
                fromAccount = WalletConnectAccount.create(senderAccountData?.account),
                assetInformation = walletConnectAssetInformation,
                groupId = groupId
            )
        }
    }

    // TODO: 05.08.2022 After use BaseAccountAssetData instead of AssetInformation get rid of AccountCacheManager
    // TODO: 17.08.2022 Use SimpleAssetDetailUseCase instead
    private fun createPaymentTransaction(
        peerMeta: WalletConnectPeerMeta,
        transactionRequest: WalletConnectTransactionRequest,
        rawTransaction: WCAlgoTransactionRequest
    ): BasePaymentTransaction.PaymentTransaction? {
        return with(transactionRequest) {
            val senderWCAddress = createWalletConnectAddress(senderAddress)
            val senderAccountData =
                accountDetailUseCase.getCachedAccountDetail(senderWCAddress?.decodedAddress.toString())?.data
            val receiverWCAddress = createWalletConnectAddress(receiverAddress)
            val receiverAccountData =
                accountDetailUseCase.getCachedAccountDetail(receiverWCAddress?.decodedAddress.toString())?.data
            val amount = amount ?: BigInteger.ZERO
            val assetDetail = simpleAssetDetailUseCase.getCachedAssetDetail(ALGO_ID)?.data
            val walletConnectAssetInformation = createWalletConnectAssetInformation(assetDetail, amount)

            BasePaymentTransaction.PaymentTransaction(
                rawTransactionPayload = rawTransaction,
                walletConnectTransactionParams = createTransactionParams(transactionRequest),
                note = decodedNote,
                amount = amount,
                senderAddress = senderWCAddress ?: return null,
                receiverAddress = createWalletConnectAddress(receiverAddress) ?: return null,
                peerMeta = peerMeta,
                signer = WalletConnectSigner.create(rawTransaction, senderWCAddress, errorProvider),
                authAddress = senderAccountData?.accountInformation?.rekeyAdminAddress,
                fromAccount = WalletConnectAccount.create(senderAccountData?.account),
                toAccount = WalletConnectAccount.create(receiverAccountData?.account),
                assetInformation = walletConnectAssetInformation,
                groupId = groupId
            )
        }
    }

    // TODO: 05.08.2022 Refactor use BaseAccountAssetData instead of AssetInformation.
    private fun createWalletConnectAssetInformation(
        assetDetail: AssetDetail?,
        amount: BigInteger
    ): WalletConnectAssetInformation? {
        lateinit var algoPrice: BigDecimal
        lateinit var currencySymbol: String
        if (currencyUseCase.isPrimaryCurrencyAlgo()) {
            algoPrice = parityUseCase.getAlgoToUsdConversionRate()
            currencySymbol = parityUseCase.getSecondaryCurrencySymbol()
        } else {
            algoPrice = parityUseCase.getAlgoToPrimaryCurrencyConversionRate()
            currencySymbol = parityUseCase.getPrimaryCurrencySymbolOrName()
        }
        return walletConnectAssetInformationMapper.algorandMapToWalletConnectAssetInformation(
            assetDetail,
            amount.toAlgoDisplayValue().multiply(algoPrice),
            currencySymbol
        )
    }
}
