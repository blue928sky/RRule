[English](README.md) | [日本語](README-ja.md)

# RRule

Repository implemented in Kotlin to easily handle iCalendar (RFC 5545) repetition rules.

## Prerequisites

- The build-logic around the [`build-logic`](https://github.com/blue928sky/RRule/tree/main/build-logic) is set up like Android's because it was originally created in a detached Android project, but it is cut out to be independent of Android, so it should be usable in the JVM as well.
- The `BYHOUR` and unused `COUNT`, such as time, are currently not supported, since what was needed in the Android project were values related to dates.

## Usage Sample

```kotlin
val rrule = RRule("RRULE:FREQ=MONTHLY;INTERVAL=3;BYDAY=SU;BYSETPOS=3")

rrule.freq
rrule.interval
rrule.byDay
rrule.byMonth
rrule.byMonthDay
rrule.bySetPos
// ...
```

Convert RRule to iCalendar (RFC 5545) string.

```kotlin
val rfc5545String = rrule.toRFC5545String()
```

## Running the tests

### Unit test

```bat
./gradlew test
```

### Spotless test

```bat
./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache
```

