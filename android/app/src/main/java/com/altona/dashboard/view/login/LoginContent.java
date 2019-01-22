package com.altona.dashboard.view.login;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.LoginService;
import com.altona.dashboard.view.InsecureAppView;
import com.altona.dashboard.view.NavigationStatus;

public class LoginContent extends InsecureAppView<ViewGroup> {

    private LoginService loginService;
    private Navigation navigation;

    private Button button;
    private EditText usernameField;
    private EditText passwordField;

    public LoginContent(MainActivity mainActivity, LoginService loginService, Navigation navigation) {
        super(mainActivity, mainActivity.findViewById(R.id.login_content));
        this.loginService = loginService;
        this.navigation = navigation;
        this.button = view.findViewById(R.id.login_button);
        this.usernameField = view.findViewById(R.id.login_username);
        this.passwordField = view.findViewById(R.id.login_password);
        setupLoginButton();
    }

    @Override
    public NavigationStatus onEnter() {
        if (!loginService.isLoggedIn()) {
            usernameField.setText("test");
            passwordField.setText("password");
        }
        return NavigationStatus.SUCCESS;
    }

    private void setupLoginButton() {
        button.setOnClickListener(view -> {
            button.setEnabled(false);
            loginService.tryLogin(
                    usernameField.getText().toString(),
                    passwordField.getText().toString(),
                    () -> {
                        hideKeyboard();
                        toast("Login Succeeded");
                        button.setEnabled(true);
                        navigation.enterMain();
                    },
                    result -> {
                        toast(result);
                        button.setEnabled(true);
                    });
        });
    }
}
