package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseActivity {

    EditText EmailInput,PasswordInput;
    Button LoginOtpButton;
    ProgressBar progressBar;
    Intent intent;
    String email,password;
    FirebaseAuth mAuth;
    TextView SignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EmailInput=findViewById(R.id.get_email);
        PasswordInput=findViewById(R.id.get_password);
        LoginOtpButton=findViewById(R.id.Login_button);
        progressBar=findViewById(R.id.login_progress_bar);
        SignUp=findViewById(R.id.sign_up_textview);

        mAuth=FirebaseAuth.getInstance();
        progressBar.setVisibility(View.GONE);

        LoginOtpButton.setOnClickListener((v) -> {

            email=EmailInput.getText().toString().trim();
            password=PasswordInput.getText().toString().trim();

            if(email.isEmpty()){EmailInput.setError("Email required");return;}

            if(password.isEmpty()){EmailInput.setError("Password required");return;}

            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    // Login successful
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        SignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

    }
}