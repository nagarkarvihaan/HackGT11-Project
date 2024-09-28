package com.example.memolens.firebase;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface FirestoreCallback {
    void onCallbackDocuments(List<DocumentSnapshot> documents);
}
