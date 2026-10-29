import type { Stance } from "./types";

// 입장별 색 클래스. Tailwind가 찾을 수 있도록 클래스 이름을 문자열 그대로 둔다.
export const STANCE_STYLE: Record<Stance, { solid: string; soft: string; text: string; border: string }> = {
  AGREE: {
    solid: "bg-stance-agree",
    soft: "bg-stance-agree-soft",
    text: "text-stance-agree",
    border: "border-stance-agree",
  },
  NEUTRAL: {
    solid: "bg-stance-neutral",
    soft: "bg-stance-neutral-soft",
    text: "text-stance-neutral",
    border: "border-stance-neutral",
  },
  DISAGREE: {
    solid: "bg-stance-disagree",
    soft: "bg-stance-disagree-soft",
    text: "text-stance-disagree",
    border: "border-stance-disagree",
  },
};
