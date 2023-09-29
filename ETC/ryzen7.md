## 라이젠 7 시리즈 언더볼팅 값 (asus 기준)

||실사 전력|오프셋|설정값|온도 제한|전압 네거티브|
|---|---|---|---|---|---|
|7600x|90w|0|AUTO|90|20|
|7700x|120w|20w|100|90|30|
|7900x|150w|15w|145|90|30|

- 7700x UEFI bios
  - (CPU 내장 그래픽 비활성화) Advanced > NB Config > Integrated Graphics -> Disabled
  - Ai Tweaker > Precision Boost Overdrive(PBO) -> Manual -> PPT Limit -> `100` 입력
  - (최대 온도 제한) Platform Termal Throttle Limit -> `90` 입력
  - Curve Optimizer -> All Cores -> All Core Curve Optimizer Sign -> `Negative` 선택 -> All Core Curve Optimizer Magnitude -> `30` 입력

- 참고 : [체험판 유튜브 - 라이젠 7천번대 세팅 무작정 따라하기 - 아수스 편](https://youtu.be/td9Oxx-hCKc)
