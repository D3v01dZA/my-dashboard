package com.altona.dashboard.view.login;

import android.widget.Button;
import android.widget.TextView;

import com.altona.dashboard.R;
import com.altona.dashboard.TestSetting;
import com.altona.dashboard.service.login.Credentials;
import com.altona.dashboard.view.InsecureAppActivity;
import com.altona.dashboard.view.main.MainActivity;

import java.util.Optional;

public class LoginActivity extends InsecureAppActivity {

    public LoginActivity() {
        super(R.layout.activity_login, false);
    }

    @Override
    public void onCreate() {
        setupLoginButton();
    }

    @Override
    public void onEnter() {
        Optional<Credentials> storedCredentials = loginService().getStoredCredentials();
        String username = "";
        String password = "";
        if (storedCredentials.isPresent()) {
            Credentials credentials = storedCredentials.get();
            username = credentials.getUsername();
            password = credentials.getPassword();
            tryLogin(credentials);
        }
        usernameField().setText(TestSetting.CURRENT.getSavedUsername(username));
        passwordField().setText(TestSetting.CURRENT.getSavedPassword(password));
    }

    @Override
    public void onHide() {

    }

    @Override
    protected void onShow() {

    }

    private Button loginButton() {
        return findViewById(R.id.login_button);
    }

    private TextView usernameField() {
        return findViewById(R.id.login_username);
    }

    private TextView passwordField() {
        return findViewById(R.id.login_password);
    }

    private void setupLoginButton() {
        loginButton().setOnClickListener(view -> tryLogin(new Credentials(usernameField().getText().toString(), passwordField().getText().toString())));
    }

    private void tryLogin(Credentials credentials) {
        Button loginButton = loginButton();
        loginButton.setEnabled(false);
        loginService().tryLogin(
                credentials,
                () -> {
                    hideKeyboard();
                    toast("Login Succeeded");
                    loginButton.setEnabled(true);
                    enter(MainActivity.class, true);
                },
                result -> {
                    longToast(result);
                    loginButton.setEnabled(true);
                });
    }
}
