import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = { title: "이용약관" };

// 초안. 출시 전에 대괄호 항목을 채우고 법률 검토를 받는다(ai-agents/local/ilililissue/app-client-guide.md 6장).
// 3장(금지 행위)과 4장(조치)은 앱 스토어 심사(Apple 1.2: 부적절한 콘텐츠 무관용, 신고·차단, 연락처)에 필요한 내용이다.
const OPERATOR = "[운영자 이름 또는 상호]";
const CONTACT = "[문의 이메일]";
const EFFECTIVE_DATE = "[시행일 YYYY-MM-DD]";

export default function TermsPage() {
  return (
    <article className="flex flex-col gap-6 text-sm leading-relaxed [&_h2]:mb-2 [&_h2]:text-base [&_h2]:font-semibold [&_li]:ml-5 [&_li]:list-disc">
      <header>
        <h1 className="mb-2 text-xl font-bold">이용약관</h1>
        <p className="text-stone-500">
          이 약관은 {OPERATOR}(이하 &lsquo;운영자&rsquo;)가 제공하는 1일 1이슈 서비스(웹·앱)의 이용 조건을 정합니다.
        </p>
      </header>

      <section>
        <h2>1. 서비스</h2>
        <ul>
          <li>매일 하나의 이슈를 소개하고, 회원이 찬성·중립·반대로 투표하고 의견을 남기는 서비스입니다.</li>
          <li>이슈 설명은 운영자가 원문 기사를 바탕으로 직접 작성하며, 원문은 링크로 제공합니다.</li>
          <li>투표 결과는 서비스 참여자 기준이며 여론조사가 아닙니다.</li>
        </ul>
      </section>

      <section>
        <h2>2. 회원</h2>
        <ul>
          <li>이 약관과 <Link href="/privacy" className="underline">개인정보 처리방침</Link>에 동의하면 가입할 수 있습니다.</li>
          <li>계정은 본인만 쓸 수 있습니다. 여러 계정으로 투표 결과를 조작하면 안 됩니다.</li>
          <li>언제든 내 정보 화면에서 탈퇴할 수 있습니다. 탈퇴 후에도 작성한 의견은 &lsquo;탈퇴한 회원&rsquo;으로 남습니다.</li>
        </ul>
      </section>

      <section>
        <h2>3. 금지 행위</h2>
        <p className="mb-2">다음과 같은 의견과 행위는 허용하지 않습니다.</p>
        <ul>
          <li>욕설, 혐오·차별 표현, 특정인이나 집단을 향한 위협과 괴롭힘</li>
          <li>음란하거나 폭력적인 내용</li>
          <li>다른 사람의 개인정보 노출, 명예훼손, 저작권 침해</li>
          <li>광고, 도배, 서비스와 관계없는 내용</li>
          <li>법령을 위반하거나 범죄를 부추기는 내용</li>
          <li>여러 계정 사용, 자동화 프로그램 등으로 투표·추천을 조작하는 행위</li>
        </ul>
      </section>

      <section>
        <h2>4. 신고·차단과 운영자의 조치</h2>
        <ul>
          <li>회원은 부적절한 의견을 신고하고, 보고 싶지 않은 회원을 차단할 수 있습니다.</li>
          <li>운영자는 신고된 의견을 확인해 금지 행위에 해당하면 숨깁니다. 신고가 없어도 금지 행위를 발견하면 숨길 수 있습니다.</li>
          <li>금지 행위를 반복하는 회원은 이용을 제한할 수 있습니다.</li>
          <li>숨김 조치에 이의가 있으면 아래 연락처로 알려 주세요.</li>
        </ul>
      </section>

      <section>
        <h2>5. 게시물</h2>
        <ul>
          <li>의견의 권리와 책임은 작성한 회원에게 있습니다.</li>
          <li>운영자는 서비스 안에서 의견을 보여주고 투표 결과와 함께 집계하는 데에만 의견을 씁니다.</li>
          <li>의견은 이슈당 1개, 수정은 1회입니다. 삭제하면 같은 이슈에 다시 쓸 수 없습니다.</li>
        </ul>
      </section>

      <section>
        <h2>6. 책임의 한계</h2>
        <ul>
          <li>운영자는 서비스를 안정적으로 제공하려고 노력하지만, 점검이나 장애로 일시 중단될 수 있습니다.</li>
          <li>회원이 작성한 의견의 내용은 운영자의 입장이 아닙니다.</li>
        </ul>
      </section>

      <section>
        <h2>7. 약관 변경과 연락처</h2>
        <ul>
          <li>약관을 바꾸면 시행 7일 전에 서비스 화면으로 알립니다.</li>
          <li>문의: {CONTACT}</li>
          <li>시행일: {EFFECTIVE_DATE}</li>
        </ul>
      </section>
    </article>
  );
}
