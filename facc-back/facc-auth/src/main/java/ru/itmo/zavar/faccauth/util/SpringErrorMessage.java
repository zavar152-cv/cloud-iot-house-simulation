package ru.itmo.zavar.faccauth.util;

import java.util.Date;

public record SpringErrorMessage(Date timestamp, Integer status, String error, String message, String path) {
}
