# AI Agents

이 저장소는 에이전트가 여러 프로젝트에서 일관되게 일하도록 만드는 기준 문서 모음입니다.

핵심 목표는 단순합니다. 프로젝트마다 매번 새로 설명하지 않아도 되는 공통 규칙은 `global/`에 두고, 프로젝트마다 달라지는 맥락은 `local/`에 둡니다.

## 범위 구분

- `global/`: 어느 프로젝트에서든 공통으로 가져갈 에이전트 기준을 둡니다.
- `local/`: 각 프로젝트별로 달라지는 문서, 데이터, 제약, 기록을 둡니다.

## 현재 모델

- [Codex 모델](./global/models/codex/README.md)
- [Claude 모델](./global/models/claude/README.md)
- [프로젝트별 로컬 문서](./local/README.md)
- [Codex 하네스](./global/models/codex/harness/README.md)
- [Claude 하네스](./global/models/claude/harness/README.md)
- [공통 실행·평가 하네스](./global/harness/README.md)

## 문서 기준

각 에이전트 모델은 다음 문서들을 기준으로 관리합니다.

| 문서 | 역할 |
| --- | --- |
| `AGENT.md` | 에이전트가 먼저 읽는 실행 진입 문서 |
| `goal.md` | 해야 할 일과 하지 말아야 할 일 |
| `context-map.md` | 필요한 문서와 데이터 위치 |
| `project-rules.md` | 프로젝트 진입과 로컬 하네스 생성 기준 |
| `domain-rules.md` | 도메인 정책을 다루는 글로벌 규칙 |
| `model-routing.md` | 역할별 모델 선택 기준 |
| `tools.md` | 허용 도구와 금지 도구 |
| `validation.md` | 검증 체크리스트 |
| `handoff-log.md` | 작업 기록과 다음 세션 인계 |

사람은 각 모델의 `README.md`를 먼저 보면 되고, 에이전트는 `AGENT.md`를 먼저 보면 됩니다. 실제 프로젝트의 도메인 정책은 `local/<project-name>/domain-policy.md`, 아키텍처 청사진은 `local/<project-name>/architecture.md`에 둡니다.

## 하네스 기준

각 모델은 규칙 문서 외에 하네스 문서를 함께 사용합니다. 현재 모델별 하네스는 행동 정책과 판정 기준이고, `global/harness/`는 향후 실행 런타임과 자동 평가가 사용할 공통 데이터 계약입니다.

- `harness/state-machine.md`: 작업 상태와 전이 조건
- `harness/failure-cases.md`: 실패로 판정해야 하는 위반 사례
- `harness/output-schema.md`: 청사진, 구현 결과, 리뷰 결과의 필수 산출물
- `harness/fixtures.md`: 대표 요청과 기대 행동
- `global/harness/deliberation.md`: 독립 의견, 비판, 수정, 합성, 검증 라운드 기반 숙의형 멀티 에이전트 패턴

문서 하네스의 필수 파일과 핵심 참조는 아래 명령으로 확인합니다.

```sh
./scripts/validate-docs.sh
node scripts/validate-harness.mjs
node scripts/test-evaluator.mjs
```

같은 검사는 `.github/workflows/validate-docs.yml`을 통해 `master` 대상 push와 PR에서도 자동 실행됩니다.

## 복사 후 부트스트랩

이 폴더를 다른 프로젝트로 복사해 쓸 때 루트 진입 문서와 기본 Codex 에이전트가 빠졌다면 아래 명령으로 생성합니다. 이미 있는 파일은 덮어쓰지 않습니다.

```sh
node scripts/bootstrap-project-root.mjs
```

생성 대상은 Codex용 `AGENTS.md`, Claude Code용 `CLAUDE.md`, 그리고 `.codex/agents/`와 `.claude/agents/`의 기본 토론 에이전트 세트입니다.

루트 진입 문서만 필요하면 아래 명령을 사용할 수 있습니다.

```sh
node scripts/ensure-entrypoints.mjs
```

`ai-agents/` 디렉터리 자체를 프로젝트 루트 안에 넣어 쓰는 경우에는 부모 루트의 자동 진입 문서가 필요합니다. Codex나 Claude가 `ai-agents` 안의 지침을 한 번이라도 읽었고 부모 루트에 `AGENTS.md` 또는 `CLAUDE.md`가 없다면 아래 명령으로 부모 루트에 얇은 포인터 문서를 생성합니다. 이미 있는 파일은 덮어쓰지 않으며, 부모 폴더가 프로젝트 루트처럼 보이지 않으면 생성하지 않습니다.

```sh
node scripts/install-parent-entrypoints.mjs
```

현재 저장소는 문서 정책과 정적 평가 계약까지 제공하며, 모델 호출, checkpoint 저장, trace 수집, queue, 배포를 수행하는 프로덕션 런타임은 포함하지 않습니다.
