import createClient from "openapi-fetch";

import type { paths } from "@/lib/backend/apiV1/schema";

const client = createClient<paths>({
  baseUrl: process.env.NEXT_PUBLIC_API_BASE_URL,
  credentials: "include",
});

export default client;
