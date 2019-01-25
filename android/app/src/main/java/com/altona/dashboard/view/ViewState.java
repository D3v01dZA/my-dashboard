package com.altona.dashboard.view;

import com.altona.dashboard.service.login.Credentials;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.Optional;

@JsonSerialize(using = ViewState.Serializer.class)
public class ViewState {

    public static final String CREDENTIALS = "credentials";

    private Credentials credentials;

    @JsonCreator
    ViewState(@JsonProperty(CREDENTIALS) Credentials credentials) {
        this.credentials = credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void clearCredentials() {
        this.credentials = null;
    }

    public Optional<Credentials> getCredentials() {
        return Optional.ofNullable(credentials);
    }

    public static class Serializer extends JsonSerializer<ViewState> {

        @Override
        public void serialize(ViewState value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField(CREDENTIALS, value.credentials);
            gen.writeEndObject();
        }

    }

}
