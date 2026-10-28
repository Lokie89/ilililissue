package com.ilililissue.member.adapter.out.persistence;

import com.ilililissue.member.application.port.out.LoadMemberPort;
import com.ilililissue.member.application.port.out.SaveMemberPort;
import com.ilililissue.member.domain.entity.Member;
import com.ilililissue.member.domain.vo.Email;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.member.domain.vo.Nickname;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class MemberPersistenceAdapter implements LoadMemberPort, SaveMemberPort {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(MemberId id) {
        return memberJpaRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    public Optional<Member> findByEmail(Email email) {
        return memberJpaRepository.findByEmail(email.getValue()).map(this::mapToDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return memberJpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public boolean existsByNickname(Nickname nickname) {
        return memberJpaRepository.existsByNickname(nickname.getValue());
    }

    @Override
    public Map<MemberId, Nickname> findNicknames(Collection<MemberId> ids) {
        return memberJpaRepository.findAllById(ids.stream().map(MemberId::getValue).distinct().toList()).stream()
                .collect(Collectors.toMap(entity -> new MemberId(entity.getId()),
                        entity -> Nickname.from(entity.getNickname())));
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity saved = memberJpaRepository.save(new MemberJpaEntity(
                member.getId() == null ? null : member.getId().getValue(),
                member.getEmail().getValue(),
                member.getNickname().getValue(),
                member.getEncodedPassword(),
                member.getRole()
        ));
        return mapToDomain(saved);
    }

    private Member mapToDomain(MemberJpaEntity entity) {
        return Member.withId(
                new MemberId(entity.getId()),
                Email.from(entity.getEmail()),
                Nickname.from(entity.getNickname()),
                entity.getEncodedPassword(),
                entity.getRole()
        );
    }
}
