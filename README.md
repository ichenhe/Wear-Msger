# Wear Msger

[ ![Download](https://api.bintray.com/packages/liangchenhe55/maven/wear-msger/images/download.svg) ](https://bintray.com/liangchenhe55/maven/wear-msger/_latestVersion)

**[English](#english)**

## 概述

WearMsger 是 [WearTools](https://github.com/liangchenhe55/WearTools) 的升级版， 二次封装了 [Ticear提供的SDK](https://bintray.com/ticwear/maven/mobvoi-api) 以及 Google WearOS API，能够兼容 WearOS、WearOS China、Ticwear 系统。下载量超10万的[腕间图库](http://wg.chenhe.cc/)使用的即是此库。

弱弱地求个Star★(*￣3￣)╭ 

使用 WearMsger ，可以大幅简化手表与手机的通讯代码，你只需关注业务逻辑而不必将大量精力放在传输的维护上。本库提供了不同系统不同设备下统一的API，手表与手机可以互为发送方与接收方并且不需要编写不同的代码。

## 特性

- 兼容多系统。（WearOS、WearOS China、Ticwear）
- 提供全局静态函数便于发送。
- 使用协程等现代化技术。
- 支持 Request/Response 模型。
- 支持超时返回。
version
## 依赖

> 请最低使用 `1.0.3`，更低的版本因为 ProGuard 配置错误无法找到所需的类。

1. 添加依赖。

   在 Module 的 build.gradle 中添加 WearTools 的依赖：`implementation "cc.chenhe:wear-msger:{version}"`
   如果正在使用 Android studio 3.0 以下版本，请把 `implementation` 替换为 `compile`.

2. 删除多余依赖。

   你没必要再添加 `com.ticwear:mobvoi-api` 或 `com.google.android.gms:play-services-wearable` 的依赖项，其他需求除外。


### 具体使用方法见 [Wiki](https://github.com/liangchenhe55/Wear-Msger/wiki)

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

   Add dependence in Module's build.gradle: `implementation "cc.chenhe:wear-msger:{version}"`.
   If you are using Android studio below 3.0，please replace `implementation` with `compile`.

2. Delete redundant dependencies.

   You do not need depend `com.ticwear:mobvoi-api` or `com.google.android.gms:play-services-wearable` again except other uses, because the two libraries were already included on the project.


### For the detail of usage, see [Wiki](https://github.com/liangchenhe55/Wear-Msger/wiki).

## Different from WearTools

- The new API design is easier to use.
- No more rely on Mobvoi SDK internal implementations about GMS compatibility to update the Play Service version.
- Written by Kotlin, using coroutines instead of callback, the continuous request code is more concise.
