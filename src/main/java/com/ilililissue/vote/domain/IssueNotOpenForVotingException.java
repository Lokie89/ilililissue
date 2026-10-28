package com.ilililissue.vote.domain;

public class IssueNotOpenForVotingException extends RuntimeException {

    private static final String message = "투표할 수 없는 이슈입니다.";

    public IssueNotOpenForVotingException() {
        super(message);
    }
}
