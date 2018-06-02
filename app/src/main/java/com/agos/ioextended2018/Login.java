package com.agos.ioextended2018;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {


    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener fireAuthStateListener;
    private FirebaseUser user = null;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        getSupportActionBar().hide();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();

        fireAuthStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Crashlytics.log(Log.DEBUG, App.tag, "Nuevo usuario:" + user.getUid());
                startActivity(new Intent(Login.this, Main.class));
                finish();
            }
        };

        findViewById(R.id.login).setOnClickListener(view -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setCurrentScreen(Login.this, Login.class.getName(), null);
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(fireAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fireAuthStateListener != null) {
            firebaseAuth.removeAuthStateListener(fireAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, App.tag, e.getMessage());
                Crashlytics.logException(e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        startActivity(new Intent(Login.this, Main.class));
                    }
                });
    }
}
