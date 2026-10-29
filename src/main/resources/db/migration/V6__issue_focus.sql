-- 핵심 쟁점 한 줄과 입장별(찬성, 반대·신중) 대표 근거. 모두 선택이며, 근거는 둘 다 있거나 둘 다 없다.
-- H2(테스트)가 한 문장에 여러 컬럼 추가를 지원하지 않아 나눠 쓴다.

alter table issue
   add column key_point varchar(100);

alter table issue
   add column agree_reason varchar(300);

alter table issue
   add column disagree_reason varchar(300);
