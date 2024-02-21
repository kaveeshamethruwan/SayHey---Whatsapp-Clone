package com.example.sayhey.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sayhey.R;
import com.example.sayhey.databinding.ActivityOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;

public class OtpActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;
    private FirebaseAuth auth;
    private PhoneAuthOptions authOptions;
    private String verificationID;

    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_layout);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.progress_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        auth = FirebaseAuth.getInstance();

        if (phoneNumber != null) {

            String phoneNoText = "Verify "+phoneNumber;
            binding.verifyMobileNumberText.setText(phoneNoText);

            authOptions = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                            loadingDialog.dismiss();

                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();

                        }

                        @Override
                        public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                            verificationID = verifyId;
                            loadingDialog.cancel();
                            binding.otpTextView.requestFocus();
                            binding.otpTextView.setFocusable(true);

                        }

                        }).build();

            PhoneAuthProvider.verifyPhoneNumber(authOptions);

        }



        binding.otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {

            }

            @Override
            public void onOTPComplete(String otp) {

                loadingDialog.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, otp);
                auth.signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    Toast.makeText(OtpActivity.this, "Successfully Logged In!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(OtpActivity.this, SetupProfileActivity.class);
                                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finishAffinity();

                                } else {

                                    Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                }

                                loadingDialog.dismiss();

                            }
                        });


            }
        });

    }
}