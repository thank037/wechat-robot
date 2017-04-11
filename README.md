# wechat-robot

修复bug:
	1. 对群聊中的消息判断不准确.(WechatServiceImpl --> handleMsg()) 
 
新增功能:
	1. 机器人调用变为图灵机器人(原来是茉莉机器人)
	2. 群聊中被@回复消息
	3. 增加给特定用户定时发送问候语
	4. 在定时发送功能中增加金山和茉莉机器人的API调用
		a. 金山API(获取每日一句英语)
		b. 茉莉机器人(获取当天当地天气信息)
	5. 增加emoji表情, 并随机发送
	6. 程序处理"图灵机器人"消息内容的水印
	7. 增加消息防撤回(个人和群聊中的文字信息)
	8. 完善控制台和文件的LOGGER日志, 方便日后维护及调试