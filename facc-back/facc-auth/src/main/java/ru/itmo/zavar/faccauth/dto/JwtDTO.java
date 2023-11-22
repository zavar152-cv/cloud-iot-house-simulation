package ru.itmo.zavar.faccauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.Date;
import java.util.Set;

public enum JwtDTO {
    ;

    private interface Jwt {
        @NotBlank
        String getJwtToken();
    }

    private interface Expiration {
        @NotNull
        Date getJwtExpiration();
    }

    private interface Id {
        @NotNull
        @Positive
        Long getId();
    }

    private interface Username {
        @NotBlank
        @Size(min = 5, max = 25)
        String getUsername();
    }

    private interface RoleList {
        @NotNull
        Set<String> getRoles();
    }

    public enum Request {
        ;

        @Value
        public static class JwtValidation implements Jwt, Username {
            String username;
            String jwtToken;
        }
    }

    public enum Response {
        ;

        @Value
        public static class JwtDetails implements Username, RoleList, Jwt, Expiration, Id {
            Long id;
            String jwtToken;
            Date jwtExpiration;
            String username;
            Set<String> roles;
        }
    }
}
