package com.ilililissue.member.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Member {

    private MemberRole role;

    public static Builder builder(MemberRole role) {
        return new Builder(role);
    }

    public boolean isAdministrator() {
        return MemberRole.ADMINISTRATOR == this.role;
    }

    public static class Builder {
        private final MemberRole role;

        private Builder(MemberRole role) {
            this.role = role;
        }

        public Member build() {
            return new Member(this.role);
        }
    }

}
