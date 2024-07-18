package com.moko.support.lw008mte.service;

import com.moko.support.lw008mte.entity.DeviceInfo;

public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
