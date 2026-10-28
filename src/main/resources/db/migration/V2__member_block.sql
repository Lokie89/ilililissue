-- 회원 차단. 차단한 회원의 의견 목록에서 차단된 회원의 의견을 뺀다.

create table member_block (
    blocked_at datetime(6) not null,
    blocked_id bigint not null,
    blocker_id bigint not null,
    id bigint not null auto_increment,
    primary key (id)
) engine=InnoDB;

alter table member_block
   add constraint uk_member_block_blocker_blocked unique (blocker_id, blocked_id);
