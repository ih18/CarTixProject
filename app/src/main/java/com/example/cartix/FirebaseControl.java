package com.example.cartix;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseControl {
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase DATABASE;
    private static DatabaseReference REFERENCE;

    private Context context;


    public void readData(FirebaseCallback firebaseCallback){
        getReference().child(getmAuth().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInformation user = dataSnapshot.getValue(UserInformation.class);
                firebaseCallback.onCallbackUser(user);
                Log.d("itaiharel", user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("itaiharel", databaseError.getDetails());

            }
        });
    }
    public FirebaseControl(Context context) {
        this.context = context;
    }

    public static FirebaseAuth getmAuth() {
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }
    public static FirebaseDatabase getDatabase()
    {
        if(DATABASE == null)
            DATABASE = FirebaseDatabase.getInstance();
        return DATABASE;
    }
    public static DatabaseReference getReference()
    {
        if(REFERENCE == null)
            REFERENCE = getDatabase().getReference("users");
        return REFERENCE;
    }

    public void logOutUser()
    {
        getmAuth().signOut();
        context.startActivity(new Intent(context, LoginActivity.class));

    }
    public void createUser(UserInformation user){
        getmAuth().createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            context.startActivity(new Intent(context, HomePage.class));
                            Log.d("TAG", "createUserWithEmail :success", task.getException());
                            getReference().child(task.getResult().getUser().getUid()).setValue(user);

                        }
                        else{
                            Toast.makeText(context, "createUserWithEmail:failure"+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void logInUser(String email, String password){
        getmAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            context.startActivity(new Intent(context, HomePage.class));
                            Log.d("TAG", "createUserWithEmail:success", task.getException());

                        }
                        else{
                            Toast.makeText(context, "createUserWithEmail:failure"+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public boolean isConnect() {
        if(getmAuth().getCurrentUser()!=null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }



}
