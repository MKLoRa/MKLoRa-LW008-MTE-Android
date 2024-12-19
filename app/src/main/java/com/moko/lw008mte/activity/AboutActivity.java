package com.moko.lw008mte.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.moko.lw008mte.AppConstants;
import com.moko.lw008mte.BuildConfig;
import com.moko.lw008mte.R;
import com.moko.lw008mte.databinding.Lw008MteActivityAboutBinding;
import com.moko.lw008mte.utils.Utils;

import androidx.core.content.ContextCompat;


public class AboutActivity extends BaseActivity {
    private Lw008MteActivityAboutBinding mBind;

    private int mDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008MteActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mDeviceType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, 0x00);
        if (mDeviceType == 0x10) {
            mBind.appName.setText("LW008-PTE");
        } else if (mDeviceType == 0x20) {
            mBind.appName.setText("LW001-BGE");
            mBind.ivLogo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.lw001_ic_logo));
        } else if (mDeviceType == 0x30) {
            mBind.appName.setText("LW011-MT");
            mBind.ivLogo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.lw011_ic_logo));
        }
        if (!BuildConfig.IS_LIBRARY) {
            mBind.appVersion.setText(String.format("APP Version:V%s", Utils.getVersionInfo(this)));
        }
    }

    public void onBack(View view) {
        finish();
    }

    public void onCompanyWebsite(View view) {
        if (isWindowLocked())
            return;
        Uri uri = Uri.parse("https://" + getString(R.string.company_website));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
