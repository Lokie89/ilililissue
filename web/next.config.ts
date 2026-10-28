import type { NextConfig } from "next";

// 브라우저는 같은 출처의 /api/* 로 요청하고, Next.js가 백엔드로 전달한다.
// 같은 출처가 되므로 인증 쿠키(SameSite=Lax)가 CORS 없이 동작한다.
const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  // Docker 이미지용 최소 실행 파일(.next/standalone)을 만든다.
  output: "standalone",
  cacheComponents: true,
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${backendUrl}/api/:path*` }];
  },
};

export default nextConfig;
