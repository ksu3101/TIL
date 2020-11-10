## Signature클래스를 이용한 데이터 확인

> 이 글은 Enrique López-Mañas의 [Using the Signature class to verify data](https://medium.com/snapp-mobile/using-the-signature-class-to-verify-data-ff1add1da348)을 번역 하였다. 

정보들을 전달 받을때 우리는 종종 데이터의 출처가 올바른지 확인할 필요가 있다. 이는 올바른 클라이언트가 우리의 리소스들에 액세스 할 수 있도록 하는데 사용할 수 있다. 

예를 들어, 승인된 기기가 백엔드의 민감한 정보가 포함 된 파일을 쿼리하는지 확인하고 싶다고 가정해 보도록 하자. 이에 대한 즉각적인 해결방법은 장치에서 `X-Api-Token`을 사용하는 것 이다. 이에 대해서는 [이전에 글쓴이가 작성](https://medium.com/google-developer-experts/a-follow-up-on-how-to-store-tokens-securely-in-android-e84ac5f15f17) 한 적이 있다. Android에서 토큰을 안전하게 저장할 수 있는 방법에 대해서 설명하자면, 이상적으로 X-Api-Token을 일반 텍스트로 저장해서는 안된다. Android앱에서 일반 텍스트로 제공되는 모든 것 은 오픈 소스로 간주 될 수 있기 때문이다. 이를 비틀어보면 로컬에서 토큰을 생성하는 기능을 갖게 됨을 말하며 서버에서도 이를 알고 있다는 점 이다. 

그러나 실제로 구체적인 리소스에 연결하여 추가 보안 계층을 제공 할 수 있다. 개념적으로, XYZ파일에 접근 하려면 그에 따라 파일에 접근하기 위한 버리피케이션을 생성한다. 이는 X-Api-Token과 함께 사용 할 수 있다. Android에서 이를 수행하려면 Signature클래스를 사용하여 서명을 만들 수 있다. 

Android의 `Signature`클래스는 Android의 히스토리를 통해 몇가지 추가 사항들이 있었지만 첫번째 버전부터 존재 해 왔었다. 특히 API레벨 23이후로 SHA512 with RSA/PSS를 포함한 몇 가지 암호화 알고리즘에 접근 할 수 있다. SHA512withRSA/PSS는 대부분의 상업용 어플리케이션만큼 안전하다. 

이 알고리즘은 512비트의 다이제스트 길이에서 서명을 생성한다. PSS는 [Probabilistic Signature Scheme](https://web.archive.org/web/20040713140300/http://grouper.ieee.org/groups/1363/P1363a/contributions/pss-submission.pdf)을 말한다. RSA는 그 자체가 암호화 알고리즘이 아니라 코드를 모호하게 하는 순열 계열이지만 보안 격리에서는 유용하지 않다. PSS는 암호화 스키마를 추가 해 준다. 

안드로이드는 실제로 PKCS1을 지원하지 않으므로 먼저 PKCS키를 생성 해야 한다. 이는 openssl을 사용하면 쉽게 사용할 수 있다:  

`openssl genpkey -out rsakey.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048`

PKCS키는 이미 생성되어 있으므로 이를 장치로 전달 해야 한다. 한가지 옵션은 assets폴더에 포함 된 비공개 키로 APK를 제공하는 것 이지만 이 또한 오픈 소스로 간주되어 외부에 노출 될 수 있다. 해결방법은 SSL을 통해 전달 하는 것 이다. 이는 오류가 없는 해결방법이 아니라 약간의 위험을 제거 한다. 

이 작업이 이미 완료되었다면 키에서 몇가지를 설정해주어야 한다. 먼저 `BEGIN PRIVATE KEY`와 `END PRIVATE KYE`를 제거 해 주어야 한다. 그렇지 않으면 Signature클래스가 작동하지 않는다. 그리고 아래처럼 서명을 할 수 있다. 

```kotlin
val signatureSHA256 = Signature.getInstance("SHA256withRSA/PSS")
signatureSHA256.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))

val privateKey = loadPrivateKey("MY_KEY")
signatureSHA256.initSign(privateKey)

val data: ByteArray = "name_of_the_file".toByteArray()
signatureSHA256.update(data)

var finalSignature = signatureSHA256.sign()
```

키 에서 관련없는 컨텐츠를 제거 하는 방법은 아래와 같다. 

```kotlin
private fun loadPrivateKey(key: String): PrivateKey {
    val readString = key.replace("-----BEGIN PRIVATE KEY-----\n", "")
        .replace("-----END PRIVATE KEY-----", "")
    val encoded = Base64.decode(readString, Base64.DEFAULT)
    return KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(encoded))
}
```

또 다른 비트는 방법으로서 결과 서명을 16진수의 코드로 변환 하는 방법이 있다. 안드로이드에서는 Kotlin확장으로 쉽게 할 수 있다: 

```kotlin
fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
```

이제 모든 준비가 되었다. 접근하려는 파일과 이와 관련된 서명을 지정하여 서버에 요청을 보낼 수 있다. 서버에서는 반대로 절차를 수행 해야 한다. (16진수에서 텍스트로 변환한 다음 서명을 확인). 요청은 아래와 같다. 

![request params](https://miro.medium.com/max/700/1*fLlzBI0NXea94VkuXKwlEQ.png)

이제 백엔드에서 서명을 확인하고 데이터가 제대로 확인되었는지 확인 해야 한다. 이전 코드에서 몇가지 변경 사항이 있다. 기본적으로 이번에는 공개 키로 작업하고 `verify(data)` 메소드를 사용하여 궁극적으로 데이터가 적절한 클라이언트에서 전송되었는지 확인해 볼 수 있다. 

```kotlin
val signatureSHA256 = Signature.getInstance("SHA256withRSA/PSS");
signatureSHA256.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));

val privateKey = loadPublicKey("MY_KEY")
signatureSHA256.initSign(privateKey);

val data: ByteArray = "name_of_the_file".toByteArray()
signatureSHA256Java.verify(data)
```

### The extra mile

다운로드 받은 인증서는 [Charles](https://www.charlesproxy.com/)와 같은 도구를 사용하는 잠재적인 공격자가 쉽게 가로챌 수 있다. 고정된 인증서를 완전히 우회하는 [Objection](https://github.com/sensepost/objection)이라는 라이브러리도 있다. 이를 쉽고 확실하게 피할수 있는 방법이 있다. `network_security_config.xml`파일을 추가 하기만 하면 된다:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config>
        <domain includeSubdomains="true">example.com</domain>
        <pin-set expiration="2018-01-01">
            <pin digest="SHA-256">7HIpactkIAq2Y49orFOOQKurWxmmSFZhBCoQYcRhJ3Y=</pin>
            <!-- backup pin -->
            <pin digest="SHA-256">fwza0LRMXouZHRC8Ei+4PyuldPDcf3UKgO/04cDM1oE=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

안드로이드는 기기에서 공개/비공개 키 쌍을 생성할 수 있다. 몰론 이것들은 처리 되어야 하지만, 또 다른 가능성이 발생한다. 이는 키가 실시간으로 생성된다면 더 안전하다는 말이 된다. 몰론 여전히 키 전송 및 보안 문제를 처리하고 있지만 몇가지 실제하는 위협을 제거 했을때 해당 된다. 

```kotlin
private lateinit var keyPair: KeyPair

private fun generateKey() {
    val startDate = GregorianCalendar()
    val endDate = GregorianCalendar()
    endDate.add(Calendar.YEAR, 1)
    
    val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)

    val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_ALIAS,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).run {
            setCertificateSerialNumber(BigInteger.valueOf(777))       //Serial number used for the self-signed certificate of the generated key pair, default is 1
            setCertificateSubject(X500Principal("CN=$KEY_ALIAS"))     //Subject used for the self-signed certificate of the generated key pair, default is CN=fake
            setDigests(KeyProperties.DIGEST_SHA256)                         //Set of digests algorithms with which the key can be used
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1) //Set of padding schemes with which the key can be used when signing/verifying
            setCertificateNotBefore(startDate.time)                         //Start of the validity period for the self-signed certificate of the generated, default Jan 1 1970
            setCertificateNotAfter(endDate.time)                            //End of the validity period for the self-signed certificate of the generated key, default Jan 1 2048
            setUserAuthenticationRequired(true)                             //Sets whether this key is authorized to be used only if the user has been authenticated, default false
            setUserAuthenticationValidityDurationSeconds(30)                //Duration(seconds) for which this key is authorized to be used after the user is successfully authenticated
            build()
    }

    //Initialization of key generator with the parameters we have specified above
    keyPairGenerator.initialize(parameterSpec)

    //Generates the key pair
    keyPair = keyPairGenerator.genKeyPair()
}
```

### Conclusions

이전 글쓴이의 기사에서 언급했듯이 절대적인 보안은 존재하지 않는다. 그래서 불가피한 보안 침해 프로세스를 늦추는것 을 목표로 하고 있으며 대부분의 시스템에서 100%의 보안을 달성하기 위해서는 각종 자원들을 무제한으로 투입할 수 없다. 그에 대해 항상 절충안이 존재한다. 합리적인 수준의 보호를 보장하기 위해 합리적인 자원을 투자 해야 할 것 이다. 

