## Sonarqube 적용기 (2/2)

> 수정 중 

이전 문서였던 [Sonarqube 적용기 1](https://github.com/ksu3101/TIL/blob/master/Android/210119_android.md)에 이어 code coverage를 적용 하기 위해 jacoco를 설정 해보자. 

소나큐드에 작성한 코드에 대한 테스트 코드의 커버리지를 전달하기 위해서는 리포트 파일을 생성해 주어야 한다. 이번에는 gradle task에 jacoco를 추가 및 설정 하여 코드 커버리지를 측정 하고 gradle task를 실행하여 이 측정된 리포트를 소나큐브 서비스에 전달 할 것 이다. 

### 1. gradle 설정

프로젝트의 root `build.gradle`에 아래와 같이 jacoco설정과 task을 추가 한다. 

```gradle
apply plugin: "jacoco"

jacoco {
    // xml report 파일 생성 위치 
	reportsDir = file("${buildDir}/reports")
}

// task 설정
task coverageReport(type: JacocoReport, dependsOn: 'testDebugUnitTest') {
	group = "Reporting"
	description = "Generate Jacoco coverage reports"

	def coverageSourceDirs = ['src/']
	classDirectories.from = fileTree(
			dir: "${buildDir}/intermediates/classes/dev/debug",
			excludes: ['**/R.class',
					   '**/R$*.class',
					   '**/BuildConfig.*',
					   '**/Manifest*.*',
					   'com/android/**/*.class']
	)

	sourceDirectories.from = files(coverageSourceDirs)
	executionData .from= files("${buildDir}/jacoco/testDebugUnitTest.exec")

	reports {
		xml.enabled = true
		html.enabled = true
	}
}
```

위 설정은 kotlin을 대상으로 설정 된 task 이다. 

### 2. task 실행 

### 3. 결과 