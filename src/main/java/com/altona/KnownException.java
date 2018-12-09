package com.altona;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KnownException extends RuntimeException {

    private String message;

}
