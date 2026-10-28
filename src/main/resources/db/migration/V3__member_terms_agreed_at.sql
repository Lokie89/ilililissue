-- 가입 시 이용약관·개인정보 처리방침 동의 시각. 이전에 가입한 회원은 null이다.

alter table member
   add column terms_agreed_at datetime(6);
