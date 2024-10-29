package com.example.simple;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.ads.nativead.LevelPlayNativeAd;
import com.ironsource.mediationsdk.ads.nativead.LevelPlayNativeAdListener;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InitializationListener;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoListener;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayAdError;
import com.unity3d.mediation.LevelPlayAdInfo;
import com.unity3d.mediation.LevelPlayAdSize;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitRequest;
import com.unity3d.mediation.banner.LevelPlayBannerAdView;
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private LevelPlayInterstitialAd interstitialAd;
    private boolean loading = false;
    private ProgressDialog progressDialog;
    private ViewGroup bannerContainer;

    public void setLoading(boolean loading) {
        this.loading = loading;
        switchLoadingDialog();
    }

    private void switchLoadingDialog() {
        if (loading) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("加载广告中...");
            progressDialog.setCancelable(false); // 是否允许取消
            progressDialog.show();
        } else {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    }

    private void showErrorDialog(int errorCode, String errorMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("出现错误")
                .setMessage(String.format("错误代码：%d\n错误描述：%s", errorCode, errorMessage))
                .setPositiveButton(android.R.string.ok, null)  // "OK" 按钮
                .setIcon(android.R.drawable.ic_dialog_alert)   // 错误图标
                .show();
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bannerContainer = findViewById(R.id.banner_container);
    }

    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    public void loadRewardVideoAd(View view) {
        IronSource.init(this, IronSourceParam.APP_KEY, IronSource.AD_UNIT.REWARDED_VIDEO);
        IronSource.setLevelPlayRewardedVideoListener(new LevelPlayRewardedVideoListener() {
            @Override
            public void onAdAvailable(AdInfo adInfo) {
                setLoading(false);
                Button button = findViewById(R.id.btn_reward_video);
                button.setText("showRewardVideoAd");
                button.setTextColor(Color.GREEN);
                button.setOnClickListener(v -> showRewardVideoAd());
            }

            @Override
            public void onAdUnavailable() {
                Log.i("", "loadRewardVideoAd onAdUnavailable");
            }

            @Override
            public void onAdOpened(AdInfo adInfo) {
            }

            @Override
            public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {
                setLoading(false);
                showErrorDialog(ironSourceError.getErrorCode(), ironSourceError.getErrorMessage());
            }

            @Override
            public void onAdClicked(Placement placement, AdInfo adInfo) {

            }

            @Override
            public void onAdRewarded(Placement placement, AdInfo adInfo) {

            }

            @Override
            public void onAdClosed(AdInfo adInfo) {
                Button button = findViewById(R.id.btn_reward_video);
                button.setText("Cannot be used multiple times");
                button.setTextColor(Color.BLACK);
                button.setEnabled(false);
            }
        });
        setLoading(true);
    }

    public void showRewardVideoAd() {
        if (IronSource.isRewardedVideoAvailable()) {
            IronSource.showRewardedVideo();
        }
    }

    public void showInterstitialAd(View view) {
        setLoading(true);

        List<LevelPlay.AdFormat> legacyAdFormats = Arrays.asList(LevelPlay.AdFormat.REWARDED);
        LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(IronSourceParam.APP_KEY)
                .withLegacyAdFormats(legacyAdFormats)
                .build();

        LevelPlay.init(this, initRequest, new LevelPlayInitListener() {
            @Override
            public void onInitSuccess(LevelPlayConfiguration levelPlayConfiguration) {
                interstitialAd = new LevelPlayInterstitialAd(IronSourceParam.INTERSTITIAL_AD_UNIT_ID);
                interstitialAd.setListener(new LevelPlayInterstitialAdListener() {
                    @Override
                    public void onAdLoaded(LevelPlayAdInfo levelPlayAdInfo) {
                        setLoading(false);
                        interstitialAd.showAd(MainActivity.this);
                    }

                    @Override
                    public void onAdLoadFailed(LevelPlayAdError levelPlayAdError) {
                        setLoading(false);
                        showErrorDialog(levelPlayAdError.getErrorCode(), levelPlayAdError.getErrorMessage());
                    }

                    @Override
                    public void onAdDisplayed(LevelPlayAdInfo levelPlayAdInfo) {

                    }
                });
                interstitialAd.loadAd();
            }

            @Override
            public void onInitFailed(LevelPlayInitError levelPlayInitError) {
                setLoading(false);
                showErrorDialog(levelPlayInitError.getErrorCode(), levelPlayInitError.getErrorMessage());
            }
        });
    }

    public void showBannerAd(View view) {
        setLoading(true);

        List<LevelPlay.AdFormat> legacyAdFormats = Arrays.asList(LevelPlay.AdFormat.REWARDED);
        LevelPlayInitRequest initRequest = new LevelPlayInitRequest.Builder(IronSourceParam.APP_KEY)
                .withLegacyAdFormats(legacyAdFormats)
                .build();

        LevelPlay.init(this, initRequest, new LevelPlayInitListener() {
            @Override
            public void onInitSuccess(LevelPlayConfiguration levelPlayConfiguration) {
                LevelPlayAdSize adSize = LevelPlayAdSize.createAdaptiveAdSize(getBaseContext());
                LevelPlayBannerAdView bannerAd = new LevelPlayBannerAdView(getBaseContext(), IronSourceParam.BANNER_AD_UNIT_ID);
                bannerAd.setAdSize(adSize);
                bannerAd.setBannerListener(new LevelPlayBannerAdViewListener() {
                    @Override
                    public void onAdLoaded(LevelPlayAdInfo levelPlayAdInfo) {
                        setLoading(false);
                        bannerContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdLoadFailed(LevelPlayAdError levelPlayAdError) {
                        setLoading(false);
                        showErrorDialog(levelPlayAdError.getErrorCode(), levelPlayAdError.getErrorMessage());
                    }
                });
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bannerAd.setLayoutParams(layoutParams);
                bannerContainer.addView(bannerAd);
                bannerAd.loadAd();
            }

            @Override
            public void onInitFailed(LevelPlayInitError levelPlayInitError) {
                setLoading(false);
                showErrorDialog(levelPlayInitError.getErrorCode(), levelPlayInitError.getErrorMessage());
            }
        });
    }
}