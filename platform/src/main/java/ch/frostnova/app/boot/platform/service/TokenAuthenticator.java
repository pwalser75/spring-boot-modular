package ch.frostnova.app.boot.platform.service;

import ch.frostnova.app.boot.platform.model.UserInfo;

public interface TokenAuthenticator {

    UserInfo authenticate(String token) throws SecurityException;
}
