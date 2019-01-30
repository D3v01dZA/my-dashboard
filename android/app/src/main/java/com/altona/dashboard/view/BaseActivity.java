package com.altona.dashboard.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.altona.dashboard.R;
import com.altona.dashboard.Static;
import com.altona.dashboard.service.login.Credentials;
import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.view.login.LoginActivity;
import com.altona.dashboard.view.settings.SettingsActivity;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String VIEW_STATE = "viewState";
    private int activityId;
    private boolean drawer;

    protected BaseActivity(int activityId, boolean drawer) {
        this.activityId = activityId;
        this.drawer = drawer;
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void longToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void logoutErrorHandler(String message) {
        longToast("Error: " + message);
        enter(LoginActivity.class, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(activityId);

        enableDrawer();

        create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean returned = enter(Static.NAV_ID_TO_CLASS.get(id));
        drawer().closeDrawer(GravityCompat.START);
        return returned;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_settings) {
            return enter(SettingsActivity.class);
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = drawer();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        enter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Integer id = Static.CLASS_TO_NAV_ID.get(getClass());
        if (id != null) {
            navigationView().setCheckedItem(id);
        }
        show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected DrawerLayout drawer() {
        return findViewById(R.id.drawer_layout);
    }

    protected Toolbar toolbar() {
        return findViewById(R.id.toolbar);
    }

    protected NavigationView navigationView() {
        return findViewById(R.id.nav_view);
    }

    protected LoginService loginService() {
        return new LoginService(this);
    }

    protected boolean enter(Class<? extends BaseActivity> activity) {
        return enter(activity, false);
    }

    protected boolean enter(Class<? extends BaseActivity> activity, boolean clearHistory) {
        if (!getClass().equals(activity)) {
            Intent intent = new Intent(this, activity);
            if (clearHistory) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            startActivity(intent);
            return true;
        }
        return false;
    }

    protected abstract void create();

    protected abstract void enter();

    protected abstract void hide();

    protected abstract void show();

    private void enableDrawer() {
        Toolbar toolbar = toolbar();
        setSupportActionBar(toolbar);

        if (drawer) {
            DrawerLayout drawerLayout = drawer();
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = navigationView();
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    private void logout() {
        loginService().logout();
        enter(LoginActivity.class, true);
    }

}
