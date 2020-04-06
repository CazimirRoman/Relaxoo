package com.cazimir.relaxoo.ui.admin_add_sound;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.model.Sound;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminAddSoundViewModel extends ViewModel {

    private static final String TAG = "AdminAddSoundViewModel";

    public void saveToFirebase(Sound sound) {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("sounds");

        final String uid = myRef.push().getKey();

        //Sound sound1 = Sound.withId(sound, uid);

        myRef
                .child(uid)
                .setValue(
                        sound,
                        (databaseError, databaseReference) -> {
                            if (databaseError != null) {
                                Log.d(TAG, "onComplete() called with: databaseError" + databaseError.getMessage());
                            } else {
                                Log.d(TAG, "onComplete() called with: exception occurred");
                            }
                        });
    }
}
