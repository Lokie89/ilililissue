-- 투표 연장 시각. 매일 06:59 자동 마감은 이 값(없으면 게시 시각)이 그날 0시 전인 이슈를 마감한다.

alter table issue
   add column extended_at datetime(6);
