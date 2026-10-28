# 모델 라우팅 매핑 (선택 문서)

이 문서는 같은 프로젝트에서 Codex와 Claude를 함께 쓸 때만 필요하다. 한쪽 모델만 쓰면 이 파일을 만들지 않아도 된다.

목적은 두 모델의 작업 난이도 판단 기준([Claude model-routing.md](../../global/models/claude/model-routing.md), [Codex model-routing.md](../../global/models/codex/model-routing.md))을 이 프로젝트 안에서 서로 맞춰보는 것이다.

## 매핑 표

| 작업 난이도 | Claude 별칭 | Claude 추론 강도 | Codex 등급(문서 용어) | 비고 |
| --- | --- | --- | --- | --- |
| 단순 |  |  | 단순 |  |
| 일반 |  |  | 일반 |  |
| 복잡 |  |  | 복잡 |  |
| 고위험 |  |  | 고위험 |  |

## 실제 모델 ID

- Claude 별칭 → 모델 ID: (비밀값이 아니므로 기록해도 되지만, 계정/제품 표면에 따라 바뀔 수 있음을 함께 적는다)
- Codex 모델 ID / `model_reasoning_effort`: (`.codex/config.toml` 또는 `.codex/agents/*.toml`과 일치시킨다)

## 결정되지 않은 질문

- 
