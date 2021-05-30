package com.android.leobernet.myfeedback.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.accounthelper.AccountHelper;
import com.android.leobernet.myfeedback.databinding.SignUpLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class SignDialog {
    private FirebaseAuth auth;
    private Activity activity;
    private AccountHelper accountHelper;
    private SignUpLayoutBinding binding;
    private AlertDialog dialog;

    public SignDialog(FirebaseAuth auth, Activity activity, AccountHelper accountHelper) {
        this.auth = auth;
        this.activity = activity;
        this.accountHelper = accountHelper;
    }

    public void showSignDialog(int title, int buttonTitle, final int index) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        binding = SignUpLayoutBinding.inflate(activity.getLayoutInflater());
        dialogBuilder.setView(binding.getRoot());
        binding.tvAlertTitle.setText(title);
        showForgetButton(index);

        binding.buttonSignUp.setText(buttonTitle);
        binding.buttonSignUp.setOnClickListener(onClickSignWithEmail(index));
        binding.bSignGoogle.setOnClickListener(onClickSignWithGoogle());
        binding.bForgetPassword.setOnClickListener(onClickForgetButton());

        dialog = dialogBuilder.create();
        //Код который убирает задний белый фон в Алерт диалоге.
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private View.OnClickListener onClickSignWithEmail(final int index) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().isAnonymous()) {
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (index == 0)
                                        accountHelper.signUp(binding.edEmail.getText().toString(),
                                                binding.edPassword.getText().toString());
                                    else
                                        accountHelper.signIn(binding.edEmail.getText().toString(),
                                                binding.edPassword.getText().toString());
                                }
                            }
                        });
                    }
                }
                dialog.dismiss();
            }
        };
    }

    private View.OnClickListener onClickSignWithGoogle() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() != null) {
                    if (auth.getCurrentUser().isAnonymous()) {
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    accountHelper.signInGoogle(AccountHelper.GOOGLE_SIGN_IN_CODE);
                            }
                        });
                    }
                }
                dialog.dismiss();
            }
        };
    }

    private View.OnClickListener onClickForgetButton() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.edPassword.isShown()) {

                    binding.edPassword.setVisibility(View.GONE);
                    binding.buttonSignUp.setVisibility(View.GONE);
                    binding.bSignGoogle.setVisibility(View.GONE);
                    binding.tvAlertTitle.setText(R.string.forget_password);
                    binding.bForgetPassword.setText(R.string.send_reset_password);
                    binding.tvMessage.setVisibility(View.VISIBLE);
                    binding.tvMessage.setText(R.string.forget_password_message);

                } else {
                    if (!binding.edEmail.getText().toString().equals("")) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(binding.edEmail.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(activity, R.string.email_is_send, Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(activity, R.string.email_is_empty, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    private void showForgetButton(int index) {
        if (index == 0) {
            binding.bForgetPassword.setVisibility(View.GONE);
        } else {
            binding.bForgetPassword.setVisibility(View.VISIBLE);
        }
    }
}
