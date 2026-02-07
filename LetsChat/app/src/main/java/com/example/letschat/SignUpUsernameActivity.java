package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;


public class SignUpUsernameActivity extends BaseActivity {

    EditText usernameInput;
    Button letMeInButton;
    ProgressBar progressBar;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_username);

        usernameInput = findViewById(R.id.username);
        letMeInButton = findViewById(R.id.Let_me_in_button);
        progressBar = findViewById(R.id.username_progress_bar);

        getUsername();

        letMeInButton.setOnClickListener(v -> saveUsername());
    }

    private void saveUsername() {
        String username = usernameInput.getText().toString().trim();

        if (username.isEmpty()) {usernameInput.setError("Username required");return;}
        if (username.length() < 3) {usernameInput.setError("At least 3 characters");return;}
        FirebaseUser currentUser = FirebaseUtils.currentUser();
        if (currentUser == null) {AndroidUtil.showToast(this, "User not logged in");return;}

        setInProgress(true);

        if (userModel != null) {userModel.setUsername(username);}
        else{userModel = new UserModel(
                FirebaseUtils.currentUserID(), username,
                System.currentTimeMillis(), currentUser.getEmail()
            );
        }

        DocumentReference userRef = FirebaseUtils.currentUserDetail();
        if (userRef == null) {
            setInProgress(false);
            AndroidUtil.showToast(this, "Unable to get user reference");
            return;
        }

        userRef.set(userModel)
                .addOnSuccessListener(aVoid -> {
                    setInProgress(false);
                    startActivity(new Intent(SignUpUsernameActivity.this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    AndroidUtil.showToast(this, "Failed: " + e.getMessage());
                });
    }

    private void getUsername() {
        setInProgress(true);

        DocumentReference userRef = FirebaseUtils.currentUserDetail();
        if (userRef == null) {setInProgress(false);return;}

        userRef.get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful() && task.getResult() != null) {
                userModel = task.getResult().toObject(UserModel.class);
                if (userModel != null) {
                    usernameInput.setText(userModel.getUsername());
                }
            }
        });
    }
    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(android.view.View.VISIBLE);
            letMeInButton.setVisibility(android.view.View.GONE);
        } else {
            progressBar.setVisibility(android.view.View.GONE);
            letMeInButton.setVisibility(android.view.View.VISIBLE);
        }
    }
}
