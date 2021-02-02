/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.ega.permissions.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EncryptionUtils {

    private final String keyAlgorithm;
    private final String pbeAlgorithm;

    public EncryptionUtils(final String keyAlgorithm, String pbeAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
        this.pbeAlgorithm = pbeAlgorithm;
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
        keyGen.initialize(512, new SecureRandom()); // 512 is keysize
        return keyGen.generateKeyPair();
    }

    public byte[] encryptWithKey(byte[] publicKey, byte[] inputData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        PublicKey key = KeyFactory.getInstance(keyAlgorithm)
                .generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance(keyAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(inputData);
    }

    public byte[] decryptWithKey(byte[] privateKey, byte[] inputData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {

        PrivateKey key = KeyFactory.getInstance(keyAlgorithm).generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(keyAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(inputData);
    }

    public byte[] encryptWithPassword(byte[] password, byte[] inputData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Key aesKey = new SecretKeySpec(password, pbeAlgorithm);
        Cipher cipher = Cipher.getInstance(pbeAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(inputData);
    }

    public byte[] decryptWithPassword(byte[] password, byte[] inputData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Key aesKey = new SecretKeySpec(password, pbeAlgorithm);
        Cipher cipher = Cipher.getInstance(pbeAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(inputData);
    }

}
