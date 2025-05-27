export { auth as middleware } from "@/lib/auth";

export const config = {
  // Protect all routes under /dashboard, /api, /diagram, and the token example page
  matcher: [
    "/dashboard/:path*",
    "/api/:path*",
    "/diagram/:path*",
    "/taskdef/:path*",
    "/usertaskdef/:path*",
    "/externaleventdef/:path*",
    "/token-example",
  ],
};
