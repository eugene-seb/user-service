package com.eugene.user_service.exception;

import java.util.Date;

public record ErrorMessage(int statusCode, Date timestamp, String message, String cause,
                           String description) {

}