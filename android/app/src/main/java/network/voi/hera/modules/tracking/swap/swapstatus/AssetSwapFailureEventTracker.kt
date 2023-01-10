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

package network.voi.hera.modules.tracking.swap.swapstatus

import network.voi.hera.modules.swap.assetswap.domain.model.SwapQuote
import network.voi.hera.modules.tracking.core.BaseEventTracker
import network.voi.hera.modules.tracking.core.PeraEventTracker
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.INPUT_ASA_AMOUNT_AS_ALGO_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.INPUT_ASA_AMOUNT_AS_USD_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.INPUT_ASA_ID_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.INPUT_ASA_NAME_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.OUTPUT_ASA_AMOUNT_AS_ALGO_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.OUTPUT_ASA_AMOUNT_AS_USD_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.OUTPUT_ASA_ID_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.OUTPUT_ASA_NAME_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.SWAP_DATE_FORMATTED_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.SWAP_DATE_TIMESTAMP_PAYLOAD_KEY
import network.voi.hera.modules.tracking.swap.swapstatus.SwapConfirmedEventTrackerConstants.SWAP_WALLET_ADDRESS_PAYLOAD_KEY
import javax.inject.Inject

class AssetSwapFailureEventTracker @Inject constructor(
    peraEventTracker: PeraEventTracker
) : BaseEventTracker(peraEventTracker) {

    suspend fun logFailureSwapEvent(
        swapQuote: SwapQuote,
        inputAsaAmountAsAlgo: Double,
        inputAsaAmountAsUsd: Double,
        outputAsaAmountAsAlgo: Double,
        outputAsaAmountAsUsd: Double,
        swapDateTimestamp: Long,
        formattedSwapDateTime: String
    ) {
        val eventPayload = getEventPayload(
            swapQuote = swapQuote,
            inputAsaAmountAsAlgo = inputAsaAmountAsAlgo,
            inputAsaAmountAsUsd = inputAsaAmountAsUsd,
            outputAsaAmountAsAlgo = outputAsaAmountAsAlgo,
            outputAsaAmountAsUsd = outputAsaAmountAsUsd,
            swapDateTimestamp = swapDateTimestamp,
            formattedSwapDateTime = formattedSwapDateTime
        )
        logEvent(SWAP_FAILURE_EVENT_KEY, eventPayload)
    }

    companion object {
        private const val SWAP_FAILURE_EVENT_KEY = "swapscr_assets_failed"
    }

    private fun getEventPayload(
        swapQuote: SwapQuote,
        inputAsaAmountAsAlgo: Double,
        inputAsaAmountAsUsd: Double,
        outputAsaAmountAsAlgo: Double,
        outputAsaAmountAsUsd: Double,
        swapDateTimestamp: Long,
        formattedSwapDateTime: String
    ): Map<String, Any> {
        return mapOf(
            INPUT_ASA_AMOUNT_AS_USD_PAYLOAD_KEY to inputAsaAmountAsUsd,
            INPUT_ASA_AMOUNT_AS_ALGO_PAYLOAD_KEY to inputAsaAmountAsAlgo,
            INPUT_ASA_ID_PAYLOAD_KEY to swapQuote.fromAssetDetail.assetId,
            INPUT_ASA_NAME_PAYLOAD_KEY to swapQuote.fromAssetDetail.name.getName().orEmpty(),
            OUTPUT_ASA_AMOUNT_AS_USD_PAYLOAD_KEY to outputAsaAmountAsUsd,
            OUTPUT_ASA_AMOUNT_AS_ALGO_PAYLOAD_KEY to outputAsaAmountAsAlgo,
            OUTPUT_ASA_ID_PAYLOAD_KEY to swapQuote.toAssetDetail.assetId,
            OUTPUT_ASA_NAME_PAYLOAD_KEY to swapQuote.toAssetDetail.name.getName().orEmpty(),
            SWAP_DATE_TIMESTAMP_PAYLOAD_KEY to swapDateTimestamp,
            SWAP_DATE_FORMATTED_PAYLOAD_KEY to formattedSwapDateTime,
            SWAP_WALLET_ADDRESS_PAYLOAD_KEY to swapQuote.accountAddress
        )
    }
}
