package com.example.calis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * @author atakanbayrak
 */
public class Hashing {

    public static String toHexString(byte[] hash)
    {
        StringBuilder hexString = new StringBuilder(2* hash.length);
        for(int i = 0; i< hash.length; i++)
        {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1)
            {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String generateHash(String tckn) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(tckn.getBytes());
        return toHexString(encodedHash);
    }
}
