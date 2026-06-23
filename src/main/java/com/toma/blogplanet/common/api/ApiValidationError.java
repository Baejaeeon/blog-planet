package com.toma.blogplanet.common.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiValidationError {

    private final String field;
    private final String message;
    private final Object rejectedValue;
}
