# blog-planet

Spring Boot 3.x + Java 21 + Gradle + H2 + Vue 3 + TypeScript 기반의 기술 블로그 수집 및 알림 프로젝트입니다.

## 기술 스택

- Java 21
- Spring Boot 3.5.x
- Gradle
- H2 Database
- Vue 3
- TypeScript
- Vite

## 실행 전 준비

아래 도구가 로컬에 설치되어 있어야 합니다.

- JDK 21
- Node.js
- npm

버전 확인 예시:

```powershell
java -version
node -v
npm -v
```

## 로컬 실행

기본 실행 방식은 Spring Boot 애플리케이션을 기동하는 것입니다.  
프론트엔드 빌드는 Gradle 과정에 연결되어 있어, 백엔드를 실행하면 Vue 정적 리소스도 함께 빌드됩니다.

```powershell
cd /path/to/blog-planet
.\gradlew.bat bootRun
```

실행 후 확인 URL:

- 메인 화면: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- Actuator Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## 프론트엔드만 개발 실행

UI 작업만 빠르게 확인하고 싶다면 Vite 개발 서버를 따로 실행할 수 있습니다.

```powershell
cd /path/to/blog-planet/frontend
npm install
npm run dev
```

접속 URL:

- Vite Dev Server: [http://localhost:5173](http://localhost:5173)

주의:

- 현재 운영 기준의 기본 실행 방식은 `Spring Boot` 단일 실행입니다.
- 프론트엔드 개발 서버는 UI 개발 편의를 위한 용도입니다.

## 테스트 실행

전체 테스트 실행:

```powershell
cd /path/to/blog-planet
.\gradlew.bat test
```

## 주요 환경변수

로컬 기본값이 `application.yml`에 들어 있으므로, 필요한 경우에만 환경변수로 덮어쓰면 됩니다.

### PowerShell 예시

```powershell
$env:BLOG_PLANET_H2_FILE_PATH = "./data/blog-planet-local"
$env:BLOG_PLANET_FEED_POLLING_INTERVAL = "15m"
$env:BLOG_PLANET_FEED_CONNECT_TIMEOUT = "5s"
$env:BLOG_PLANET_FEED_READ_TIMEOUT = "10s"
$env:BLOG_PLANET_NOTIFICATION_DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/..."
.\gradlew.bat bootRun
```

### 지원 환경변수

- `BLOG_PLANET_H2_FILE_PATH`
- `BLOG_PLANET_FEED_POLLING_INTERVAL`
- `BLOG_PLANET_FEED_CONNECT_TIMEOUT`
- `BLOG_PLANET_FEED_READ_TIMEOUT`
- `BLOG_PLANET_NOTIFICATION_DISCORD_WEBHOOK_URL`

## H2 설정 안내

기본 H2 JDBC URL:

```text
jdbc:h2:file:./data/blog-planet;AUTO_SERVER=TRUE
```

기본 파일 경로:

```text
./data/blog-planet
```

즉, 로컬 실행 후 프로젝트 하위 `data` 디렉토리에 H2 파일이 생성됩니다.

H2 Console 접속 예시:

- JDBC URL: `jdbc:h2:file:./data/blog-planet;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: 비움

## 샘플 데이터

샘플 데이터는 기본적으로 비활성화되어 있습니다.

```yaml
blog-planet:
  sample-data:
    enabled: false
```

필요하면 `application.yml` 또는 환경별 설정에서 활성화할 수 있습니다.

## 빌드 결과

프론트엔드 빌드 결과는 Gradle 실행 중 정적 리소스로 복사되어 Spring Boot에서 함께 서빙됩니다.

관련 흐름:

- `npmInstall`
- `npmBuild`
- `syncFrontendAssets`
- `processResources`
