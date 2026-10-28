package com.ilililissue.vote.domain;

import com.ilililissue.common.DomainException;

public class IssueNotOpenForVotingException extends DomainException {

    private static final String message = "투표할 수 없는 이슈입니다.";

    public IssueNotOpenForVotingException() {
        super(ErrorType.CONFLICT, message);
    }
}
