/*
 * Copyright  2021 EMBL - European Bioinformatics Institute
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionUtilsTest {

    private EncryptionUtils encryptionUtils;
    private final String keyAlgorithm = "RSA";
    private final String pbeAlgorithm = "AES";

    @BeforeEach
    void setup() {
        encryptionUtils = new EncryptionUtils(keyAlgorithm, pbeAlgorithm);
    }

    @Test
    void generateKeyPair_OK() throws NoSuchAlgorithmException {
        KeyPair keyPair = encryptionUtils.generateKeyPair();
        assertThat(keyPair).isNotNull();
        assertThat(keyPair.getPrivate()).isNotNull();
        assertThat(keyPair.getPublic()).isNotNull();
    }

    @Test
    void generateKeyPair_Exception() {
        encryptionUtils = new EncryptionUtils("WEIRD_ALG", pbeAlgorithm);
        assertThatThrownBy(() -> {
            encryptionUtils.generateKeyPair();
        }).isInstanceOf(NoSuchAlgorithmException.class);
    }

    @Test
    void encryptAndDecryptWithKey() throws Exception {
        String textToEncrypt = "This is a string text";
        KeyPair generateKeyPair = encryptionUtils.generateKeyPair();

        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();
        byte[] encryptedBytes = encryptionUtils.encryptWithKey(publicKey, textToEncrypt.getBytes());
        byte[] decryptedBytes = encryptionUtils.decryptWithKey(privateKey, encryptedBytes);

        assertThat(encryptedBytes).isNotNull();
        assertThat(new String(encryptedBytes)).isNotEqualTo(textToEncrypt);
        assertThat(textToEncrypt).isEqualTo(new String(decryptedBytes));
    }

    @Test
    void encryptAndDecryptWithPassword() throws Exception {
        String password = "Bar12345Bar54321";
        String textToEncrypt = "This is a string text";
        byte[] encryptedBytes = encryptionUtils.encryptWithPassword(password.getBytes(), textToEncrypt.getBytes());
        byte[] decryptedBytes = encryptionUtils.decryptWithPassword(password.getBytes(), encryptedBytes);

        assertThat(encryptedBytes).isNotNull();
        assertThat(new String(encryptedBytes)).isNotEqualTo(textToEncrypt);
        assertThat(textToEncrypt).isEqualTo(new String(decryptedBytes));
    }

}