package com.example.letschat.utils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtils {

    private static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    private static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    }

    public static FirebaseUser currentUser() {
        return auth().getCurrentUser();
    }

    public static String currentUserID() {
        FirebaseUser user = currentUser();
        return user != null ? user.getUid() : null;
    }

    public static DocumentReference currentUserDetail() {
        if (currentUserID()== null) return null;
        return db().collection("users").document(currentUserID());
    }

    public static CollectionReference usersCollection() {
        return db().collection("users");
    }

    public static boolean isLoggedIn() {
        return currentUser() != null;
    }

    public static void logout() {
        auth().signOut();
    }

    public static String getChatroomId(String userId1, String userId2) {
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;
        }else{
            return userId2+"_"+userId1;
        }
    }

    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    public static CollectionReference getAllChatroomReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds.get(0).equals(FirebaseUtils.currentUserID())){
            return usersCollection().document(userIds.get(1));
        }else{
            return usersCollection().document(userIds.get(0));
        }
    }

    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(FirebaseUtils.currentUserID());
    }

    public static StorageReference  getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }

}