package com.ilililissue.member.application.service;

import com.ilililissue.member.application.DuplicateMemberException;
import com.ilililissue.member.application.MemberNotFoundException;
import com.ilililissue.member.application.PasswordMismatchException;
import com.ilililissue.member.application.port.in.GetMemberQuery;
import com.ilililissue.member.application.port.in.MemberWithdrawnEvent;
import com.ilililissue.member.application.port.in.RegisterMemberCommand;
import com.ilililissue.member.application.port.in.RegisterMemberUseCase;
import com.ilililissue.member.application.port.in.WithdrawMemberUseCase;
import com.ilililissue.member.application.port.out.LoadMemberPort;
import com.ilililissue.member.application.port.out.SaveMemberPort;
import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class MemberService implements RegisterMemberUseCase, GetMemberQuery, WithdrawMemberUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    @Override
    public MemberId register(RegisterMemberCommand command) {
        if (loadMemberPort.existsByEmail(command.getEmail())) {
            throw DuplicateMemberException.email();
        }
        if (loadMemberPort.existsByNickname(command.getNickname())) {
            throw DuplicateMemberException.nickname();
        }
        Member member = Member.register(
                command.getEmail(),
                command.getNickname(),
                passwordEncoder.encode(command.getRawPassword())
        );
        return saveMemberPort.save(member).getId();
    }

    @Transactional(readOnly = true)
    @Override
    public Member getMember(MemberId id) {
        return loadMemberPort.findById(id)
                .filter(member -> !member.isWithdrawn())
                .orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    @Override
    public void withdraw(MemberId memberId, String rawPassword) {
        Member member = getMember(memberId);
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, member.getEncodedPassword())) {
            throw new PasswordMismatchException();
        }
        member.withdraw(LocalDateTime.now(clock));
        saveMemberPort.save(member);
        eventPublisher.publishEvent(new MemberWithdrawnEvent(memberId));
    }
}
