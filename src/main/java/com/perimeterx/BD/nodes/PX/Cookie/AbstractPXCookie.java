package com.perimeterx.BD.nodes.PX.Cookie;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.perimeterx.BD.nodes.PX.Crypto.PBKDF2Engine;
import com.perimeterx.BD.nodes.PX.Crypto.PBKDF2Parameters;
import com.perimeterx.BD.nodes.PX.Exceptions.PXCookieDecryptionException;
import com.perimeterx.BD.nodes.PX.Utils.HMACUtils;

import org.forgerock.util.encode.Base64;

public abstract class AbstractPXCookie implements PXCookie {
    private static final PXLogger logger = PXLogger.getLogger(AbstractPXCookie.class);

    private static final int KEY_LEN = 32;
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private String cookieVersion;
    protected String ip;

    protected String userAgent;

    protected ObjectMapper mapper;
    protected Config pxConfiguration;
    protected String pxCookie;
    protected JsonNode decodedCookie;
    protected String cookieKey;
    protected String cookieOrig;

    public AbstractPXCookie(Config pxConfiguration, CookieData cookieData) {
        this.mapper = new ObjectMapper();
        this.pxCookie = cookieData.getPxCookie();
        this.cookieOrig = cookieData.getCookieOrig();
        this.pxConfiguration = pxConfiguration;
        this.userAgent = cookieData.isMobileToken() ? "" : cookieData.getUserAgent();
        this.ip = cookieData.getIp();
        this.cookieKey = String.valueOf(pxConfiguration.pxCookieSecret());
        this.cookieVersion = cookieData.getCookieVersion();
    }

    public String getPxCookie() {
        return pxCookie;
    }

    public String getCookieOrig() {
        return cookieOrig;
    }

    public String getCookieVersion() {
        return cookieVersion;
    }

    public JsonNode getDecodedCookie() {
        return decodedCookie;
    }

    public void setDecodedCookie(JsonNode decodedCookie) {
        this.decodedCookie = decodedCookie;
    }

    public boolean deserialize() throws PXCookieDecryptionException {

        if (this.decodedCookie != null) {
            return true;
        }

        JsonNode decodedCookie;
        decodedCookie = this.decrypt();

        if (!isCookieFormatValid(decodedCookie)) {
            return false;
        }

        this.decodedCookie = decodedCookie;
        return true;
    }

    private JsonNode decrypt() throws PXCookieDecryptionException {
        final String[] parts = this.pxCookie.split(":");
        if (parts.length != 3) {
            throw new PXCookieDecryptionException("Part length invalid");
        }
        final byte[] salt = Base64.decode(parts[0]);
        if (salt == null) {
            throw new PXCookieDecryptionException("Salt is empty");
        }
        final int iterations = Integer.parseInt(parts[1]);
        if (iterations < 1 || iterations > 5000) {
            throw new PXCookieDecryptionException("Iterations not in range");
        }
        final byte[] encrypted = Base64.decode(parts[2]);
        if (encrypted == null) {
            throw new PXCookieDecryptionException("No payload");
        }

        final Cipher cipher; // aes-256-cbc decryptData no salt
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int dkLen = KEY_LEN + cipher.getBlockSize();
            PBKDF2Parameters p = new PBKDF2Parameters(HMAC_SHA_256, "UTF-8", salt, iterations);
            byte[] dk = new PBKDF2Engine(p).deriveKey(this.cookieKey, dkLen);
            byte[] key = Arrays.copyOf(dk, KEY_LEN);
            byte[] iv = Arrays.copyOfRange(dk, KEY_LEN, dk.length);
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec parameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            final byte[] data = cipher.doFinal(encrypted, 0, encrypted.length);

            String decryptedString = new String(data, StandardCharsets.UTF_8);
            return mapper.readTree(decryptedString);
        } catch (Exception e) {
            throw new PXCookieDecryptionException("Cookie decryption failed in reason => ".concat(e.getMessage()));
        }
    }

    public boolean isHighScore() {
        return this.getScore() >= this.pxConfiguration.pxBlockingScore();
    }

    public boolean isExpired() {
        return this.getTimestamp() < System.currentTimeMillis();
    }

    public boolean isHmacValid(String hmacStr, String cookieHmac) {
        boolean isValid = HMACUtils.isHMACValid(hmacStr, cookieHmac, this.cookieKey, logger);
        if (!isValid) {
            logger.debug("Cookie HMAC validation failed, value: {}, user-agent: {}", pxCookie, this.userAgent);
        }

        return isValid;
    }

    @Override
    public long getTimestamp() {
        return decodedCookie.get("t").asLong();
    }

    @Override
    public String getUUID() {
        return decodedCookie.get("u").asText();
    }

    @Override
    public String getVID() {
        return decodedCookie.get("v").asText();
    }
}
