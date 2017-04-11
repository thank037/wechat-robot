#  微信web协议分析和实现微信机器人（微信网页版 wx2.qq.com）
序言:
	在QQ群里突然看到一个叫QQ小冰的机器人, 在群里只要@他 就会出来跟你聊天. 
可以讲笑话, 查天气等功能, 跟人聊天的语义理解也非常智能. 类似的还有美拍的小冰.

于是去找了相关的机器人, 有微软小冰, 茉莉机器人, 图灵机器人等...
还有些是收费的. 果断跳过

关于实现, 发现现有很多机器人都有API, 提供第三方接入. 
当然微信和QQ也支持. 
通过微信公众号接入机器人或关注机器人好友都可以快速实现与机器人聊天.
对于这种接入, 我还试着去注册了微信公众号. 发现并不是我想要的.

首先, 不想让机器人作为一个公众号, 我希望他的消息出现在好友对话列表, 而非订阅号列表中.
而且要支持群聊. 
其次, 我希望作为一个开发者, 能够自由的为这个机器人写出想要的功能.

参考:
挖掘微信Web版通信的全过程[http://www.tanhao.me/talk/1466.html/]
Python版本[https://github.com/Urinx/WeixinBot]
java版本[https://github.com/biezhi/wechat-robot]
看了网上的一些实现后, 不得不说, Python的版本很多, 而且功能普遍要比java的完善.
java版本的还有部分bug. 这里我参考了这个版本. 
除了修正部分bug之外, 根据自己想法, 又加入了如下功能:
修复bug:
	1. 对群聊中的消息判断不准确.(WechatServiceImpl --> handleMsg()) 

新增功能:
	1. 机器人调用变为图灵机器人(原来是茉莉机器人)
	2. 群聊中被@回复消息
	3. 增加给特定用户定时发送问候语(主动推送)
	4. 在定时发送功能中增加金山和茉莉机器人的API调用
		a. 金山API(获取每日一句英语)
		b. 茉莉机器人(获取当天当地天气信息)
	5. 增加Emoji表情, 并随机发送
	6. 程序处理"图灵机器人"消息内容的水印
	7. 增加消息防撤回(识别撤回消息并保存到消息字典)
	8. 增加语义处理(趣味回答, 口头禅等...)
	9. 完善控制台和记录文件的LOGGER日志, 方便日后维护及调试
	10. 调用API异常的处理(例如茉莉机器人的接口有时很不稳定, 为了不影响功能, 增加备用接口处理异常)
TODO:
	1. 增加发送图片和语音的功能.
	2. 如何不依赖手机端, 程序出现异常后重新选择线路
	3. 增强程序稳定性


## 执行流程

```sh
       +--------------+     +---------------+   +---------------+
       |              |     |               |   |               |
       |   Get UUID   |     |  Get Contact  |   | Status Notify |
       |              |     |               |   |               |
       +-------+------+     +-------^-------+   +-------^-------+
               |                    |                   |
               |                    +-------+  +--------+
               |                            |  |
       +-------v------+               +-----+--+------+      +--------------+
       |              |               |               |      |              |
       |  Get QRCode  |               |  Weixin Init  +------>  Sync Check  <----+
       |              |               |               |      |              |    |
       +-------+------+               +-------^-------+      +-------+------+    |
               |                              |                      |           |
               |                              |                      +-----------+
               |                              |                      |
       +-------v------+               +-------+--------+     +-------v-------+
       |              | Confirm Login |                |     |               |
+------>    Login     +---------------> New Login Page |     |  Weixin Sync  |
|      |              |               |                |     |               |
|      +------+-------+               +----------------+     +---------------+
|             |
|QRCode Scaned|
+-------------+
```

## WebWechat API

### 1. 获取会话ID（Get UUID）
```
URL: https://login.wx.qq.com/jslogin
请求方式: GET
参数: 
	a. appid: wx782c26e4c19acffb(固定字符串)
	b. fun: new(固定值)
	c. lang: zh_CN(固定值)
	d. _: 1491804797(13位毫秒时间戳)
返回数据(String):
window.QRLogin.code = 200; window.QRLogin.uuid = "[UUID]"
状态码code=200表示成功
```

### 2. 显示二维码图片（Get QRCode）
```
URL: https://login.weixin.qq.com/qrcode/[UUID] (上一步获取到的返回值window.QRLogin.uuid)
请求方式: GET
返回数据: 二维码
```

### 3. 手机端扫描二维码等待确认登录
```
URL: https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login
请求方式: GET
参数: 
	a. uuid: [UUID](前面获取到的UUID)
	b. tip: 1 (1-未扫描  0-已扫描)
	d. _: 1491804797(13位毫秒时间戳)
返回数据(String):
window.code=xxx(408 登陆超时, 201 扫描成功但未确认, 200 确认登录)
由于该请求需要用户在手机端连续做几个操作, 所以代码里要轮询来实现. 直到返回结果为200.
获取到以下URL后需要继续访问当前链接获取wxuin和wxsid
window.redirect_uri="https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=xxx&uuid=xxx&lang=xxx&scan=xxx";
```
后面还有一些步骤，麻烦不想写了啊
大致步骤是初始化微信, 开启状态通知, 保存个人信息, 登录信息, 并将联系人列表和群组列表保存下来. 
然后选择同步线路, 轮询进行消息检查. 获取到最新消息后调用机器人API(这里我用的是图灵机器人)获得回答结果.
然后调用消息发送API, 完成消息发送. 

相关的通信过程和API网上有很多. 在开头参考中有推荐


方便开发, 加几个附注:

1.同步状态
在同步消息检查的API中:https://webpush2.weixin.qq.com/cgi-bin/mmwebwx-bin/synccheck
为了模拟实时消息的更新, 在程序中轮询2秒检查一次, 此接口的返回值如下:
window.synccheck={retcode:"xxx",selector:"xxx"}
第一步判断: retcode
	0-正常
	1100-失败/登出微信
第二步判断: selector
	0 正常
	2/6 新的消息
	7 进入/离开聊天界面
 
所以当selector=2/6时, 我们就可以进行消息处理.
这里selector有个很奇怪的返回值, 就是3! 我翻阅各种API也没找到为什么有时会返回3导致程序暴死.


2.消息账户类型
在发送消息之前, 需要获取同步消息.
URL: https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsync?sid=xxx&skey=xxx&pass_ticket=xxx
返回值包括了消息发送方, 接收方, 消息内容, 消息类型.
消息来源的账号类型大致有这几类:
来自个人: 以@开头
来自群聊: 以@@开头
来自公众号/服务号: 以@开头，VerifyFlag & 8 != 0  
来自特殊账号: 
```java
// 特殊用户 须过滤
("newsapp", "fmessage", "filehelper", "weibo", "qqmail", "fmessage", "tmessage", "qmessage",
 "qqsync", "floatbottle", "lbsapp", "shakeapp", "medianote", "qqfriend", "readerapp", "blogapp",
 "facebookapp", "masssendapp", "meishiapp", "feedsapp", "voip", "blogappweixin", "weixin", "wxitil",
"brandsessionholder", "weixinreminder", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c", "officialaccounts",
"notification_messages", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c",  "userexperience_alarm");
```

3.消息类型
<br>
| MsgType | 说明 |
| ------- | --- |
| 1  | 文本消息 |
| 3  | 图片消息 |
| 34 | 语音消息 |
| 37 | VERIFYMSG |
| 40 | POSSIBLEFRIEND_MSG |
| 42 | 共享名片 |
| 43 | 视频通话消息 |
| 47 | 动画表情 |
| 48 | 位置消息 |
| 49 | 分享链接 |
| 50 | VOIPMSG |
| 51 | 微信初始化消息 |
| 52 | VOIPNOTIFY |
| 53 | VOIPINVITE |
| 62 | 小视频 |
| 9999 | SYSNOTICE |
| 10000 | 系统消息 |
| 10002 | 撤回消息 |
<br>


