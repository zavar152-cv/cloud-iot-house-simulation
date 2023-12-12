package ru.itmo.zavar.faccauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.Value;
import ru.itmo.zavar.faccauth.security.ValidPassword;

import java.util.Set;

public enum UserDTO {
    ;

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

    private interface Password {
        @NotBlank
        String getPassword();
    }

    private interface OldPassword {
        @NotBlank
        String getOldPassword();
    }

    private interface RoleList {
        @NotNull
        Set<String> getRoles();
    }

    public enum Request {
        ;

        @Value
        public static class SignUp implements Username, Password {
            String username;
            @ValidPassword
            String password;
        }

        @Value
        @NoArgsConstructor(force = true)
        public static class ChangeRole implements Username {
            String username;
        }

        @Value
        @NoArgsConstructor(force = true)
        public static class ChangeName implements Username {
            String username;
        }

        @Value
        public static class ChangePassword implements OldPassword, Password {
            String oldPassword;
            @ValidPassword
            String password;
        }

        @Value
        public static class SignIn implements Username, Password {
            String username;
            String password;
        }
    }

    public enum Response {
        ;

        @Value
        public static class UserDetails implements Username, RoleList, Id {
            Long id;
            String username;
            Set<String> roles;
        }
    }
}
