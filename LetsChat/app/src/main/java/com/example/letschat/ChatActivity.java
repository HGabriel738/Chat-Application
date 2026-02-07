package com.example.letschat;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.adapters.ChatRecyclerAdapter;
import com.example.letschat.model.ChatMessageModel;
import com.example.letschat.model.ChatroomModel;
import com.example.letschat.model.UserModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;


import java.util.Arrays;


public class ChatActivity extends BaseActivity {

    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        View rootView = findViewById(android.R.id.content);

        View bottomLayout = findViewById(R.id.bottom_layout);

        ViewCompat.setOnApplyWindowInsetsListener(bottomLayout, (v, insets) -> {
            // Include navigation bar + keyboard height
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.ime() | WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottomInset + 8
            );
            return insets;
        });

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtils.getChatroomId(FirebaseUtils.currentUserID(),otherUser.getUid());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);

        FirebaseUtils.getOtherProfilePicStorageRef(otherUser.getUid()).getDownloadUrl()
                .addOnCompleteListener(t-> {
                    if(t.isSuccessful()){
                        Uri uri  = t.getResult();
                        AndroidUtil.setProfilePic(this,uri,imageView);
                    }
                });

        backBtn.setOnClickListener(v-> getOnBackPressedDispatcher().onBackPressed());
        otherUsername.setText(otherUser.getUsername());

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message);
        }));

        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

    private void setupChatRecyclerView() {
        Query query = FirebaseUtils.getChatroomMessageReference(chatroomId).orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        // Recommendation: Use 'this' instead of 'getApplicationContext()' for better UI styling
        adapter = new ChatRecyclerAdapter(options, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        // --- ADD THIS LINE BELOW ---
        recyclerView.setAdapter(adapter);
        // ---------------------------

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    private void sendMessageToUser(String message) {
        // 1. Update the Chatroom Model (Last Message)
        chatroomModel.setLastMessage(message);
        chatroomModel.setLastMessageSenderId(FirebaseUtils.currentUserID());
        // Use Server Timestamp here
        chatroomModel.setLastMessageTimestamp(FieldValue.serverTimestamp());

        FirebaseUtils.getChatroomReference(chatroomId).set(chatroomModel);

        // 2. Create the Chat Message Model
        // Use Server Timestamp here as well
        ChatMessageModel chatMessageModel = new ChatMessageModel(
                message,
                FirebaseUtils.currentUserID(),
                FieldValue.serverTimestamp()
        );

        FirebaseUtils.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            messageInput.setText("");
                            // sendNotification(message);
                        }
                    }
                });
    }

    private void getOrCreateChatroomModel() {
        FirebaseUtils.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatroomModel=task.getResult().toObject(ChatroomModel.class);
            }
            if(chatroomModel==null){
                chatroomModel=new ChatroomModel(chatroomId,
                        Arrays.asList(FirebaseUtils.currentUserID(),otherUser.getUid()),
                        Timestamp.now(),
                        ""
                );
                FirebaseUtils.getChatroomReference(chatroomId).set(chatroomModel);
            }
        });
    }

    void callApi(JSONObject jsonObject){

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.startListening();
        }
    }
}