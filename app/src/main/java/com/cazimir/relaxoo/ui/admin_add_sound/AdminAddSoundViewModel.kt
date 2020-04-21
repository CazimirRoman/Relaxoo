package com.cazimir.relaxoo.ui.admin_add_sound

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.model.Sound
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminAddSoundViewModel : ViewModel() {
    fun saveToFirebase(sound: Sound) {

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("sounds")
        val uid = myRef.push().key

        uid?.let {
            val soundWithId = sound.copy(id = uid)
            myRef
                    .child(uid)
                    .setValue(
                            soundWithId
                    ) { databaseError: DatabaseError?, databaseReference: DatabaseReference? ->
                        if (databaseError != null) {
                            Log.d(TAG, "onComplete() called with: databaseError" + databaseError.message)
                        } else {
                            Log.d(TAG, "onComplete() called with: exception occurred")
                        }
                    }
        }
    }

    companion object {
        private const val TAG = "AdminAddSoundViewModel"
    }
}