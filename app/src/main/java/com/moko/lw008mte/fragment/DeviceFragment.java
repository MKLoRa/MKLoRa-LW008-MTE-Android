package com.moko.lw008mte.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.task.OrderTask;
import com.moko.lw008mte.R;
import com.moko.lw008mte.activity.DeviceInfoActivity;
import com.moko.lw008mte.databinding.Lw008MteFragmentDeviceBinding;
import com.moko.lw008mte.dialog.BottomDialog;
import com.moko.support.lw008mte.LoRaLW008MTEMokoSupport;
import com.moko.support.lw008mte.OrderTaskAssembler;

import java.util.ArrayList;

public class DeviceFragment extends Fragment {
    private static final String TAG = DeviceFragment.class.getSimpleName();
    private Lw008MteFragmentDeviceBinding mBind;

    private ArrayList<String> mTimeZones;
    private int mSelectedTimeZone;

    private final ArrayList<String> mLowPowerPrompts = new ArrayList<>(8);
    private int mSelectedLowPowerPrompt;
    private boolean mLowPowerPayloadEnable;


    private DeviceInfoActivity activity;

    public DeviceFragment() {
    }


    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw008MteFragmentDeviceBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        mTimeZones = new ArrayList<>();
        for (int i = -24; i <= 28; i++) {
            if (i < 0) {
                if (i % 2 == 0) {
                    mTimeZones.add(String.format("UTC%d", i / 2));
                } else {
                    mTimeZones.add(i < -1 ? String.format("UTC%d:30", (i + 1) / 2) : "UTC-0:30");
                }
            } else if (i == 0) {
                mTimeZones.add("UTC");
            } else {
                if (i % 2 == 0) {
                    mTimeZones.add(String.format("UTC+%d", i / 2));
                } else {
                    mTimeZones.add(String.format("UTC+%d:30", (i - 1) / 2));
                }
            }
        }

        mLowPowerPrompts.add("10%");
        mLowPowerPrompts.add("20%");
        mLowPowerPrompts.add("30%");
        mLowPowerPrompts.add("40%");
        mLowPowerPrompts.add("50%");
        mLowPowerPrompts.add("60%");
        return mBind.getRoot();
    }

    public void setTimeZone(int timeZone) {
        mSelectedTimeZone = timeZone + 24;
        mBind.tvTimeZone.setText(mTimeZones.get(mSelectedTimeZone));
    }

    public void showTimeZoneDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mTimeZones, mSelectedTimeZone);
        dialog.setListener(value -> {
            mSelectedTimeZone = value;
            mBind.tvTimeZone.setText(mTimeZones.get(value));
        });
        dialog.show(activity.getSupportFragmentManager());
    }


    public void showLowPowerDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mLowPowerPrompts, mSelectedLowPowerPrompt);
        dialog.setListener(value -> {
            mSelectedLowPowerPrompt = value;
            mBind.tvLowPowerPrompt.setText(mLowPowerPrompts.get(value));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void setLowPowerReportInterval(int interval) {
        mBind.etLowPowerReportInterval.setText(String.valueOf(interval));
    }


    public void setLowPowerPayload(int enable) {
        mBind.cbLowPowerPayload.setChecked(enable == 1);
    }

    public void setLowPower(int lowPower) {
        mSelectedLowPowerPrompt = lowPower;
        mBind.tvLowPowerPrompt.setText(mLowPowerPrompts.get(mSelectedLowPowerPrompt));
    }

    public boolean isValid() {
        final String intervalStr = mBind.etLowPowerReportInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr))
            return false;
        final int interval = Integer.parseInt(intervalStr);
        if (interval < 1 || interval > 255) {
            return false;
        }
        return true;
    }

    public void saveParams() {
        final String intervalStr = mBind.etLowPowerReportInterval.getText().toString();
        final int interval = Integer.parseInt(intervalStr);
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setTimeZone(mSelectedTimeZone - 24));
        orderTasks.add(OrderTaskAssembler.setLowPowerPercent(mSelectedLowPowerPrompt));
        orderTasks.add(OrderTaskAssembler.setLowPowerPayloadEnable(mBind.cbLowPowerPayload.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setLowPowerReportInterval(interval));
        LoRaLW008MTEMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
