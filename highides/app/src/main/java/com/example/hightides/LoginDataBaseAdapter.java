package com.example.hightides;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LoginDataBaseAdapter {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "login.db";
    private static SQLiteDatabase db;
    private static DataBaseHelper dbHelper;

    // Constructor
    public LoginDataBaseAdapter(Context context) {
        dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Open db
    public LoginDataBaseAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    // Close db
    public void close() {
        db.close();
    }

    // Return instance of db
    public SQLiteDatabase getDatabaseInstance() {
        return db;
    }

    // Get password of username
    public String getSingleEntry(String userName) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("user", null, "userName=?",
                new String[]{userName},null, null, null);

        if(cursor.getCount() < 1)
            return "Does not exist";

        cursor.moveToFirst();
        String getPassword = cursor.getString(cursor.getColumnIndex("userPassword"));

        return getPassword;
    }

    // Insert username and password
    public void insertNewUser(String userName, String password) {

        ContentValues newVals = new ContentValues();
        newVals.put("userName", userName);
        newVals.put("userPassword", password);

        db.insert("user", null, newVals);
    }
}
