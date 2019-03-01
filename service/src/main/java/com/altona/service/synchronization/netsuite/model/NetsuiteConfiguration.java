package com.altona.service.synchronization.netsuite.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class NetsuiteConfiguration {

    @Getter
    @NonNull
    private String username;

    @Getter
    @NonNull
    private String password;

    @Getter
    @NonNull
    private String project;

    @Getter
    @NonNull
    private String task;

    @NonNull
    private List<Answer> answers;

    public Optional<String> getAnswer(String test) {
        return answers.stream()
                .filter(answer -> test.contains(answer.getFragment()))
                .map(Answer::getAnswer)
                .findFirst();
    }

    @Getter
    @AllArgsConstructor
    private static class Answer {

        private String fragment;
        private String answer;

    }

}
