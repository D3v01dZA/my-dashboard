package com.altona.dashboard.view;

import android.os.Parcel;
import android.os.Parcelable;

import com.altona.dashboard.service.login.Credentials;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.Optional;

public class ViewState implements Parcelable {

    private Credentials credentials;

    ViewState(Credentials credentials) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(credentials, flags);
    }

    public static final Parcelable.Creator<ViewState> CREATOR = new Creator<ViewState>() {
        @Override
        public ViewState createFromParcel(Parcel source) {
            return new ViewState(source.readParcelable(getClass().getClassLoader()));
        }

        @Override
        public ViewState[] newArray(int size) {
            return new ViewState[size];
        }
    };
}
