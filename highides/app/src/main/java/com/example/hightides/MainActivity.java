package com.example.hightides;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    LoginDataBaseAdapter loginDataBaseAdapter;
    EditText editTextUsername;
    EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginDataBaseAdapter = new LoginDataBaseAdapter(this);
        loginDataBaseAdapter = loginDataBaseAdapter.open();

        // Assign views
        editTextUsername = findViewById(R.id.editText_username);
        editTextPassword = findViewById(R.id.editText_password);

        // Clear on activity load
        editTextUsername.setText("");
        editTextPassword.setText("");
    }

    public void logIn(View view) {
        try {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            if (username.equals("") || password.equals("")) {
                Toast.makeText(MainActivity.this, "Please provide your username and password",
                        Toast.LENGTH_LONG).show();
            }

            if (!username.equals("")) {
                String storedPassword = loginDataBaseAdapter.getSingleEntry(username);
                if (password.equals(storedPassword)) {
                    Toast.makeText(MainActivity.this, "Login Successful",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, Dashboard.class);
                    intent.putExtra("Name", username);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "Credentials not recognised, please sign up",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception ex) {
            Log.e("Error", "error login");
        }
    }

    public void redirectToRegister(View view) {
        Intent intent = new Intent(MainActivity.this, RegisterUser.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginDataBaseAdapter.close();
    }
}
