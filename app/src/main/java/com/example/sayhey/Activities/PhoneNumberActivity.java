package com.example.sayhey.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sayhey.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneNumberActivity extends AppCompatActivity {

    //private EditText mobileNumberText;
    //private Button continueButton;

    private ActivityPhoneNumberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            Intent intent = new Intent(PhoneNumberActivity.this, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity();

        }

        //mobileNumberText = findViewById(R.id.mobileNumberText);
        //continueButton = findViewById(R.id.continueButton);

        binding.mobileNumberText.requestFocus();

        binding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = binding.mobileNumberText.getText().toString();

                if (!phoneNumber.isEmpty()) {

                    Intent intent = new Intent(PhoneNumberActivity.this, OtpActivity.class);
                    intent.putExtra("phoneNumber", "+"+phoneNumber);
                    startActivity(intent);

                } else {

                    Toast.makeText(PhoneNumberActivity.this, "Please Enter Valid Phone Number!", Toast.LENGTH_SHORT).show();

                }


            }
        });

    }
}