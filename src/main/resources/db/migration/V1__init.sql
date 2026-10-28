-- 초기 스키마 (MySQL 8). JPA 엔티티 매핑에서 생성한 DDL을 기준으로 한다.
-- 이후 스키마 변경은 V2__설명.sql 처럼 새 파일로 추가하고, 이미 적용된 파일은 수정하지 않는다.

create table category (
    id bigint not null auto_increment,
    name varchar(20) not null,
    primary key (id)
) engine=InnoDB;

create table issue (
    category_id bigint not null,
    corrected_at datetime(6),
    id bigint not null auto_increment,
    published_at datetime(6),
    title varchar(100) not null,
    summary varchar(1000) not null,
    status enum ('CLOSED','DRAFT','PUBLISHED','REVIEWED') not null,
    primary key (id)
) engine=InnoDB;

create table issue_source (
    position integer not null check ((position>=0)),
    published_on date not null,
    issue_id bigint not null,
    publisher varchar(50) not null,
    url varchar(2048) not null,
    primary key (position, issue_id)
) engine=InnoDB;

create table member (
    id bigint not null auto_increment,
    withdrawn_at datetime(6),
    nickname varchar(12),
    email varchar(320),
    encoded_password varchar(255),
    role enum ('ADMIN','USER') not null,
    primary key (id)
) engine=InnoDB;

create table opinion (
    deleted bit not null,
    edited bit not null,
    hidden bit not null,
    author_id bigint not null,
    id bigint not null auto_increment,
    issue_id bigint not null,
    recommendation_count bigint not null,
    written_at datetime(6) not null,
    content varchar(500) not null,
    stance enum ('AGREE','DISAGREE','NEUTRAL') not null,
    primary key (id)
) engine=InnoDB;

create table participation_event (
    id bigint not null auto_increment,
    issue_id bigint not null,
    member_id bigint,
    occurred_at datetime(6) not null,
    visitor_id varchar(36),
    stance enum ('AGREE','DISAGREE','NEUTRAL'),
    target_stance enum ('AGREE','DISAGREE','NEUTRAL'),
    type enum ('ISSUE_VIEWED','OPINIONS_VIEWED','OPINION_WRITTEN','VOTED') not null,
    primary key (id)
) engine=InnoDB;

create table recommendation (
    id bigint not null auto_increment,
    member_id bigint not null,
    opinion_id bigint not null,
    recommended_at datetime(6) not null,
    primary key (id)
) engine=InnoDB;

create table refresh_token (
    expires_at datetime(6) not null,
    id bigint not null auto_increment,
    member_id bigint not null,
    token_hash varchar(64) not null,
    primary key (id)
) engine=InnoDB;

create table report (
    id bigint not null auto_increment,
    opinion_id bigint not null,
    reported_at datetime(6) not null,
    reporter_id bigint not null,
    resolved_at datetime(6),
    reason varchar(300) not null,
    status enum ('RECEIVED','RESOLVED_DISMISSED','RESOLVED_HIDDEN') not null,
    primary key (id)
) engine=InnoDB;

create table vote (
    id bigint not null auto_increment,
    issue_id bigint not null,
    member_id bigint not null,
    voted_at datetime(6) not null,
    stance enum ('AGREE','DISAGREE','NEUTRAL') not null,
    primary key (id)
) engine=InnoDB;

alter table category 
   add constraint uk_category_name unique (name);

create index idx_issue_status_published_at 
   on issue (status, published_at);

alter table member 
   add constraint uk_member_email unique (email);

alter table member 
   add constraint uk_member_nickname unique (nickname);

create index idx_opinion_recommended 
   on opinion (issue_id, stance, hidden, deleted, recommendation_count);

create index idx_opinion_latest 
   on opinion (issue_id, stance, hidden, deleted, written_at);

alter table opinion 
   add constraint uk_opinion_issue_author unique (issue_id, author_id);

create index idx_event_type_time 
   on participation_event (type, occurred_at);

create index idx_event_member_time 
   on participation_event (member_id, occurred_at);

create index idx_event_visitor 
   on participation_event (issue_id, visitor_id);

alter table recommendation 
   add constraint uk_recommendation_opinion_member unique (opinion_id, member_id);

create index idx_refresh_token_member 
   on refresh_token (member_id);

alter table refresh_token 
   add constraint uk_refresh_token_hash unique (token_hash);

create index idx_report_status 
   on report (status, reported_at);

alter table report 
   add constraint uk_report_opinion_reporter unique (opinion_id, reporter_id);

alter table vote 
   add constraint uk_vote_issue_member unique (issue_id, member_id);

alter table issue_source 
   add constraint fk_issue_source_issue 
   foreign key (issue_id) 
   references issue (id);
