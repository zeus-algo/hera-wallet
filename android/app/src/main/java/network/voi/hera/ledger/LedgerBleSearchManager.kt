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

package network.voi.hera.ledger

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import network.voi.hera.R
import network.voi.hera.ledger.LedgerBleConnectionManager.Companion.SERVICE_UUID
import network.voi.hera.utils.areBluetoothPermissionsGranted
import network.voi.hera.utils.sendErrorLog

class LedgerBleSearchManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager?,
    private val ledgerBleConnectionManager: LedgerBleConnectionManager
) {

    private var isScanning = false
    private var scanCallback: CustomScanCallback? = null
    private var connectionTimeoutHandler: Handler? = null

    @SuppressLint("MissingPermission")
    fun scan(
        newScanCallback: CustomScanCallback,
        currentTransactionIndex: Int? = null,
        totalTransactionCount: Int? = null,
        filteredAddress: String? = null
    ) {
        if (filteredAddress != null &&
            isLedgerConnected(
                deviceAddress = filteredAddress,
                currentTransactionIndex = currentTransactionIndex,
                totalTransactionCount = totalTransactionCount
            )
        ) {
            // Don't need to scan any ledger devices because it has already connected.
            return
        }

        if (isScanning) return
        isScanning = true
        this.scanCallback = newScanCallback.apply {
            this.filteredAddress = filteredAddress
            this.currentTransactionIndex = currentTransactionIndex
            this.totalTransactionCount = totalTransactionCount
        }

        val scanFilters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
        )

        val scanSettings = ScanSettings.Builder().build()

        if (context.areBluetoothPermissionsGranted()) {
            bluetoothManager?.adapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
            if (filteredAddress != null) {
                startTimeout()
            } else {
                provideConnectedLedger(
                currentTransactionIndex = currentTransactionIndex,
                totalTransactionCount = totalTransactionCount
            )
            }
        } else {
            return
        }
    }

    private fun startTimeout() {
        connectionTimeoutHandler = Handler()
        connectionTimeoutHandler?.postDelayed({
            scanCallback?.onScanError(R.string.error_connection_message, R.string.error_connection_title)
            stop()
        }, MAX_SCAN_DURATION)
    }

    private fun provideConnectedLedger(currentTransactionIndex: Int?, totalTransactionCount: Int?) {
        val connectedDevice = ledgerBleConnectionManager.bluetoothDevice
        if (connectedDevice != null) {
            scanCallback?.onLedgerScanned(
                device = connectedDevice,
                currentTransactionIndex = currentTransactionIndex,
                totalTransactionCount = totalTransactionCount
            )
        }
    }

    private fun isLedgerConnected(
        deviceAddress: String,
        currentTransactionIndex: Int?,
        totalTransactionCount: Int?
    ): Boolean {
        val connectedDevice = ledgerBleConnectionManager.bluetoothDevice
        if (connectedDevice != null && connectedDevice.address == deviceAddress) {
            scanCallback?.onLedgerScanned(
                device = connectedDevice,
                currentTransactionIndex = currentTransactionIndex,
                totalTransactionCount = totalTransactionCount
            )
            return true
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        connectionTimeoutHandler?.removeCallbacksAndMessages(null)
        scanCallback?.run {
            if (context.areBluetoothPermissionsGranted()) {
                bluetoothManager?.adapter?.bluetoothLeScanner?.stopScan(this)
            } else {
                sendErrorLog("Bluetooth permission revoked before scanning could stop.")
                return
            }
        }
        isScanning = false
    }

    companion object {
        private const val MAX_SCAN_DURATION = 15000L
    }
}
