package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.letschat.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends BaseActivity {

    EditText EmailInput, PasswordInput, ConfirmPasswordInput;
    Button signUpButton;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EmailInput = findViewById(R.id.get_email);
        PasswordInput = findViewById(R.id.get_password);
        ConfirmPasswordInput = findViewById(R.id.get_confirmpassword);
        signUpButton = findViewById(R.id.SignUp_button);
        progressBar = findViewById(R.id.SignUp_progress_bar);

        mAuth = FirebaseAuth.getInstance();
        progressBar.setVisibility(View.GONE);

        signUpButton.setOnClickListener(v -> {
            String email = EmailInput.getText().toString().trim();
            String password = PasswordInput.getText().toString().trim();
            String confirmPassword = ConfirmPasswordInput.getText().toString().trim();

            if (email.isEmpty()) {
                EmailInput.setError("Email required");
                return;
            }

            if (password.isEmpty()) {
                PasswordInput.setError("Password required");
                return;
            }

            if (password.length() < 6) {
                PasswordInput.setError("Minimum 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                ConfirmPasswordInput.setError("Passwords do not match");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    AndroidUtil.showToast(getApplicationContext(),"Account created successfully");
                    startActivity(new Intent(SignUpActivity.this, SignUpUsernameActivity.class));
                    finish();
                } else {
                    AndroidUtil.showToast(getApplicationContext(),"Signup failed");
                }
            });
        });
    }
}