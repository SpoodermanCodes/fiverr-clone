package com.example.fiverr.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fiverr.models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fiverr_clone.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_PHONE = "phone";
    private static final String COL_AGE = "age";
    private static final String COL_GENDER = "gender";
    private static final String COL_SKILLS = "skills";
    private static final String COL_STATUS = "status";
    private static final String COL_ROLE = "role";
    private static final String COL_CREATED_AT = "created_at";

    // Default admin credentials
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_EMAIL = "admin@fiverr.com";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String ADMIN_PHONE = "+15555215554";
    public static final String ADMIN_OTP = "123456";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COL_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_PHONE + " TEXT, "
                + COL_AGE + " INTEGER, "
                + COL_GENDER + " TEXT, "
                + COL_SKILLS + " TEXT, "
                + COL_STATUS + " TEXT DEFAULT 'active', "
                + COL_ROLE + " TEXT DEFAULT 'user', "
                + COL_CREATED_AT + " INTEGER)";
        db.execSQL(createTable);

        // Insert default admin user
        ContentValues adminValues = new ContentValues();
        adminValues.put(COL_USERNAME, ADMIN_USERNAME);
        adminValues.put(COL_EMAIL, ADMIN_EMAIL);
        adminValues.put(COL_PASSWORD, hashPassword(ADMIN_PASSWORD));
        adminValues.put(COL_PHONE, ADMIN_PHONE);
        adminValues.put(COL_AGE, 0);
        adminValues.put(COL_GENDER, "other");
        adminValues.put(COL_SKILLS, "Administration");
        adminValues.put(COL_STATUS, "active");
        adminValues.put(COL_ROLE, "admin");
        adminValues.put(COL_CREATED_AT, System.currentTimeMillis());
        db.insert(TABLE_USERS, null, adminValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Hash password with SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    // Create a new user
    public long createUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PASSWORD, hashPassword(user.getPassword()));
        values.put(COL_PHONE, user.getPhone());
        values.put(COL_AGE, user.getAge());
        values.put(COL_GENDER, user.getGender());
        values.put(COL_SKILLS, user.getSkills());
        values.put(COL_STATUS, "active");
        values.put(COL_ROLE, "user");
        values.put(COL_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_USERS, null, values);
    }

    // Authenticate user by username and password
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPw = hashPassword(password);
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, hashedPw}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    // Authenticate admin by username and password
    public User authenticateAdmin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPw = hashPassword(password);
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=? AND " + COL_ROLE + "=?",
                new String[]{username, hashedPw, "admin"}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    // Get user by ID
    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    // Check if email exists
    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID},
                COL_EMAIL + "=?", new String[]{email},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    // Check if username exists
    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID},
                COL_USERNAME + "=?", new String[]{username},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    // Get all users (non-admin) for admin dashboard
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_ROLE + "=?", new String[]{"user"},
                null, null, COL_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return users;
    }

    // Update user status (active/dormant) - used by SMS receiver
    public boolean updateUserStatus(int userId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        int rows = db.update(TABLE_USERS, values, COL_ID + "=?",
                new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // Delete user
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, COL_ID + "=?",
                new String[]{String.valueOf(userId)}) > 0;
    }

    // Update user details
    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PHONE, user.getPhone());
        values.put(COL_AGE, user.getAge());
        values.put(COL_GENDER, user.getGender());
        values.put(COL_SKILLS, user.getSkills());
        values.put(COL_STATUS, user.getStatus());
        int rows = db.update(TABLE_USERS, values, COL_ID + "=?",
                new String[]{String.valueOf(user.getId())});
        return rows > 0;
    }

    // Helper to convert cursor row to User object
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
        user.setAge(cursor.getInt(cursor.getColumnIndexOrThrow(COL_AGE)));
        user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COL_GENDER)));
        user.setSkills(cursor.getString(cursor.getColumnIndexOrThrow(COL_SKILLS)));
        user.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        return user;
    }
}
