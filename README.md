# YouTube SEO Tags & Thumbnail Toolkit

Spring Boot 3 (Java 17, Maven) backend + React (Vite, Tailwind CSS, Bootstrap Icons) frontend.
Integrates with YouTube Data API v3.

## Known limitation (read before filing a bug)
YouTube deprecated the `relatedToVideoId` search parameter. "Related videos" here means the
next-best search results for the same title query — not YouTube's internal recommendation graph.
If you need true relatedness, the only realistic path is scraping the watch page, which violates
YouTube's ToS. Don't build on that.

## 1. Get a YouTube Data API v3 key
1. Go to https://console.cloud.google.com/apis/credentials
2. Create a project, enable "YouTube Data API v3"
3. Create an API key, restrict it to that API

## 2. Run the backend
```bash
cd backend
export YOUTUBE_API_KEY=your_key_here
./mvnw spring-boot:run
```
Backend runs on `http://localhost:8080`. The key is never sent to the browser — the frontend
only talks to your backend, which proxies YouTube calls server-side.

## 3. Run the frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173` and proxies `/api/*` to the backend (see `vite.config.js`).

## Routes
| Path | Page |
|---|---|
| `/`, `/home` | SEO Tag Generator |
| `/video-details` | Video Data Retriever |
| `/thumbnail` | Thumbnail Generator |

## API endpoints
- `GET /api/seo-tags?title=...&relatedCount=5`
- `GET /api/seo-tags/keywords?seed=...&maxResults=10`
- `GET /api/video/details?input=<url-or-id>`
- `GET /api/thumbnail?input=<url-or-id>`
- `GET /api/thumbnail/download?input=<url-or-id>&quality=default|medium|high|standard|maxres`

## Production build
```bash
cd frontend && npm run build
# copy frontend/dist into backend/src/main/resources/static for a single-jar deploy,
# or serve dist/ separately behind nginx and keep CORS origins updated.
```

## Not yet built (you flagged these as remaining)
- Export TXT: **done** — SEO Tag Generator has Export TXT buttons per tag set (client-side, no backend change needed).
- Multi-resolution thumbnail download picker: **done** — Thumbnail Generator page, 5 quality tiers.
- Dark/light ThemeContext: **done** — `src/context/ThemeContext.jsx`, toggle in navbar, persisted to localStorage.
