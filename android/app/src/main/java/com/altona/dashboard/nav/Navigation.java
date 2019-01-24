package com.altona.dashboard.nav;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.service.time.TimeService;
import com.altona.dashboard.view.AppView;
import com.altona.dashboard.view.configuration.ConfigurationContent;
import com.altona.dashboard.view.NavigationStatus;
import com.altona.dashboard.view.login.LoginContent;
import com.altona.dashboard.view.main.MainContent;
import com.altona.dashboard.view.settings.Settings;
import com.altona.dashboard.view.settings.SettingsContent;
import com.altona.dashboard.view.time.TimeContent;

import okhttp3.OkHttpClient;

public class Navigation implements NavigationView.OnNavigationItemSelectedListener {

    private LoginService loginService;

    private MainContent mainContent;
    private TimeContent timeContent;
    private ConfigurationContent configurationContent;
    private SettingsContent settingsContent;
    private LoginContent loginContent;

    private DrawerLayout drawer;

    private AppView<?> current;

    public Navigation(
            MainActivity mainActivity,
            DrawerLayout drawer
    ) {
        Settings settings = new Settings(mainActivity, this);
        OkHttpClient okHttpClient = new OkHttpClient();
        this.loginService = new LoginService(mainActivity, settings, okHttpClient);
        TimeService timeService = new TimeService(loginService);
        this.mainContent = new MainContent(mainActivity, loginService, this);
        this.timeContent = new TimeContent(mainActivity, loginService, this, timeService);
        this.configurationContent = new ConfigurationContent(mainActivity, loginService, this);
        this.settingsContent = new SettingsContent(mainActivity, this, settings);
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

    public void appHidden() {
        current.hide();
    }

    public void appShown() {
        current.enter();
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

    public void logout() {
        loginService.logout();
        enter(loginContent);
    }

    private boolean enter(AppView<?> appView) {
        current.leave();
        NavigationStatus status = appView.enter();
        if (status == NavigationStatus.SUCCESS) {
            current = appView;
            return true;
        } else if (status == NavigationStatus.LOGIN_REDIRECT) {
            return enter(loginContent);
        } else if (status == NavigationStatus.MAIN_REDIRECT) {
            return enter(mainContent);
        } else {
            throw new IllegalArgumentException("Unrecognized Navigation Status: " + status);
        }
    }

}
