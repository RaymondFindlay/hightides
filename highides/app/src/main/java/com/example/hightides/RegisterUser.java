package com.example.hightides;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterUser extends AppCompatActivity {

    // Declare loginDataBaseAdapter
    LoginDataBaseAdapter loginDataBaseAdapter;

    // Declare views
    EditText editTextUserName;
    EditText editTextPassword;
    EditText editTextConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Assignments
        loginDataBaseAdapter = new LoginDataBaseAdapter(this);
        loginDataBaseAdapter = loginDataBaseAdapter.open();
        editTextUserName = findViewById(R.id.editText_register_username);
        editTextPassword = findViewById((R.id.editText_register_password));
        editTextConfirmPassword = findViewById(R.id.editText_confirm_password);
    }

    public void registerUser(View view) {
        String userName = editTextUserName.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        ValidateRegex validateRegex = new ValidateRegex();

        if (userName.equals("") || password.equals("") || confirmPassword.equals("")) {
            Toast.makeText(RegisterUser.this, "Please fill in all fields",
                    Toast.LENGTH_LONG).show();
        }

        if (userName.length() < 4) {
            Toast.makeText(RegisterUser.this, "Username must be at least 3 characters",
                    Toast.LENGTH_LONG).show();
        }

        if (validateRegex.validate(password) == false) {
            Toast.makeText(RegisterUser.this,
                    "Password must contain between 6 and 15 characters, contain at least " +
                            "one upper case letter, and a symbol",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Passwords must match",
                    Toast.LENGTH_LONG).show();
            return;
        }
        else {
            loginDataBaseAdapter.insertNewUser(userName, password);
            Toast.makeText(getApplicationContext(), "Account created successfully, you can now login",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RegisterUser.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public class ValidateRegex {
        private Pattern pattern = Pattern.compile(
                "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,15})");

        public boolean validate(String password) {
            Matcher matcher = pattern.matcher(password);
            if(matcher.matches()) {
                return true;
            }
            return false;
        }
    }

    // Already a member, go to login
    public void redirectToLogin(View view) {
        Intent intent = new Intent(RegisterUser.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginDataBaseAdapter.close();
    }
}
