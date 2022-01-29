package com.satux.duax.tigax.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.applovin.adview.AppLovinAdView;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinAdSize;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.satux.duax.tigax.R;


public class Ads {


    Activity  activity;
    InterstitialAd mInterstitialAd;
    KProgressHUD hud;
    public static String mode="admob";  //    admob/apploving


    public interface MyCustomObjectListener {
         void onAdsfinish();
    }

    private MyCustomObjectListener listener;

    public void setCustomObjectListener(MyCustomObjectListener listener) {
        this.listener = listener;
    }

    public Ads(Activity activity) {
        this.activity = activity;
            hud = KProgressHUD.create(activity)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel("Loading")
                    .setCancellable(true)
                    .setDetailsLabel("Please Wait")
                    .setMaxProgress(100);

    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void showBanners(Activity activity, LinearLayout layout){

        if (mode.equals("admob")){
            showBannerAdmob(activity,layout);
        }
        else showBannerApplovin(activity, layout);

    }

    public void showInters(){
        if (mode.equals("admob")){
            showinteradmob();
        }
        else showInterApplovin();
    }





     void showInterApplovin()
    {
        final MaxInterstitialAd interstitialAd;
        hud = KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading")
                .setCancellable(true)
                .setDetailsLabel("Please Wait")
                .setMaxProgress(100)
                .show();
        interstitialAd = new MaxInterstitialAd( activity.getString(R.string.maxinter), activity );
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                interstitialAd.showAd();

            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                hud.dismiss();


            }

            @Override
            public void onAdHidden(MaxAd ad) {
                hud.dismiss();
                listener.onAdsfinish();

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                hud.dismiss();
                listener.onAdsfinish();


            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                hud.dismiss();
                listener.onAdsfinish();

            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                hud.dismiss();
                listener.onAdsfinish();


            }
        });

        // Load the first ad
        interstitialAd.loadAd();
    }



      void showinteradmob() {
            hud.show();
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(activity, activity.getString(R.string.inter), adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    interstitialAd.show(activity);
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            hud.dismiss();
                            listener.onAdsfinish();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();

                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            hud.dismiss();
                            listener.onAdsfinish();
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            hud.dismiss();
                            listener.onAdsfinish();
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    hud.dismiss();
                    listener.onAdsfinish();
                }
            });





    }

    @RequiresApi(api = Build.VERSION_CODES.R)
     static   void showBannerAdmob(Activity activity,LinearLayout mAdViewLayout) {
        AdView mAdView;
        mAdViewLayout.removeAllViews();
        mAdView  = new AdView(activity);
        AdSize adSize = getAdSize(activity, activity.getDisplay());
        mAdView.setAdSize(adSize);
        mAdView.setAdUnitId(activity.getString(R.string.banner));
        AdRequest.Builder builder = new AdRequest.Builder();
        mAdView.loadAd(builder.build());
            mAdViewLayout.addView(mAdView);
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
     static void showBannerApplovin(Activity activity,LinearLayout mAdViewLayout) {
        MaxAdView adView = new MaxAdView( activity.getString(R.string.maxbanner), activity );
        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        // Banner height on phones and tablets is 50 and 90, respectively

//        adView.setLayoutParams( new FrameLayout.LayoutParams( width, 200 ) );
        mAdViewLayout.removeAllViews();
        mAdViewLayout.addView(adView);
        adView.loadAd();
    }


    private static AdSize getAdSize(Context context, Display display) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }


    public  void loadOpenAds(){
            AdManagerAdRequest request = new AdManagerAdRequest.Builder().build();
            AppOpenAd.load(
                    activity,
                    activity.getString(R.string.openads),
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        /**
                         * Called when an app open ad has loaded.
                         *
                         * @param ad the loaded app open ad.
                         */
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            ad.show(activity);
                            ad.setFullScreenContentCallback(
                                    new FullScreenContentCallback() {
                                        /** Called when full screen content is dismissed. */
                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                           listener.onAdsfinish();
                                            // Set the reference to null so isAdAvailable() returns false.

                                        }

                                        /** Called when fullscreen content failed to show. */
                                        @Override
                                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                                            listener.onAdsfinish();


                                        }

                                        /** Called when fullscreen content is shown. */
                                        @Override
                                        public void onAdShowedFullScreenContent() {

                                        }
                                    });

//                            Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }

                        /**
                         * Called when an app open ad has failed to load.
                         *
                         * @param loadAdError the error.
                         */
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            listener.onAdsfinish();

//                            Toast.makeText(context, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }
                    });



             }


}