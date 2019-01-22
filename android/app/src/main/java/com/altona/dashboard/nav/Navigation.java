package com.altona.dashboard.nav;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.service.LoginService;
import com.altona.dashboard.view.AppView;
import com.altona.dashboard.view.ConfigurationContent;
import com.altona.dashboard.view.login.LoginContent;
import com.altona.dashboard.view.MainContent;
import com.altona.dashboard.view.settings.Settings;
import com.altona.dashboard.view.settings.SettingsContent;
import com.altona.dashboard.view.time.TimeContent;

import okhttp3.OkHttpClient;

public class Navigation implements NavigationView.OnNavigationItemSelectedListener {

    private AppView mainContent;
    private AppView timeContent;
    private AppView configurationContent;
    private AppView settingsContent;
    private AppView loginContent;

    private DrawerLayout drawer;

    private AppView current;

    public Navigation(
            MainActivity mainActivity,
            DrawerLayout drawer
    ) {
        Settings settings = new Settings(mainActivity);
        OkHttpClient okHttpClient = new OkHttpClient();
        LoginService loginService = new LoginService(settings, okHttpClient);
        this.mainContent = new MainContent(mainActivity, loginService);
        this.timeContent = new TimeContent(mainActivity, loginService);
        this.configurationContent = new ConfigurationContent(mainActivity, loginService);
        this.settingsContent = new SettingsContent(mainActivity, settings);
        this.loginContent = new LoginContent(mainActivity, loginService, this);
        this.drawer = drawer;
        this.current = loginContent;
        enter(mainContent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean successful = false;
        if (id == R.id.nav_home) {
            successful = enter(mainContent);
        } else if (id == R.id.nav_settings) {
            successful = enter(configurationContent);
        } else if (id == R.id.nav_time) {
            successful = enter(timeContent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return successful;
    }

    public void backPressed(Runnable delegate) {
        if (current == mainContent) {
            delegate.run();
        } else {
            enter(mainContent);
        }
    }

    public void enterMain() {
        enter(mainContent);
    }

    public void enterSettings() {
        enter(settingsContent);
    }

    private boolean enter(AppView appView) {
        current.leave();
        boolean successful = appView.enter(loginContent);
        if (successful) {
            current = appView;
        } else {
            current = loginContent;
        }
        return successful;
    }

}
