package com.ilililissue.vote.application;

import com.ilililissue.common.DomainException;

public class VoteException extends DomainException {

    private VoteException(ErrorType type, String message) {
        super(type, message);
    }

    public static VoteException alreadyVoted() {
        return new VoteException(ErrorType.CONFLICT, "이미 투표한 이슈입니다.");
    }

    public static VoteException voteRequired() {
        return new VoteException(ErrorType.FORBIDDEN, "투표한 회원만 이용할 수 있습니다.");
    }
}
