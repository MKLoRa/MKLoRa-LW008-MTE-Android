package com.moko.support.lw008mte.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.lw008mte.entity.OrderCHAR;
import com.moko.support.lw008mte.entity.ParamsKeyEnum;

import java.util.Arrays;

public class ParamsReadTask extends OrderTask {
    public byte[] data;

    public ParamsReadTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum key) {
        createGetConfigData(key.getParamsKey());
    }

    private void createGetConfigData(int configKey) {
        byte[] cmdBytes = MokoUtils.toByteArray(configKey, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x00,
                (byte) cmdBytes[0],
                (byte) cmdBytes[1],
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void getFilterName() {
        byte[] cmdBytes = MokoUtils.toByteArray(ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey(), 2);
        data = new byte[]{
                (byte) 0xEE,
                (byte) 0x00,
                (byte) cmdBytes[0],
                (byte) cmdBytes[1],
                (byte) 0x00
        };
        response.responseValue = data;
    }
}
