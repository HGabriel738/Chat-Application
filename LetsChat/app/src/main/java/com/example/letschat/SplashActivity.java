package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtils;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_LOGGED_IN = 1000L;
    private static final long SPLASH_NOT_LOGGED_IN = 3000L;

    TextView footer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        footer = findViewById(R.id.footer_text);
        final boolean isLoggedIn = FirebaseUtils.isLoggedIn();
        long delay = isLoggedIn ? SPLASH_LOGGED_IN : SPLASH_NOT_LOGGED_IN;

        ViewCompat.setOnApplyWindowInsetsListener( footer, new androidx.core.view.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            v.getPaddingLeft(),
                            v.getPaddingTop(),
                            v.getPaddingRight(),
                            systemBars.bottom + 20 // keep your original margin
                    );
                    return insets;
                }
            }
        );

//        getWindow().getDecorView().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent;
//                if (isLoggedIn) {
//                    intent = new Intent(SplashActivity.this, MainActivity.class);
//                } else {
//                    intent = new Intent(SplashActivity.this, LoginActivity.class);
//                }
//                startActivity(intent);
//                finish();
//            }
//        }, delay);

//        if(getIntent().getExtras()!=null){
//            //from notification
//            String userId = getIntent().getExtras().getString("userId");
//            FirebaseUtils.usersCollection().document(userId).get()
//                    .addOnCompleteListener(task -> {
//                        if(task.isSuccessful()){
//                            UserModel model = task.getResult().toObject(UserModel.class);
//
//                            Intent mainIntent = new Intent(this,MainActivity.class);
//                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                            startActivity(mainIntent);
//
//                            Intent intent = new Intent(this, ChatActivity.class);
//                            AndroidUtil.passUserModelAsIntent(intent,model);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                            finish();
//                        }
//                    });
//
//
//        }else{
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if(FirebaseUtils.isLoggedIn()){
//                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
//                    }else{
//                        startActivity(new Intent(SplashActivity.this,LoginActivity.class));
//                    }
//                    finish();
//                }
//            },1000);
//        }
        Bundle extras = getIntent().getExtras();
        boolean handledNotification = false;

        if (extras != null && extras.containsKey("userId")) {

            String userId = extras.getString("userId");

            if (userId != null && !userId.isEmpty() && FirebaseUtils.isLoggedIn()) {

                handledNotification = true;

                FirebaseUtils.usersCollection()
                        .document(userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {

                                UserModel model = task.getResult().toObject(UserModel.class);
                                if (model == null) {
                                    goNormal(); // fallback
                                    return;
                                }

                                Intent mainIntent = new Intent(this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mainIntent);

                                Intent chatIntent = new Intent(this, ChatActivity.class);
                                AndroidUtil.passUserModelAsIntent(chatIntent, model);
                                startActivity(chatIntent);

                                finish();
                            } else {
                                goNormal(); // fallback
                            }
                        });
            }
        }

        if (!handledNotification) {
            new Handler().postDelayed(this::goNormal, delay);
        }
    }
    private void goNormal() {
        Intent intent = FirebaseUtils.isLoggedIn()
                ? new Intent(this, MainActivity.class)
                : new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();
    }
}