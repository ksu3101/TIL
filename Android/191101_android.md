# 그동안 만들었었던 확장 함수들 정리

기존 모듈에 패턴을 적용 하지 않고 새로운 기능을 추가 하기 위해서 정말 좋은 방법 중 하나가 코틀린의 확장 함수 이다. 그동안 만들었던 확장 함수들을 정리 해 보았다. 
확장 함수는 편하기는 하지만 여러개발자들이 협업을 하는 경우 오히려 중복코드가 생길 가능성이 높다. 그렇기 때문에 서드파티 라이브러리를 사용 하거나 모듈을 새로 만들고 거기서 따로 관리하는게 좋다. 그렇지 않으면 같은 기능의 확장함수가 여기 저기 모듈에서 존재 할 수 있기 때문이다. 아무리 코드리뷰를 열심히 해도 이전에 내가 만들었던 확장함수를 일일히 기억하지 않는이상 중복 코드가 발생할 수 있으니 다른 방법을 찾아보는것도 좋을 것 같다. 

아래 정리한 내용 외에 더 있는데 회사 업무와 관련된 코드가 많아 관련되지 않은 확장 함수만 정리 하였다. 

## Any 
state, action 들 을 로깅 하기 위해서 만들었던 확장 함수 이다. 

```kotlin
fun Any.getSuperClassNames(): String {
    var currentSuperClazz = this.javaClass.superclass
    if (!isAvailableClass(currentSuperClazz)) {
        return "ERROR_NO_SUPERCLASS"
    }
    val clazzNames = StringBuilder("")
    while (isAvailableClass(currentSuperClazz)) {
        clazzNames.append(currentSuperClazz.simpleName)
        currentSuperClazz = currentSuperClazz.superclass
        if (isAvailableClass(currentSuperClazz)) {
            clazzNames.append(".")
        }
    }
    return clazzNames.toString()
}

private fun isAvailableClass(clazz: Class<*>?): Boolean {
    return (clazz != null && !clazz.isInterface && "Object".notEqual(clazz.simpleName))
}
```

## List 

```kotlin 
fun <E> List<E>?.isNullOrEmpty(): Boolean = (this == null || this.isEmpty())

fun <E> List<E>?.isNotNullOrEmpty(): Boolean = !(this.isNullOrEmpty())

infix fun <E> List<E?>?.whenNotEmptyAndThen(func: (List<E?>) -> Unit) {
    if (isNotNullOrEmpty()) {
        func(this!!)
    }
}

fun <E> List<E>?.`when`(ifNotEmpty: (List<E>) -> Unit, ifEmpty: () -> Unit) {
    if (isNotNullOrEmpty()) {
        ifNotEmpty(this!!)
    } else {
        ifEmpty()
    }
}

fun <E> List<E?>?.availableIndex(position: Int): Boolean {
    if (position < 0 || this == null || this.isEmpty()) return false
    return (position < this.size)
}

fun <E> List<E?>?.getElement(position: Int): E? =
        (if (this.availableIndex(position)) this?.get(position) else null)

fun <E> List<E?>?.getItem(position: Int): E? = this.getElement(position)

infix fun <E> List<E?>?.get(position: Int): E? = getElement(position)

infix fun <E> List<E?>?.hasMoreThan(expectedSize: Int): Boolean = (this != null && this.size > expectedSize)

infix fun <E> List<E?>?.hasMoreThanOrEqualTo(expectedSize: Int): Boolean = (this != null && this.size >= expectedSize)

infix fun <E> List<E?>?.hasLessThan(expectedSize: Int): Boolean = (this != null && this.size < expectedSize)

infix fun <E> List<E?>?.hasLessThanOrEqualTo(expectedSize: Int): Boolean = (this != null && this.size <= expectedSize)

fun <E> List<E?>.filterNotNull(): List<E> = filterNotNullTo(arrayListOf())

fun <E> List<E?>.filterNotNullTo(dest: MutableList<E>): List<E> {
    for (e in this) {
        if (e != null) {
            dest.add(e)
        }
    }
    return dest
}

@CheckResult
fun <E> List<E>.moveItemToFirstPositionIfFoundedByFilter(filter: (E) -> Boolean): List<E> {
    if (this.isEmpty()) return this
    val mutableList = this.toMutableList()
    val foundedItem = mutableList.find(filter)
    if (foundedItem != null) {
        mutableList.remove(foundedItem)
        mutableList.add(0, foundedItem)
    }
    return mutableList.toList()
}

fun <E> List<E>?.isSafeIndex(index: Int): Boolean = (this != null && (index >= 0 && index < this.size))
```

## String

```kotlin
private val REGEX_NORMAL: String = "^[A-Za-z0-9\\s]+"

fun String?.toNonNullString(): String {
    return WmsStringUtils.toNonNullString(this)
}

fun String?.isNotNullOrEmpty(): Boolean {
    return !(this.isNullOrEmpty())
}

fun String?.isNotNullOrBlank(): Boolean {
    return !(this.isNullOrBlank())
}

fun String?.isNumber(): Boolean {
    return if (this.isNullOrEmpty()) {
        false
    } else {
        REGEX_NUMBER.toRegex() matches this!!
    }
}

fun String?.isNotNumber(): Boolean {
    return !(this.isNumber())
}

inline fun String?.convertToInteger(integersFunc: (Int) -> Int): Int {
    this?.toIntOrNull()?.let {
        return integersFunc(it)
    }
    return 0
}

inline fun String?.ifItIntegerString(func: (Int) -> Unit) {
    this?.toIntOrNull()?.let {
        func(it)
    }
}

fun String?.isSpecialCharacter(): Boolean {
    return if (this.isNullOrEmpty()) {
        false
    } else {
        !(REGEX_NORMAL.toRegex() matches this!!)
    }
}

fun String?.appendOf(text: String?): String {
    return if (this == null && text == null) {
        ""
    } else if (this != null && text == null) {
        this
    } else if (this == null && text != null) {
        text
    } else {
        "$this$text"
    }
}

fun String?.deleteLastOnce(): String {
    return if (this == null) {
        return ""
    } else if (this.length == 0) {
        return this
    } else {
        this.substring(0, this.length() - 1)
    }
}

fun String?.getLastCharcter(): String {
    return if (this.isNullOrEmpty()) {
        ""
    } else {
        this!!.substring(this.length() - 1, this.length())
    }
}

fun String?.isLongerThan(targetLength: Int): Boolean {
    return if (this == null || targetLength == 0) {
        return false
    } else {
        this.length() > targetLength
    }
}

fun String?.isLongerThan(target: String?): Boolean {
    return this.isLongerThan(target.length())
}

fun String?.isLongerThanOrEqualTo(targetLength: Int): Boolean {
    return this.length() >= targetLength
}

fun String?.isLongerThanOrEqualTo(target: String?): Boolean {
    return this.isLongerThanOrEqualTo(target.length())
}

fun String?.isShorterThan(targetLength: Int): Boolean {
    return if (this == null || targetLength == 0) {
        false
    } else {
        this.length() < targetLength
    }
}

fun String?.isShorterThan(target: String?): Boolean {
    return this.isShorterThan(target.length())
}

fun String?.isShorterThanOrEqualTo(targetLength: Int): Boolean {
    return this.length() <= targetLength
}

fun String?.isShorterThanOrEqualTo(target: String?): Boolean {
    return this.isShorterThanOrEqualTo(target.length())
}

fun String?.isSameLengthOf(target: String?): Boolean {
    return if (this == null && target == null) {
        true
    } else {
        this.length() == target.length()
    }
}
```

## View 
view 와 관련된 확장 함수. 

```kotlin
fun View.setVisibility(visibility: Boolean) {
    setVisibility(if (visibility) View.VISIBLE else View.GONE)
}

fun View.setVisible() {
    setVisibility(true)
}

fun View.setHide() {
    setVisibility(false)
}

fun View.isVisible(): Boolean = visibility == VISIBLE

@Throws(NullPointerException::class)
fun <T : View> View.findViewByIdNotNullable(@IdRes id: Int): T {
    val view: T? = findViewById(id)
    isNull(view) {
        throw NullPointerException("Resource `$id` is not found in layouts. check layout files.")
    }
    return view!!
}
```
