package retrospect.aditya.whatzappfilecourierads;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.appnext.ads.interstitial.Interstitial;
import com.appnext.core.callbacks.OnAdClosed;
import com.appnext.core.callbacks.OnAdLoaded;
import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;

public class MainActivity extends ActionBarActivity {

    private PagerSlidingTabStrip tabs;
    ViewPager pager;
    MyPagerAdapter adapter;

    SharedPreferences prefs = null;
    Interstitial interstitial_Ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("retrospect.aditya.whatzappfilecourier", MODE_PRIVATE);

        createDirectories();

        MobileAds.initialize(this, "");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        interstitial_Ad = new Interstitial(this, "");
        interstitial_Ad.loadAd();
        interstitial_Ad.setOnAdClosedCallback(new OnAdClosed() {
            @Override
            public void onAdClosed() {
                MainActivity.this.finish();
                System.exit(0);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_action_wa);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" WhatzApp File Courier");
        getSupportActionBar().setElevation(30);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColor(0xFF34af23);
        tabs.setIndicatorHeight(20);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        if (width <= 480) {
            tabs.setIndicatorHeight(8);
        }
        if (width > 480 && width <= 800) {
            tabs.setIndicatorHeight(12);
        }
        if (width > 800) {
            tabs.setIndicatorHeight(20);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tabs.setElevation(20);
        }

        // init view pager
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Exit App?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(interstitial_Ad.isAdLoaded()) {
                            interstitial_Ad.showAd();
                        } else {
                            MainActivity.super.onBackPressed();
                        }
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean("firstrun", true)) {

            new AlertDialog.Builder(this).setTitle("First App Run!").setMessage("You are running is app for the first time. It is strongly recommended that to see the help section for this app's usage.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), HelpActivity.class));
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();


            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {"Send File", "Received", "Extracted"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new SendFragment();
            } else if (position == 1) {
                return new ReceiveFragment();
            } else {
                return new ExtractedFragment();
            }
        }

    }

    private void createDirectories() {

        String extracted = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/WhatsAppCourier/Extracted";
        String stub = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Stub/";
        String sent = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Sent/";
        String temp = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Temp/";
        String courier = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Temp/Splits/";
        String audio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Audio/";

        boolean checkExtracted = new File(extracted).exists();
        boolean checkStub = new File(stub).exists();
        boolean checkSent = new File(sent).exists();
        boolean checkTemp = new File(temp).exists();
        boolean checkCourier = new File(courier).exists();
        boolean checkAudio = new File(audio).exists();


        if (!checkExtracted) {
            try {
                new File(extracted).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!checkStub) {
            try {
                new File(stub).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!checkSent) {
            try {
                new File(sent).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!checkTemp) {
            try {
                new File(temp).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!checkCourier) {
            try {
                new File(courier).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!checkAudio) {
            try {
                new File(audio).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}