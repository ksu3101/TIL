## [Claude code](https://claude.com/product/claude-code)

  > curl -fsSL https://claude.ai/install.sh | bash

## 설치 후 해야 할것

- `~/.zshrc` 파일에 
`export ENABLE_EXPERIMENTAL_MCP_CLI=true`
- [Claude-hud](https://github.com/jarrodwatts/claude-hud) 설치
  - `/plugin marketplace add jarrodwatts/claude-hud`
  - `/claude-hud:setup`
  - 필수는 아니지만 현재 토큰 사용량과 남은양, 토큰 리셋 까지 얼마나 남았는지를 알려줘서 편하다.
  - 현재 작업 브랜치, 사용 모델 등 기타 정보도 있고 수정 가능 하다.
- [serena](https://github.com/oraios/serena) MCP 설치
  - `uv` 가 설치 되어 있지 않다면 brew 를 이용해 설치 할 것.
  - 로컬 서버 실행 : `uvx --from git+https://github.com/oraios/serena serena start-mcp-server`
    - 대시보드 페이지 잘 열리는지 확인
    - `/mcp` 명령어로 확인시 실패 했다면 로컬 서버 상태 확인 할 것.
  - MCP 추가 : `claude mcp add serena -- uvx --from git+https://github.com/oraios/serena serena start-mcp-server --context claude-code --project "$(pwd)"`

## 프로젝트에 설정

- `/init` 을 실행 하여 프로젝트 상태 갱신
- MCP나 플러그인 을 클로드 한테 찾아서 설치 하라고 할 수 있긴 하지만 토큰을 많이 소모 하니 될 수 있으면 커맨드 찾아서 터미널에 명령어 입력 해서 하는것 을 추천.
  - 프로젝트에서 `.mcp.json` 파일을 관리 하고 싶다면 (프로젝트 스코프) 클로드에게 프로젝트 단위로 관리하고 싶다고 하면 된다.

## git MCP 설정

### 1. github

- [`gh`](https://cli.github.com/) CLI 를 사용 한다.
  - 설치 후 `gh auth login` 으로 인증
- [MCP 설정](https://github.com/github/github-mcp-server/blob/main/docs/installation-guides/install-claude.md)
  - github 에서 엑세스 토큰을 발급 한다. 
  - docker 사용 하는 경우
    - `claude mcp add github -- docker run -i --rm -e GITHUB_PERSONAL_ACCESS_TOKEN ghcr.io/github/github-mcp-server` 터미널에 입력 한다.
  - 바이너리를 사용 하는 경우 (docker 사용 안함)
    - [바이너리 링크](https://github.com/github/github-mcp-server/releases)
    - 커맨드로 사용 될 해당 바이너리이 경로를 `command` 에 경로 입력 하거나 `PATH` 에 추가 한다. 
  - `.mcp.json` 파일에 아래 처럼 발급받은 엑세스 토큰을 추가 한다.
    ```
    "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "..."
    }
    ```

### 2. gitlab

- `glab` CLI 인증 방식에 따라 토큰을 발급해야 할 수 있다.
