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
import com.altona.dashboard.view.AppView;

public class LoginContent implements AppView {

    private ViewGroup content;
    private LoginService loginService;
    private Navigation navigation;

    private Button button;
    private EditText usernameField;
    private EditText passwordField;

    public LoginContent(MainActivity mainActivity, LoginService loginService, Navigation navigation) {
        this.content = mainActivity.findViewById(R.id.login_content);
        this.loginService = loginService;
        this.navigation = navigation;
        this.button = content.findViewById(R.id.login_button);
        this.usernameField = content.findViewById(R.id.login_username);
        this.passwordField = content.findViewById(R.id.login_password);
        setupLoginButton();
    }

    @Override
    public boolean enter(AppView loginRedirect) {
        if (!loginService.isLoggedIn()) {
            usernameField.setText("test");
            passwordField.setText("password");
        }
        content.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void leave() {
        content.setVisibility(View.GONE);
    }

    private void setupLoginButton() {
        button.setOnClickListener(view -> {
            button.setEnabled(false);
            loginService.tryLogin(
                    usernameField.getText().toString(),
                    passwordField.getText().toString(),
                    () -> {
                        Toast.makeText(content.getContext(), "Login Succeeded", Toast.LENGTH_SHORT).show();
                        button.setEnabled(true);
                        navigation.enterMain();
                    },
                    result -> {
                        Toast.makeText(content.getContext(), result, Toast.LENGTH_SHORT).show();
                        button.setEnabled(true);
                    });
        });
    }
}
