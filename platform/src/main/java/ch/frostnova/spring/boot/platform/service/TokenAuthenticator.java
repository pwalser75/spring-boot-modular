package ch.frostnova.spring.boot.platform.service;

import ch.frostnova.spring.boot.platform.model.UserInfo;

public interface TokenAuthenticator {

    UserInfo authenticate(String token) throws SecurityException;
}
