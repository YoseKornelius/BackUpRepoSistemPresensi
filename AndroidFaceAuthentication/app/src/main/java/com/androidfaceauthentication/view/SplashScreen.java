package com.androidfaceauthentication.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.facerecognizer.Recognition;
import com.androidfaceauthentication.facerecognizer.TFLiteFaceRecognizer;
import com.androidfaceauthentication.utils.PreferenceUtils;
import com.androidfaceauthentication.view.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        createSignInIntent();
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result)
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = new Intent(this, MainActivity.class);
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d("FIREBASE", "onSignInResult: " + user.getEmail());
            PreferenceUtils.saveUsername(this, user.getDisplayName());
            String userName = user.getUid() + "_" + user.getDisplayName().replace(" ", "");
            String email = user.getEmail();
            intent.putExtra("email", email);
            //String photosUser = user.getPhotoUrl();
            DatabaseReference embeddingReference = FirebaseDatabase.getInstance().getReference("faceantispooflog/" + userName + "/faceEmbeddings");
            embeddingReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        List<String> embedingFloatList = new ArrayList<>();
                        for (DataSnapshot snapshoot1 : snapshot.getChildren()) {
                            String data = String.valueOf(snapshoot1.getValue());
                            embedingFloatList.add(data);
                        }
                        PreferenceUtils.saveFaceEmbeddings(getApplicationContext(), embedingFloatList);
                        //todo : need to encapsulate this face registration process
                        try {
                            TFLiteFaceRecognizer faceRecoginzer;
                            faceRecoginzer = TFLiteFaceRecognizer.getInstance(getApplicationContext().getAssets());
                            Recognition recognition = new Recognition("0", PreferenceUtils.getUsername(getApplicationContext()),
                                    0.0f, false);
                            recognition.setEmbeddings(PreferenceUtils.getFaceEmbeddings(getApplicationContext()));
                            faceRecoginzer.register(PreferenceUtils.getUsername(getApplicationContext()), recognition);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed, how to handle?
                }

            });

          /*  DatabaseReference usernameReference = FirebaseDatabase.getInstance().getReference("faceantispooflog/" + userName);
            usernameReference.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        //dont Exist! Do whatever.
                        usernameReference.setValue("NONE");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed, how to handle?
                }

            });*/

            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, "Gagal melakukan login ", Toast.LENGTH_LONG).show();
        }
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_create_intent]
    }
}