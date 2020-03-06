package ch.frostnova.spring.boot.platform.service.impl;

import ch.frostnova.spring.boot.platform.model.UserInfo;
import ch.frostnova.spring.boot.platform.service.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public UserInfo getUserInfo() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Object details = authentication.getDetails();
        if (details instanceof UserInfo) {
            return (UserInfo) details;
        }

        return UserInfo.aUserInfo()
                .tenant("default")
                .login(String.valueOf(authentication.getPrincipal()))
                .roles(authentication.getAuthorities().stream().map(String::valueOf).collect(Collectors.toSet()))
                .build();

    }
}
