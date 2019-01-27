package com.altona.repository.time.maconomy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class MaconomyMetadata {

    @NonNull
    private String url;

    @NonNull
    private String authorization;

}
