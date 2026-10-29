-- 이슈 대표 이미지(AI가 만든 일러스트) 파일 키. 파일은 서버 저장소(issue.image-dir)에 둔다.

alter table issue
   add column image_key varchar(64);
