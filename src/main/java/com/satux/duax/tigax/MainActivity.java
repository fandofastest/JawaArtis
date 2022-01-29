package com.satux.duax.tigax;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.satux.duax.tigax.adapters.ViewPagerAdapter;
import com.satux.duax.tigax.ads.Ads;
import com.satux.duax.tigax.fragments.AllSongFragment;
import com.satux.duax.tigax.fragments.FavoriteFragment;
import com.satux.duax.tigax.fragments.PlaylistFragment;
import com.satux.duax.tigax.fragments.ServerFragment;
import com.satux.duax.tigax.utils.Config;
import com.satux.duax.tigax.utils.SharedPrefsUtils;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Config {
    ViewPager viewPager;
    int currentViewPagerPosition = 0;
    SharedPrefsUtils pref;
    @NonNull
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_songs:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_allsongs:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_favorite:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_playlists:
                    viewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Ads.showBanners(MainActivity.this,this.<LinearLayout>findViewById(R.id.ads_container));
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        viewPager.setCurrentItem(0);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                navView.getMenu().getItem(currentViewPagerPosition).setChecked(false);
                navView.getMenu().getItem(i).setChecked(true);
                currentViewPagerPosition = i;
                switch (i) {
                    case 0:
                        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
                        return;
                    case 1:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("All Songs");
                        return;
                    case 2:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Favorite");
                        return;
                    case 3:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Playlists");
                        return;
                    default:
                        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ServerFragment());
        adapter.addFragment(new AllSongFragment());
        adapter.addFragment(new FavoriteFragment());
        adapter.addFragment(new PlaylistFragment());
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_searchBtn:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.sleep_timer:
                startActivity(new Intent(this, TimerActivity.class));
                break;
            case R.id.equalizer:
                startActivity(new Intent(this, EqualizerActivity.class));
                break;
            case R.id.sync:
                startActivity(new Intent(this, SplashActivity.class).putExtra("sync", true));
                finish();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.changeTheme:
                final SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(this);
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_choose_accent_color);
                dialog.findViewById(R.id.orange).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "orange");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.cyan).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "cyan");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.green).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "green");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.yellow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "yellow");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.pink).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "pink");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.purple).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "purple");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.grey).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "grey");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.red).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor", "red");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.show();
                break;
            case R.id.rate:
                rateApps();
                break;
            case R.id.share:
                shareApp();
                break;
            case R.id.privacy:
                privacyApp();
                break;
            case R.id.disclamer:
                disclaimerApp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(TYPE);
        share.putExtra(Intent.EXTRA_TEXT, L_SHARE + getPackageName());
        startActivity(Intent.createChooser(share, SHARE_APP));
    }

    public void rateApps() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(T_RATE);
        alert.setMessage(P_RATE);
        alert.setPositiveButton(R_RATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent rate = new Intent(Intent.ACTION_VIEW);
                rate.setData(Uri.parse(L_RATE + getPackageName()));
                startActivity(rate);
                pref.writeSharedPrefs("point", 11);
            }
        });

        alert.setNegativeButton(X_RATE, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                pref.writeSharedPrefs("point", 1);
            }
        });

        Dialog mDialog = alert.create();
        mDialog.show();
    }

    private void privacyApp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(T_PRIVACY);
        alert.setMessage(Html.fromHtml(PRIVACY));

        alert.setNeutralButton(OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        Dialog mDialog = alert.create();
        mDialog.show();
    }

    private void disclaimerApp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(T_DISCLAIMER);
        alert.setMessage(Html.fromHtml(Config.DISCLAIMER));
        alert.setNeutralButton(OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        Dialog mDialog = alert.create();
        mDialog.show();
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();

        if (manager.getBackStackEntryCount() > 1) {
            // If there are back-stack entries, leave the FragmentActivity
            // implementation take care of them.
            manager.popBackStack();
        } else {
            // Otherwise, ask user if he wants to leave :)
            new AlertDialog.Builder(this)
                    .setTitle("Love this App?")
                    .setMessage("Rate us 5 - stars to help make this app even more awesome.\n" +
                            "Thanks!")
                    .setNeutralButton("Cancel", null)
                    .setNegativeButton("Hide", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            moveTaskToBack(true);
                        }
                    })
                    .setPositiveButton("Rate App", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent rate = new Intent(Intent.ACTION_VIEW);
                            rate.setData(Uri.parse(L_RATE + getPackageName()));
                            startActivity(rate);
                        }
                    }).create().show();
        }
    }
}
