package com.altona.dashboard.nav;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.altona.dashboard.R;
import com.altona.dashboard.view.AppView;

public class Navigation implements NavigationView.OnNavigationItemSelectedListener {

    private AppView mainContent;
    private AppView timeContent;
    private AppView configurationContent;
    private AppView settingsContent;

    private DrawerLayout drawer;

    private AppView current;

    public Navigation(
            AppView mainContent,
            AppView timeContent,
            AppView configurationContent,
            AppView settingsContent,
            DrawerLayout drawer
    ) {
        this.mainContent = mainContent;
        this.timeContent = timeContent;
        this.configurationContent = configurationContent;
        this.settingsContent = settingsContent;
        this.drawer = drawer;
        this.current = mainContent;
        current.enter();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            enter(mainContent);
        } else if (id == R.id.nav_settings) {
            enter(configurationContent);
        } else if (id == R.id.nav_time) {
            enter(timeContent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void backPressed(Runnable delegate) {
        if (current == mainContent) {
            delegate.run();
        } else {
            enter(mainContent);
        }
    }

    public void enterSettings() {
        enter(settingsContent);
    }

    private void enter(AppView appView) {
        current.leave();
        appView.enter();
        current = appView;
    }

}
