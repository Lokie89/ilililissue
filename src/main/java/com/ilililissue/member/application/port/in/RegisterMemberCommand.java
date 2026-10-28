package com.ilililissue.member.application.port.in;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.Nickname;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterMemberCommand extends SelfValidating<RegisterMemberCommand> {

    @NotNull
    private final Email email;

    @NotNull
    private final Nickname nickname;

    @NotNull
    @Size(min = 8, max = 64)
    private final String rawPassword;

    public RegisterMemberCommand(
            String email,
            String nickname,
            String rawPassword
    ) {
        this.email = Email.from(email);
        this.nickname = Nickname.from(nickname);
        this.rawPassword = rawPassword;
        this.validate();
    }
}
