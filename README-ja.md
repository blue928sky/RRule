[English](README.md) | [日本語](README-ja.md)

# RRule

Kotlin で iCalendar (RFC 5545) の繰り返しルールを簡単に処理するように実装した Repository。

## 前提条件

- 元々、Android のプロジェクトで使用していたものを切り離して作成しているため、[`build-logic`](https://github.com/blue928sky/RRule/tree/main/build-logic) 周りはAndroidのような設定になっていますが、Android に依存しないように切り出しているので JVM でも利用できるはずです。
- Android のプロジェクトで必要だったのは日付に関する値であったため、時間などの `BYHOUR` や使用していない `COUNT` は現在サポートされていません。

## 使用例

```kt
val rrule = RRule("RRULE:FREQ=MONTHLY;INTERVAL=3;BYDAY=SU;BYSETPOS=3")

rrule.freq
rrule.interval
rrule.byDay
rrule.byMonth
rrule.byMonthDay
rrule.bySetPos
// ...
```

RRule を iCalendar (RFC 5545) 文字列に変換。

```kt
val rfc5545String = rrule.toRFC5545String()
```

## テストの実行

### Unitテスト

```sh
./gradlew test
```

### Spotlessテスト

```sh
./gradlew spotlessCheck
```
