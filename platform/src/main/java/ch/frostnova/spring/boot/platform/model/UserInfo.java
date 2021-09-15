package ch.frostnova.spring.boot.platform.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.text.Collator;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

@ApiModel("UserInfo")
public class UserInfo {

    private final Set<String> roles = new TreeSet<>(Collator.getInstance());
    private final Map<String, String> additionalClaims = new TreeMap<>(Collator.getInstance());
    private String tenant;
    private String login;

    private UserInfo() {
    }

    public static Builder aUserInfo() {
        return new Builder();
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

    @ApiModelProperty(notes = "map of additional claims")
    public Map<String, String> getAdditionalClaims() {
        return additionalClaims;
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
                ", roles=" + String.join(",", roles) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, login, roles);
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

        public Builder additionalClaims(Map<String, String> additionalClaims) {
            return set(x -> {
                x.additionalClaims.clear();
                if (additionalClaims != null) {
                    x.additionalClaims.putAll(additionalClaims);
                }
            });
        }

        public Builder additionalClaim(String key, String value) {
            return set(x -> x.additionalClaims.put(key, value));
        }

        public UserInfo build() {
            consumed = true;
            return instance;
        }
    }
}
