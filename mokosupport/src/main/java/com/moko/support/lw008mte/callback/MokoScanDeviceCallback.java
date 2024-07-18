package com.moko.support.lw008mte.callback;

import com.moko.support.lw008mte.entity.DeviceInfo;

public interface MokoScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
