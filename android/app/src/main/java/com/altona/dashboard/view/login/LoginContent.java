package com.altona.dashboard.view.login;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;
import com.altona.dashboard.TestSetting;
import com.altona.dashboard.nav.Navigation;
import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.view.InsecureAppView;
import com.altona.dashboard.view.NavigationStatus;
import com.altona.dashboard.view.settings.Credentials;

public class LoginContent extends InsecureAppView<ViewGroup> {

    private LoginService loginService;
    private Navigation navigation;

    private Button button;
    private CheckBox remember;
    private EditText usernameField;
    private EditText passwordField;

    public LoginContent(MainActivity mainActivity, LoginService loginService, Navigation navigation) {
        super(mainActivity, navigation, mainActivity.findViewById(R.id.login_content));
        this.loginService = loginService;
        this.navigation = navigation;
        this.button = view.findViewById(R.id.login_button);
        this.remember = mainActivity.findViewById(R.id.login_remember);
        this.usernameField = view.findViewById(R.id.login_username);
        this.passwordField = view.findViewById(R.id.login_password);
        setupLoginButton();
    }

    @Override
    public NavigationStatus onEnter() {
        loginService.getStoredCredentials().ifPresent(this::tryLogin);
        usernameField.setText(TestSetting.CURRENT.getSavedUsername());
        passwordField.setText(TestSetting.CURRENT.getSavedPassword());
        return NavigationStatus.SUCCESS;
    }

    private void setupLoginButton() {
        button.setOnClickListener(view -> {
            tryLogin(new Credentials(usernameField.getText().toString(), passwordField.getText().toString()));
        });
    }

    private void tryLogin(Credentials credentials) {
        button.setEnabled(false);
        loginService.tryLogin(
                remember.isChecked(),
                credentials,
                () -> {
                    hideKeyboard();
                    toast("Login Succeeded");
                    button.setEnabled(true);
                    navigation.enterMain();
                },
                result -> {
                    longToast(result);
                    button.setEnabled(true);
                });
    }
}
