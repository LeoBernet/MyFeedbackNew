package com.android.leobernet.myfeedback.accounthelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.leobernet.myfeedback.act.MainActivity;
import com.android.leobernet.myfeedback.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AccountHelper {
    private FirebaseAuth mAuth;
    private MainActivity activity;
    //Google sign in
    private GoogleSignInClient mSignInClient;
    public static final int GOOGLE_SIGN_IN_CODE = 10;
    public static final int GOOGLE_SIGN_IN_LINK_CODE = 12;
    private String tempEmail, tempPassword;

    public AccountHelper(FirebaseAuth mAuth, MainActivity activity) {
        this.mAuth = mAuth;
        this.activity = activity;
        googleAccountManager();
    }
    // Sign up by email.

    public void signUp(String email, String password) {
        if (!email.equals("") && !password.equals("")) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                if (mAuth.getCurrentUser() != null) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    sendEmailVerification(user);
                                }
                                activity.updateUI();

                            } else {
                                FirebaseAuthUserCollisionException exception =
                                        (FirebaseAuthUserCollisionException)task.getException();
                                if (exception == null)return;
                                if (exception.getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE")){

                                    linkEmailAndPassword(email, password);
                                }
                            }
                        }
                    });
        } else {
            Toast.makeText(activity, R.string.email_or_password_is_empty, Toast.LENGTH_SHORT).show();
        }
    }

    public void signIn(String email, String password) {
        if (!email.equals("") && !password.equals("")) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                activity.updateUI();
                            } else {
                                Toast.makeText(activity, R.string.authentication_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(activity, R.string.email_or_password_is_empty, Toast.LENGTH_SHORT).show();
        }
    }

    public void signOut() {
        if (mAuth.getCurrentUser() == null)return;
        if (mAuth.getCurrentUser().isAnonymous())return;
        mAuth.signOut();
        mSignInClient.signOut();
        activity.updateUI();
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showDialog(R.string.alert, R.string.mail_verification);
                }
            }
        });
    }
    //Sign in by Google

    private void googleAccountManager() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id)).requestEmail().build();
        mSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void signInGoogle(int code) {

        Intent signInIntent = mSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, code);
    }

    public void signInFirebaseGoogle(String idToken, int index) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(activity, R.string.you_enter, Toast.LENGTH_SHORT).show();
                    if (index == 1)linkEmailAndPassword(tempEmail,tempPassword);
                    activity.updateUI();
                } else {

                }
            }
        });
    }

    //Dialog
    public void showDialogSignInWithLink(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signInGoogle(GOOGLE_SIGN_IN_LINK_CODE);
            }
        });
        builder.create();
        builder.show();
    }

    public void showDialog(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        builder.show();
    }
    public void showDialogNotVerified(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signOut();
            }
        });
        builder.setNegativeButton(R.string.send_email_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAuth.getCurrentUser() != null) {
                sendEmailVerification(mAuth.getCurrentUser());
                }

            }
        });
        builder.create();
        builder.show();
    }

    private void linkEmailAndPassword(String email,String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        if (mAuth.getCurrentUser() != null){
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(activity, R.string.account_lincked, Toast.LENGTH_SHORT).show();
                            if (task.getResult() == null)return;
                            activity.updateUI();
                        } else {

                            Toast.makeText(activity, R.string.auth_field,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }else{

            tempEmail = email;
            tempPassword = password;
            showDialogSignInWithLink(R.string.alert,R.string.sign_link_message);
        }
    }
    public void Anonimous(){
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    activity.updateUI();
                }
            }
        });
    }
}
