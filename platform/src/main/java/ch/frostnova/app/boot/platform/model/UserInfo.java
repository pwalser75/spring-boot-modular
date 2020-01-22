package ch.frostnova.app.boot.platform.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;

@ApiModel("UserInfo")
public class UserInfo {

    private String tenant;
    private String login;
    private final Set<String> roles = new HashSet<>();

    private UserInfo() {
    }

    @ApiModelProperty(notes = "tenant id for the user (multitenancy support)", example = "tenant123")
    public String getTenant() {
        return tenant;
    }

    @ApiModelProperty(notes = "login id of the user", example = "USER123")
    public String getLogin() {
        return login;
    }

    @ApiModelProperty(notes = "set of granted roles", example = "foo, bla")
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(tenant, userInfo.tenant) &&
                Objects.equals(login, userInfo.login) &&
                Objects.equals(roles, userInfo.roles);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "tenant='" + tenant + '\'' +
                ", login='" + login + '\'' +
                ", roles=" + roles.stream().collect(joining(",")) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, login, roles);
    }

    public static Builder aUserInfo() {
        return new Builder();
    }

    public static class Builder {

        private final UserInfo instance = new UserInfo();
        private boolean consumed;

        private Builder set(Consumer<UserInfo> access) {
            if (consumed) {
                throw new IllegalStateException("already consumed");
            }
            access.accept(instance);
            return this;
        }

        public Builder tenant(String tenant) {
            return set(x -> x.tenant = tenant);
        }

        public Builder login(String login) {
            return set(x -> x.login = login);
        }

        public Builder roles(Set<String> roles) {
            return set(x -> {
                x.roles.clear();
                if (roles != null) {
                    x.roles.addAll(roles);
                }
            });
        }

        public Builder role(String role) {
            return set(x -> x.roles.add(role));
        }

        public UserInfo build() {
            consumed = true;
            return instance;
        }
    }
}
