# 那样记账

<img alt='那样记账' src='https://i.loli.net/2018/06/27/5b33a4e2e7cb0.png' height="70"/>

>生活越复杂，记账越有用。

## 诞生记

为个人提供一款轻量的记账产品。个人开发的应用，开始是自用，后来觉得也许有人也和我一样，喜欢简单、纯粹的记账方式，所以就发布了出来。让大家以轻松的方式来记账，而不是面面俱到，个人记账不必要专业化。

没有任何多余的权限，没有广告，没有用户系统，不保存用户任何信息，只是单纯的记录收支情况，做一些明了的统计，通过清楚的了解钱花在了哪，来建立起良好的消费习惯。

## 特点

- 【快速记账】：多种直接记账方式
- 【自定义】：自定义收支分类、记账时间、随时修改
- 【多维度统计】：多维度统计收支流水
- 【预算功能】：控制消费，减少开支
- 【云端备份】：本地备份/云备份数据，保证数据不丢失
- 【剩余资产】：展示净资产金额

## 下载

发布在以下平台：

| 平台 | 下载地址 |
| ---- | ---- |
| Google Play | <a href='https://play.google.com/store/apps/details?id=me.bakumon.moneykeeper&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://i.loli.net/2018/06/27/5b32eac49f930.png' height="60"/>
</a> |
| 酷安 | <a href='https://www.coolapk.com/apk/me.bakumon.moneykeeper'><img alt='去酷安下载' src='https://ws4.sinaimg.cn/large/006tNc79ly1fsphx16ybdj30go06st8q.jpg' height="60"/>
</a> |

预览：
![img.png](https://ws3.sinaimg.cn/large/006tNc79ly1fsp159i5gxj31kw0sgwl3.jpg)
![img.png](https://ws2.sinaimg.cn/large/006tNc79ly1ft2ct8vnk7j31kw0sgtum.jpg)

## 架构和技术

### 架构

架构使用了 Google 官方的 [Architecture Components](https://developer.android.com/topic/libraries/architecture/)，包括 Lifecycle、LiveData 和 ViewModel，数据库使用 [Room](https://developer.android.com/topic/libraries/architecture/room)。
这里有一篇介绍 Architecture Components 的[文章](https://medium.com/google-developers/viewmodels-and-livedata-patterns-antipatterns-21efaef74a54)。


![Architecture Components](https://ws1.sinaimg.cn/large/006tKfTcly1fs7957cwd7j31di0gumxz.jpg)

### 其他技术

- AES 加密
- WebDAV

## 开源库

- [[ProcessPhoenix]-JakeWharton](https://github.com/JakeWharton/ProcessPhoenix)
- [[java-aes-crypto]-tozny](https://github.com/tozny/java-aes-crypto)
- [[Cipher.so]-MEiDIK](https://github.com/MEiDIK/Cipher.so)
- [[okhttp-digest]-rburgst](https://github.com/rburgst/okhttp-digest)
- [[android-storage]-sromku](https://github.com/sromku/android-storage)
- [[prettytime]-ocpsoft](https://github.com/ocpsoft/prettytime)
- [[Floo]-drakeet](https://github.com/drakeet/Floo)

## 相关链接：

1. [App下载地址](https://www.coolapk.com/apk/188475)
2. [设计稿](https://www.zcool.com.cn/work/ZMjExOTI4OTY=.html)
3. [Architecture Components](https://developer.android.com/topic/libraries/architecture/)
4. [Room](https://developer.android.com/topic/libraries/architecture/room)

## License

```
Copyright 2018 Bakumon. https://github.com/Bakumon

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
 limitations under the License.
 ```
