package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.mapper.ApiKeyMapper;
import uk.ac.ebi.ega.permissions.model.APIKeyListItem;
import uk.ac.ebi.ega.permissions.model.ApiKeyParams;
import uk.ac.ebi.ega.permissions.model.CreatedAPIKey;
import uk.ac.ebi.ega.permissions.persistence.entities.ApiKey;
import uk.ac.ebi.ega.permissions.persistence.repository.ApiKeyRepository;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class ApiKeyServiceImpl implements ApiKeyService {

    private final String keyAlgorithm;
    private final String pbeAlgorithm;
    private final String egaPassword;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyServiceImpl(final String keyAlgorithm, String pbeAlgorithm, String egaPassword,
                             ApiKeyMapper apiKeyMapper, ApiKeyRepository apiKeyRepository) {
        this.keyAlgorithm = keyAlgorithm;
        this.pbeAlgorithm = pbeAlgorithm;
        this.egaPassword = egaPassword;
        this.apiKeyMapper = apiKeyMapper;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public CreatedAPIKey createApiKey(ApiKeyParams params) throws Exception {
        params = this.generateKeys(params);
        this.saveApiKeyDetails(params);
        return apiKeyMapper.fromApiKeyParams(params);
    }

    @Override
    public void saveApiKeyDetails(ApiKeyParams params) {
        ApiKey apiKey = new ApiKey(params.getUsername(), params.getKeyId(), params.getExpiration(), params.getReason(),
                params.getSalt(), params.getPrivateKey());
        this.apiKeyRepository.save(apiKey);
    }

    @Override
    public ApiKeyParams generateKeys(ApiKeyParams params) throws Exception {
        //Generate public and private keys
        KeyPair generateKeyPair = generateKeyPair();
        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();

        //Generate and encrypt salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[20];
        random.nextBytes(salt);

        byte[] encryptedSalt = encryptWithKey(publicKey, salt);

        params.setPrivateKey(encodeBytes(privateKey));
        params.setSalt(encodeBytes(salt));

        String userAndKeyAndEncryptedSalt = encodeString(params.getUsername()) + "." + encodeString(params.getKeyId()) + "." + encodeBytes(encryptedSalt);

        byte[] encryptedToken = encryptWithPassword(egaPassword.getBytes(), userAndKeyAndEncryptedSalt.getBytes());
        params.setToken(encodeBytes(encryptedToken));

        return params;
    }

    @Override
    public List<APIKeyListItem> getApiKeysForUser(String username) {
        return this.apiKeyMapper.fromEntityList(this.apiKeyRepository.findAllByUsername(username));
    }

    @Override
    @Transactional
    public void deleteApiKey(String username, String keyId) {
        this.apiKeyRepository.removeAllByUsernameAndKeyName(username, keyId);
    }

    @Override
    public boolean verifyToken(String token) throws Exception {
        byte[] decryptedToken = decryptWithPassword(egaPassword.getBytes(), decodeString(token));
        String[] tokenParts = new String(decryptedToken).split("\\.");

        if (tokenParts.length != 3) {
            throw new SystemException("Error verifying the API_KEY");
        }

        String username = tokenParts[0];
        String keyId = tokenParts[1];
        String encryptedSalt = tokenParts[2];

        ApiKey apiKey = this.apiKeyRepository.findApiKeyByUsernameAndKeyName(username, keyId)
                .orElseThrow(() -> new SystemException("The API_KEY is not valid"));

        byte[] decryptedSalt = decryptWithKey(decodeString(apiKey.getPrivateKey()), decodeString(encryptedSalt));
        return encodeBytes(decryptedSalt).equals(apiKey.getSalt());
    }

    private KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
        SecureRandom random = new SecureRandom();

        // 512 is keysize
        keyGen.initialize(512, random);

        KeyPair generateKeyPair = keyGen.generateKeyPair();
        return generateKeyPair;
    }

    public byte[] encryptWithKey(byte[] publicKey, byte[] inputData)
            throws Exception {

        PublicKey key = KeyFactory.getInstance(keyAlgorithm)
                .generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance(keyAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(inputData);
    }

    public byte[] decryptWithKey(byte[] privateKey, byte[] inputData)
            throws Exception {

        PrivateKey key = KeyFactory.getInstance(keyAlgorithm)
                .generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(keyAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(inputData);
    }

    private byte[] encryptWithPassword(byte[] password, byte[] inputData) throws Exception {
        Key aesKey = new SecretKeySpec(password, pbeAlgorithm);
        Cipher cipher = Cipher.getInstance(pbeAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(inputData);
    }

    private byte[] decryptWithPassword(byte[] password, byte[] inputData) throws Exception {
        Key aesKey = new SecretKeySpec(password, pbeAlgorithm);
        Cipher cipher = Cipher.getInstance(pbeAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(inputData);
    }

    private String encodeString(String input) {
        return encodeBytes(input.getBytes());
    }

    private String encodeBytes(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    private byte[] decodeString(String input) {
        return Base64.getDecoder().decode(input);
    }
}
