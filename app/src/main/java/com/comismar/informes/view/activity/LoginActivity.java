package com.comismar.informes.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.comismar.informes.R;
import com.comismar.informes.view.utils.AppSettings;

public class LoginActivity extends AppCompatActivity {

    private static final long CLOSE_CONFIRM_WINDOW_MS = 2000L;

    private View loginControls;
    private View loginForm;
    private EditText inputUser;
    private EditText inputPassword;
    private CheckBox checkRemember;
    private long lastCloseTapMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnClose = findViewById(R.id.btnCloseLogin);
        Button btnEnter = findViewById(R.id.btnEnter);
        Button btnBack = findViewById(R.id.btnBackLogin);
        TextView txtPoliticaPrivacidad = findViewById(R.id.txtPoliticaPrivacidad);
        TextView txtDatosContacto = findViewById(R.id.txtDatosContacto);

        loginControls = findViewById(R.id.loginControls);
        loginForm = findViewById(R.id.loginForm);
        inputUser = findViewById(R.id.inputUser);
        inputPassword = findViewById(R.id.inputPassword);
        checkRemember = findViewById(R.id.checkRemember);

        showLoginForm(false);

        btnLogin.setOnClickListener(v -> showLoginForm(true));

        btnBack.setOnClickListener(v -> {
            inputUser.setText("");
            inputPassword.setText("");
            showLoginForm(false);
        });

        btnEnter.setOnClickListener(v -> handleLogin());
        btnClose.setOnClickListener(v -> handleCloseRequest());

        txtPoliticaPrivacidad.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, PoliticaPrivacidadActivity.class);
            startActivity(intent);
        });

        txtDatosContacto.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DatosContactoActivity.class);
            startActivity(intent);
        });
    }

    private void showLoginForm(boolean show) {
        loginControls.setVisibility(show ? View.GONE : View.VISIBLE);
        loginForm.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void handleLogin() {
        String user = inputUser.getText().toString().trim();
        String password = inputPassword.getText().toString();

        if (!isValidCredentials(user, password)) {
            showStyledToast(getString(R.string.login_invalid_credentials));
            return;
        }

        AppSettings.setRememberLoginEnabled(this, checkRemember.isChecked());

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isValidCredentials(String user, String password) {
        return ("Alvaro".equals(user) && "oravlA".equals(password))
                || ("Santi".equals(user) && "itnaS".equals(password));
    }

    private void handleCloseRequest() {
        long now = System.currentTimeMillis();
        if (now - lastCloseTapMs <= CLOSE_CONFIRM_WINDOW_MS) {
            closeApp();
            return;
        }

        lastCloseTapMs = now;
        showStyledToast(getString(R.string.login_close_confirm));
    }

    private void closeApp() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finishAffinity();
    }

    private void showStyledToast(String message) {
        TextView toastView = new TextView(this);
        toastView.setText(message);
        toastView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        toastView.setBackgroundResource(R.drawable.boton_redondeado);
        toastView.setPadding(48, 24, 48, 24);

        Toast toast = new Toast(this);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 140);
        toast.show();
    }
}
