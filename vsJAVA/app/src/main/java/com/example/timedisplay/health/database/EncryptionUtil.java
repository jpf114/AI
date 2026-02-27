package com.example.timedisplay.health.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {
    
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String PREFS_FILE = "health_encrypted_prefs";
    private static final String KEY_PASSWORD_HASH = "pdf_password_hash";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    private static EncryptedSharedPreferences encryptedPrefs;
    
    public static void init(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            encryptedPrefs = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void savePassword(String password) {
        if (encryptedPrefs != null) {
            String hashedPassword = hashPassword(password);
            encryptedPrefs.edit().putString(KEY_PASSWORD_HASH, hashedPassword).apply();
        }
    }
    
    public static boolean verifyPassword(String password) {
        if (encryptedPrefs != null) {
            String storedHash = encryptedPrefs.getString(KEY_PASSWORD_HASH, null);
            if (storedHash != null) {
                return storedHash.equals(hashPassword(password));
            }
        }
        return false;
    }
    
    public static boolean hasPassword() {
        return encryptedPrefs != null && encryptedPrefs.contains(KEY_PASSWORD_HASH);
    }
    
    public static void clearPassword() {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().remove(KEY_PASSWORD_HASH).apply();
        }
    }
    
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }
    
    public static byte[] encryptData(byte[] data, String password) throws Exception {
        SecretKeySpec keySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(data);
        
        byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
        System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);
        
        return result;
    }
    
    public static byte[] decryptData(byte[] encryptedData, String password) throws Exception {
        if (encryptedData.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted data");
        }
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] cipherText = new byte[encryptedData.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
        
        SecretKeySpec keySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
        
        return cipher.doFinal(cipherText);
    }
    
    private static SecretKeySpec generateKey(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }
    
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
