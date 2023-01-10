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

package network.voi.hera.ui.common.walletconnect

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import network.voi.hera.R
import network.voi.hera.databinding.CustomWalletConnectAmountInfoCardBinding
import network.voi.hera.models.BaseWalletConnectDisplayedAddress
import network.voi.hera.models.TransactionRequestAmountInfo
import network.voi.hera.utils.ALGO_SHORT_NAME
import network.voi.hera.utils.ALGO_DECIMALS
import network.voi.hera.utils.extensions.show
import network.voi.hera.utils.formatAmount
import network.voi.hera.utils.viewbinding.viewBinding
import java.math.BigInteger

class WalletConnectAmountInfoCardView(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = viewBinding(CustomWalletConnectAmountInfoCardBinding::inflate)
    private var listener: WalletConnectAmountInfoCardListener? = null

    init {
        initRootLayout()
    }

    fun setListener(listener: WalletConnectAmountInfoCardListener) {
        this.listener = listener
    }

    fun initAmountInfo(amountInfo: TransactionRequestAmountInfo?) {
        if (amountInfo == null) return
        with(amountInfo) {
            initAmount(amount, assetDecimal, assetShortName)
            initFee(fee, shouldShowFeeWarning)
            initDecimalPlaces(decimalPlaces)
            initDefaultFrozen(defaultFrozen)
            initManagerAccount(managerAccount)
            initReserveAccount(reserveAccount)
            initFreezeAccount(freezeAccount)
            initClawbackAccount(clawbackAccount)
        }
    }

    private fun initAmount(amount: BigInteger?, decimal: Int?, assetShortName: String?) {
        amount?.let {
            with(binding) {
                // TODO Move this into UseCase. Formatted data should come from domain layer
                val formattedAmount = amount.formatAmount(decimal ?: ALGO_DECIMALS)
                amountTextView.text =
                    context.getString(R.string.pair_value_format, formattedAmount, assetShortName.orEmpty())
                amountGroup.show()
            }
        }
    }

    private fun initFee(fee: Long?, shouldShowFeeWarning: Boolean) {
        fee?.let {
            with(binding) {
                val formattedAmount = fee.formatAmount(ALGO_DECIMALS)
                feeTextView.text = context.getString(R.string.pair_value_format, formattedAmount, ALGO_SHORT_NAME)
                feeWarningTextView.isVisible = shouldShowFeeWarning
                feeGroup.show()
            }
        }
    }

    private fun initDecimalPlaces(decimals: Long?) {
        decimals?.let {
            with(binding) {
                decimalPlacesTextView.text = decimals.toString()
                decimalPlacesGroup.show()
            }
        }
    }

    private fun initDefaultFrozen(frozen: Boolean?) {
        frozen?.let {
            with(binding) {
                val frozenTextRes = if (frozen) R.string.on else R.string.off
                defaultFrozenTextView.setText(frozenTextRes)
                defaultFrozenGroup.show()
            }
        }
    }

    private fun initReserveAccount(reserveAddress: BaseWalletConnectDisplayedAddress?) {
        if (!reserveAddress?.displayValue.isNullOrBlank()) {
            with(binding) {
                reserveAccountTextView.apply {
                    text = reserveAddress?.displayValue
                    isSingleLine = reserveAddress?.isSingleLine == true
                    if (reserveAddress?.fullAddress != null) {
                        setOnLongClickListener {
                            listener?.onAccountAddressLongPressed(reserveAddress.fullAddress)
                            return@setOnLongClickListener true
                        }
                    }
                }
                reserveAccountGroup.show()
            }
        }
    }

    private fun initClawbackAccount(clawbackAddress: BaseWalletConnectDisplayedAddress?) {
        if (!clawbackAddress?.displayValue.isNullOrBlank()) {
            with(binding) {
                clawbackAccountTextView.apply {
                    text = clawbackAddress?.displayValue
                    isSingleLine = clawbackAddress?.isSingleLine == true
                    if (clawbackAddress?.fullAddress != null) {
                        setOnLongClickListener {
                            listener?.onAccountAddressLongPressed(clawbackAddress.fullAddress)
                            return@setOnLongClickListener true
                        }
                    }
                }
                clawbackAccountGroup.show()
            }
        }
    }

    private fun initFreezeAccount(frozenAddress: BaseWalletConnectDisplayedAddress?) {
        if (!frozenAddress?.displayValue.isNullOrBlank()) {
            with(binding) {
                freezeAccountTextView.apply {
                    text = frozenAddress?.displayValue
                    isSingleLine = frozenAddress?.isSingleLine == true
                    if (frozenAddress?.fullAddress != null) {
                        setOnLongClickListener {
                            listener?.onAccountAddressLongPressed(frozenAddress.fullAddress)
                            return@setOnLongClickListener true
                        }
                    }
                }
                freezeAccountGroup.show()
            }
        }
    }

    private fun initManagerAccount(managerAddress: BaseWalletConnectDisplayedAddress?) {
        if (!managerAddress?.displayValue.isNullOrBlank()) {
            with(binding) {
                managerAccountNameTextView.apply {
                    text = managerAddress?.displayValue
                    isSingleLine = managerAddress?.isSingleLine == true
                    if (managerAddress?.fullAddress != null) {
                        setOnLongClickListener {
                            listener?.onAccountAddressLongPressed(managerAddress.fullAddress)
                            return@setOnLongClickListener true
                        }
                    }
                }
                managerAccountGroup.show()
            }
        }
    }

    private fun initRootLayout() {
        setPadding(resources.getDimensionPixelSize(R.dimen.spacing_large))
    }

    fun interface WalletConnectAmountInfoCardListener {
        fun onAccountAddressLongPressed(accountAddress: String)
    }
}
