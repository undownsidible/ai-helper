package com.example.aihelper.common.exception;

import com.fasterxml.jackson.databind.ser.Serializers;

public class TokenError extends BaseException {
    public TokenError(String msg){
        super(msg);
    }
}
