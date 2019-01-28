package com.altona.security;

public interface Encryptor {

    String encrypt(String original);

    String decrypt(String original);

}
