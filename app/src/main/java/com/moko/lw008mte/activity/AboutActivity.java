package com.moko.lw008mte.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.moko.lw008mte.BuildConfig;
import com.moko.lw008mte.R;
import com.moko.lw008mte.databinding.Lw008MteActivityAboutBinding;
import com.moko.lw008mte.utils.Utils;


public class AboutActivity extends BaseActivity {
    private Lw008MteActivityAboutBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008MteActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
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
