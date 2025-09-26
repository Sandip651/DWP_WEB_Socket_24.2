package com.necsws.websocketrpapoc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.google.common.hash.Hashing;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class AesEncDecService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 564897;
    private static final int KEY_LENGTH = 256;

    private static Key key = null;
    private static IvParameterSpec iv = null;  
   
//  Encryption Process
    public static String encrypt(String data,String passphrase,String salt) throws NoSuchPaddingException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException, InvalidKeyException,
    BadPaddingException, IllegalBlockSizeException {
    	try {
			key = generateKeyFromPassphrase(passphrase,salt);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	iv = generateRandomIV(passphrase);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
//  Decryption Process
    public static String decrypt(String encryptedData,String passphrase,String salt) throws NoSuchPaddingException, NoSuchAlgorithmException,
    InvalidAlgorithmParameterException, InvalidKeyException,
    BadPaddingException, IllegalBlockSizeException {
    	try {
			key = generateKeyFromPassphrase(passphrase,salt);
		} catch (Exception e) {			
			e.printStackTrace();
		}
    	iv = generateRandomIV(passphrase);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static Key generateKeyFromPassphrase(String passphrase,String salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Base64.getDecoder().decode(salt), ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }
    
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // Adjust the size based on your security requirements
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static IvParameterSpec generateRandomIV(String passphrase) {
        
        // Using Passphrase for iv generation
        String ivString = (passphrase + passphrase).toString();
        //to hash a String
        String iv1 = Hashing.sha256()//sha128
        		  .hashString(ivString, StandardCharsets.UTF_8)
        		  .toString();
        return new IvParameterSpec(iv1.getBytes(), 0, 16);
    }
    
//    private static IvParameterSpec generateRandomIV(String passphrase) {
//    	//simple iv for demo
//        byte[] iv = new byte[16]; // AES block size is 128 bits (16 bytes)
//        return new IvParameterSpec(iv);
//    }
}

