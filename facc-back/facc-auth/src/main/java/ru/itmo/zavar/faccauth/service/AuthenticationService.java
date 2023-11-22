package ru.itmo.zavar.faccauth.service;

public interface AuthenticationService {
    void addUser(String username, String password);

    String signIn(String username, String password);

    void changeRole(String username, String role);
}
