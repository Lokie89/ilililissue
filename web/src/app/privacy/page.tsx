import type { Metadata } from "next";

export const metadata: Metadata = { title: "개인정보 처리방침" };

// 초안. 출시 전에 대괄호 항목을 채우고 법률 검토를 받는다(ai-agents/local/ilililissue/app-client-guide.md 6장).
// 수집 항목이나 보관 기간을 바꾸는 코드 변경이 있으면 이 문서도 함께 고친다.
const OPERATOR = "[운영자 이름 또는 상호]";
const OFFICER = "[개인정보 보호책임자 이름]";
const CONTACT = "[문의 이메일]";
const HOSTING = "[클라우드 사업자 이름]";
const EVENT_RETENTION = "[이용 기록 보관 기간, 예: 1년]";
const EFFECTIVE_DATE = "[시행일 YYYY-MM-DD]";

export default function PrivacyPage() {
  return (
    <article className="flex flex-col gap-6 text-sm leading-relaxed [&_h2]:mb-2 [&_h2]:text-base [&_h2]:font-semibold [&_li]:ml-5 [&_li]:list-disc">
      <header>
        <h1 className="mb-2 text-xl font-bold">개인정보 처리방침</h1>
        <p className="text-stone-500">
          {OPERATOR}(이하 &lsquo;운영자&rsquo;)는 1일 1이슈 서비스(웹·앱)를 운영하면서 아래와 같이 개인정보를 처리합니다.
        </p>
      </header>

      <section>
        <h2>1. 처리하는 개인정보와 목적</h2>
        <ul>
          <li>
            회원 가입·로그인: 이메일, 닉네임, 비밀번호. 비밀번호는 복원할 수 없는 방식(해시)으로만 저장합니다.
          </li>
          <li>
            서비스 이용 중 생성되는 정보: 투표, 의견, 추천, 신고, 차단 기록과 그 시각. 의견에는 닉네임이 함께 표시됩니다.
          </li>
          <li>
            이용 통계: 이슈 조회·의견 열람 기록과 시각, 방문자 식별값(브라우저 쿠키 또는 앱 설치 시 만든 임의의 값). 서비스
            개선을 위한 참여 지표 계산에만 쓰며, 방문자 식별값으로 개인을 알아보지 않습니다.
          </li>
          <li>로그인 유지: 로그인 토큰. 서버에는 토큰 원문이 아닌 해시만 저장합니다.</li>
        </ul>
      </section>

      <section>
        <h2>2. 보유 기간과 파기</h2>
        <ul>
          <li>회원 정보는 탈퇴할 때까지 보관합니다.</li>
          <li>
            탈퇴하면 이메일·닉네임·비밀번호를 즉시 지우고 모든 로그인 토큰을 폐기합니다. 작성한 투표와 의견은 서비스 기록으로
            남으며, 의견의 작성자는 &lsquo;탈퇴한 회원&rsquo;으로 표시됩니다. 지우고 싶은 의견은 탈퇴 전에 직접 삭제할 수 있습니다.
          </li>
          <li>이용 통계 기록은 {EVENT_RETENTION} 동안 보관한 뒤 지웁니다.</li>
          <li>장애 복구용 데이터베이스 백업은 14일이 지나면 자동으로 지웁니다.</li>
          <li>로그인 토큰은 만료(최대 14일)되거나 로그아웃·탈퇴하면 폐기합니다.</li>
        </ul>
      </section>

      <section>
        <h2>3. 제3자 제공과 처리 위탁</h2>
        <ul>
          <li>개인정보를 제3자에게 제공하지 않습니다. 광고·외부 분석 도구를 쓰지 않습니다.</li>
          <li>서버 운영을 위해 {HOSTING}의 클라우드 서버에 데이터를 저장합니다.</li>
        </ul>
      </section>

      <section>
        <h2>4. 쿠키</h2>
        <ul>
          <li>
            <code>access_token</code>, <code>refresh_token</code>: 로그인 유지(각각 30분, 14일). 로그아웃하면 지웁니다.
          </li>
          <li>
            <code>visitor_id</code>: 이슈 조회 통계용 임의의 값(1년). 개인정보를 담지 않습니다.
          </li>
          <li>
            브라우저 설정에서 쿠키를 거부하거나 지울 수 있습니다. 로그인 쿠키를 거부하면 로그인이 필요한 기능을 쓸 수 없습니다.
          </li>
        </ul>
      </section>

      <section>
        <h2>5. 이용자의 권리</h2>
        <ul>
          <li>내 정보 화면에서 가입 정보를 확인하고 언제든 탈퇴할 수 있습니다.</li>
          <li>작성한 의견은 직접 수정(1회)하거나 삭제할 수 있습니다.</li>
          <li>그 밖의 열람·정정·삭제·처리 정지 요청은 아래 연락처로 보내 주시면 지체 없이 처리합니다.</li>
        </ul>
      </section>

      <section>
        <h2>6. 안전성 확보 조치</h2>
        <ul>
          <li>모든 통신은 HTTPS로 암호화합니다.</li>
          <li>비밀번호와 로그인 토큰은 해시로만 저장합니다.</li>
          <li>데이터베이스는 외부에서 직접 접속할 수 없게 운영합니다.</li>
        </ul>
      </section>

      <section>
        <h2>7. 개인정보 보호책임자</h2>
        <ul>
          <li>책임자: {OFFICER}</li>
          <li>연락처: {CONTACT}</li>
        </ul>
      </section>

      <section>
        <h2>8. 시행일</h2>
        <p>이 방침은 {EFFECTIVE_DATE}부터 적용합니다. 내용이 바뀌면 시행 7일 전에 서비스 화면으로 알립니다.</p>
      </section>
    </article>
  );
}
