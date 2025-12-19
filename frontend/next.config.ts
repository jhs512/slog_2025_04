import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async redirects() {
    return [
      {
        // /post/ 로 시작하는 모든 경로를
        source: '/post/:path*',

        // /p/ 로 영구 이동
        destination: '/p/:path*',

        // true면 308(사실상 301급)로 고정 리다이렉트
        permanent: true,
      },

      // (선택) /post 자체도 /p로
      {
        source: '/post',
        destination: '/p',
        permanent: true,
      },
    ];
  },
  images: {
    dangerouslyAllowSVG: true,
    unoptimized: process.env.NODE_ENV === "development",
    remotePatterns: [
      {
        protocol: "http",
        hostname: "localhost",
        port: "8080",
        pathname: "/**",
      },
      {
        protocol: "http",
        hostname: "*.kakaocdn.net",
      },
      {
        protocol: "https",
        hostname: "*.kakaocdn.net",
      },
      {
        protocol: "https",
        hostname: "placehold.co",
      },
      {
        protocol: "https",
        hostname: "*.pstatic.net",
      },
      {
        protocol: "http",
        hostname: "localhost",
      },
      {
        protocol: "https",
        hostname: "api.slog.gg",
      },
    ],
    contentSecurityPolicy: "default-src 'self'; img-src 'self' data: https:;",
  },
};

export default nextConfig;
