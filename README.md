# Wear Msger

[![Download](https://img.shields.io/maven-central/v/me.chenhe/wearmsger?style=flat-square)](https://search.maven.org/artifact/me.chenhe/wearmsger) ![](https://img.shields.io/github/license/ichenhe/wear-msger?style=flat-square)

> ⚠️ Warning: Since the maintenance of the Ticwear system has been stopped since 2019, only Wear OS will be supported from WearMsger 2.0.0.
>
> ⚠️ 警告：由于 Ticwear 从2019年就已经停止维护，WearMsger 从 2.0.0 版本开始仅支持 Wear OS。

**[English](#english)**

## 概述

WearMsger 是 [WearTools](https://github.com/ichenhe/WearTools) 的升级版， 二次封装了 [Ticear提供的SDK](https://bintray.com/ticwear/maven/mobvoi-api) 以及 Google WearOS API，能够兼容 WearOS、WearOS China、Ticwear 系统。下载量超10万的[腕间图库](http://wg.chenhe.cc/)使用的即是此库。

弱弱地求个Star★(*￣3￣)╭ 

使用 WearMsger ，可以大幅简化手表与手机的通讯代码，你只需关注业务逻辑而不必将大量精力放在传输的维护上。本库提供了不同系统不同设备下统一的API，手表与手机可以互为发送方与接收方并且不需要编写不同的代码。

## 特性

- 兼容多系统。（WearOS、WearOS China、Ticwear）
- 提供全局静态函数便于发送。
- 使用协程等现代化技术。
- 支持 Request/Response 模型。
- 支持超时返回。
## 依赖

> 请最低使用 `1.0.3`，更低的版本因为 ProGuard 配置错误无法找到所需的类。

1. 添加依赖。

   在 Module 的 build.gradle 中添加 WearTools 的依赖：`implementation "me.chenhe:wearmsger:{version}"`
   如果正在使用 Android studio 3.0 以下版本，请把 `implementation` 替换为 `compile`.

2. 额外依赖。(**2.0.0 版本不再需要**)

   由于 JCenter 已经停止维护个人仓库，并且 Ticwear 也已经停止维护。但是 Android Studio 无法对本地依赖的 aar 进行打包。所以请务必手动添加 [mobvoi.aar](https://github.com/ichenhe/Wear-Msger/blob/master/wearmsgerlib/libs/mobvoi-api-1.1.1.aar) 为依赖项。


### 具体使用方法见 [Wiki](https://github.com/liangchenhe55/Wear-Msger/wiki)

## 迁移到 v2

⚠️ V2 仅支持 Wear OS 系统。

- 依赖 id 改为 `me.chenhe:wearmsger`。（v1 版本是 `cc.chenhe:wear-msger`）
- 从 JCenter 迁移到 Maven Central。
- 无需进行 `WH.init()` 初始化。
- 一些兼容性的类已经删除，你应该直接使用 gms 中提供的。

## 与 WearTools 区别

- 新的 API 设计，更方面易用。
- 自主实现 WearOS 兼容，不依赖 Mobvoi SDK 内部实现以便更新 Play Service 版本。
- Kotlin 编写，使用协程取代回调，连续请求代码更简洁。

# English

## Summary

WearMsger is upgraded version of [WearTools](https://github.com/liangchenhe55/WearTools) which secondary encapsulated [Ticear SDK](https://bintray.com/ticwear/maven/mobvoi-api), and Google WearOS API, to be compatible with WearOS, WearOS China, Ticwear. [Wear Gallery](http://wg.chenhe.cc/) which more than 100,000 downloads,  had been using this library for one year+.

Please give me a Star★(*￣3￣)╭ 

WearMsger can dramatically simplify the communication code between wear and mobile. You just need to focus on the business logic and not be focused on the maintenance of the communication. This library provides a unified API for different systems and devices, that means  the watch and phone can be both sender and receiver, and don't have to write different code.

## Feature

- Compatible multisystem. (Andorid Wear/AW China/Ticwear)
- Global static functions are provided to facilitate sending.
- The use of modern technologies such as coroutines.
- Support Request/Response model.
- Support timeout.

## Dependence

> Please use at leate `1.0.3`. Earlier versions cannot find classes because of ProGuard configuration errors.

1. Add dependence.

   Add dependence in Module's build.gradle: `implementation "me.chenhe:wearmsger:{version}"`.
   If you are using Android studio below 3.0，please replace `implementation` with `compile`.

2. Extra dependence. (**v2.0.0 is no longer required**)

   Jcenter has stopped maintaining personal repositories, and Ticwear has stopped maintenance. But Android Studio cannot package local aar dependent to aar. So be sure to manually add [mobvoi.aar](https://github.com/ichenhe/Wear-Msger/blob/master/wearmsgerlib/libs/mobvoi-api-1.1.1.aar) as a dependency.


### For the detail of usage, see [Wiki](https://github.com/liangchenhe55/Wear-Msger/wiki).

## Migrate to v2

⚠️ V2 only support Wear OS system.

- Dependence id has changed to `me.chenhe:wearmsger`. (Old one is `cc.chenhe:wear-msger`)
- Move from JCenter to Maven Central.
- `WH.init()` is no longer needed.
- Some compatible class has been deleted and you should import them from gms.

## Different from WearTools

- The new API design is easier to use.
- No more rely on Mobvoi SDK internal implementations about GMS compatibility to update the Play Service version.
- Written by Kotlin, using coroutines instead of callback, the continuous request code is more concise.
