package me.biezhi.wechat.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.DateKit;
import com.blade.kit.StringKit;
import com.blade.kit.http.HttpRequest;
import com.blade.kit.json.JSONArray;
import com.blade.kit.json.JSONKit;
import com.blade.kit.json.JSONObject;

import me.biezhi.wechat.Constant;
import me.biezhi.wechat.exception.WechatException;
import me.biezhi.wechat.model.WechatContact;
import me.biezhi.wechat.model.WechatMeta;
import me.biezhi.wechat.robot.EnOneDayRobot;
import me.biezhi.wechat.robot.Robot;
import me.biezhi.wechat.robot.TuLingRobot;
import me.biezhi.wechat.util.EmojiUtil;
import me.biezhi.wechat.util.Matchers;
import me.biezhi.wechat.util.PingUtil;
import me.biezhi.wechat.util.SemanticUtil;

public class WechatServiceImpl implements WechatService {
 
	private static final Logger LOGGER = LoggerFactory.getLogger(WechatService.class);
	
	// 图灵机器人
	private Robot robot = new TuLingRobot();
	
	//金山机器人
	private Robot jinsRobot = new EnOneDayRobot();
	
	//消息字典(key=userName, value=content)
	private Map<String, String> msgMap = new HashMap<String, String>();
	
	/**
	 * 获取联系人
	 */
	@Override
	public WechatContact getContact(WechatMeta wechatMeta) {
		String url = wechatMeta.getBase_uri() + "/webwxgetcontact?pass_ticket=" + wechatMeta.getPass_ticket() + "&skey="
				+ wechatMeta.getSkey() + "&r=" + DateKit.getCurrentUnixTime();

		JSONObject body = new JSONObject();
		body.put("BaseRequest", wechatMeta.getBaseRequest());

		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8")
				.header("Cookie", wechatMeta.getCookie()).send(body.toString());
		
		LOGGER.debug(request.toString());
		String res = request.body();
		request.disconnect();

		if (StringKit.isBlank(res)) {
			throw new WechatException("获取联系人失败");
		}
		
		LOGGER.debug(res);
		
		WechatContact wechatContact = new WechatContact();
		try {
			JSONObject jsonObject = JSONKit.parseObject(res);
			JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
			if (null != BaseResponse) {
				int ret = BaseResponse.getInt("Ret", -1);
				if (ret == 0) {
					JSONArray memberList = jsonObject.get("MemberList").asArray();
					JSONArray contactList = new JSONArray();
					
					//将以下四类成员放入 联系人列表中
					if (null != memberList) {
						for (int i = 0, len = memberList.size(); i < len; i++) {
							JSONObject contact = memberList.get(i).asJSONObject();
							// 公众号/服务号
							if (contact.getInt("VerifyFlag", 0) == 8) {
								continue;
							}
							// 特殊联系人
							if (Constant.FILTER_USERS.contains(contact.getString("UserName"))) {
								continue;
							}
							// 群聊
							if (contact.getString("UserName").indexOf("@@") != -1) {
								continue;
							}
							// 自己
							if (contact.getString("UserName").equals(wechatMeta.getUser().getString("UserName"))) {
								continue;
							}
							contactList.add(contact);
						}

						wechatContact.setContactList(contactList);
						wechatContact.setMemberList(memberList);
						
						//再放一次群组的?
						this.getGroup(wechatMeta, wechatContact);
						
						return wechatContact;
					}
				}
			}
		} catch (Exception e) {
			throw new WechatException(e);
		}
		return null;
	}

	private void getGroup(WechatMeta wechatMeta, WechatContact wechatContact) {
		String url = wechatMeta.getBase_uri() + "/webwxbatchgetcontact?type=ex&pass_ticket=" + wechatMeta.getPass_ticket() + "&skey="
				+ wechatMeta.getSkey() + "&r=" + DateKit.getCurrentUnixTime();

		JSONObject body = new JSONObject();
		body.put("BaseRequest", wechatMeta.getBaseRequest());

		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8")
				.header("Cookie", wechatMeta.getCookie()).send(body.toString());
		
		LOGGER.debug(request.toString());
		String res = request.body();
		request.disconnect();

		if (StringKit.isBlank(res)) {
			throw new WechatException("获取群信息失败");
		}
		
		LOGGER.debug(res);
		
		try {
			JSONObject jsonObject = JSONKit.parseObject(res);
			JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
			if (null != BaseResponse) {
				int ret = BaseResponse.getInt("Ret", -1);
				if (ret == 0) {
					JSONArray memberList = jsonObject.get("MemberList").asArray();
					JSONArray contactList = new JSONArray();
					
					if (null != memberList) {
						for (int i = 0, len = memberList.size(); i < len; i++) {
							JSONObject contact = memberList.get(i).asJSONObject();
							// 公众号/服务号
							if (contact.getInt("VerifyFlag", 0) == 8) {
								continue;
							}
							// 特殊联系人
							if (Constant.FILTER_USERS.contains(contact.getString("UserName"))) {
								continue;
							}
							// 群聊
							if (contact.getString("UserName").indexOf("@@") != -1) {
								continue;
							}
							// 自己
							if (contact.getString("UserName").equals(wechatMeta.getUser().getString("UserName"))) {
								continue;
							}
							contactList.add(contact);
						}
						
						wechatContact.setContactList(contactList);
						wechatContact.setMemberList(memberList);
					}
				}
			}
		} catch (Exception e) {
			throw new WechatException(e);
		}
	}

	/**
	 * 1.获取UUID
	 */
	@Override
	public String getUUID() {
		HttpRequest request = HttpRequest.get(Constant.JS_LOGIN_URL, true, "appid", "wx782c26e4c19acffb", "fun", "new",
				"lang", "zh_CN", "_", DateKit.getCurrentUnixTime());
		
		LOGGER.info("---获取uuid请求URL:" + request.toString());
		
		String res = request.body();
		request.disconnect();
		
		if (StringKit.isNotBlank(res)) {
			String code = Matchers.match("window.QRLogin.code = (\\d+);", res);
			if (null != code) {
				if (code.equals("200")) {
					return Matchers.match("window.QRLogin.uuid = \"(.*)\";", res);
				} else {
					throw new WechatException("错误的状态码: " + code);
				}
			}
		}
		throw new WechatException("获取UUID失败");
	}

	/**
	 * 打开状态提醒
	 */
	@Override
	public void openStatusNotify(WechatMeta wechatMeta) {

		String url = wechatMeta.getBase_uri() + "/webwxstatusnotify?lang=zh_CN&pass_ticket=" + wechatMeta.getPass_ticket();

		JSONObject body = new JSONObject();
		body.put("BaseRequest", wechatMeta.getBaseRequest());
		body.put("Code", 3);
		body.put("FromUserName", wechatMeta.getUser().getString("UserName"));
		body.put("ToUserName", wechatMeta.getUser().getString("UserName"));
		body.put("ClientMsgId", DateKit.getCurrentUnixTime());

		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8")
				.header("Cookie", wechatMeta.getCookie()).send(body.toString());

		LOGGER.debug("" + request);
		String res = request.body();
		request.disconnect();

		if (StringKit.isBlank(res)) {
			throw new WechatException("状态通知开启失败");
		}

		try {
			JSONObject jsonObject = JSONKit.parseObject(res);
			JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
			if (null != BaseResponse) {
				int ret = BaseResponse.getInt("Ret", -1);
				if (ret != 0) {
					throw new WechatException("状态通知开启失败，ret：" + ret);
				}
			}
		} catch (Exception e) {
			throw new WechatException(e);
		}
	}

	/**
	 * 微信初始化
	 */
	@Override
	public void wxInit(WechatMeta wechatMeta) {
		String url = wechatMeta.getBase_uri() + "/webwxinit?r=" + DateKit.getCurrentUnixTime() + "&pass_ticket="
				+ wechatMeta.getPass_ticket() + "&skey=" + wechatMeta.getSkey();

		JSONObject body = new JSONObject();
		body.put("BaseRequest", wechatMeta.getBaseRequest());

		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8")
				.header("Cookie", wechatMeta.getCookie()).send(body.toString());
		
		String res = request.body();
		request.disconnect();

		if (StringKit.isBlank(res)) {
			throw new WechatException("微信初始化失败");
		}

		try {
			JSONObject jsonObject = JSONKit.parseObject(res);
			if (null != jsonObject) {
				JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
				if (null != BaseResponse) {
					int ret = BaseResponse.getInt("Ret", -1);
					if (ret == 0) {
						wechatMeta.setSyncKey(jsonObject.get("SyncKey").asJSONObject());
						wechatMeta.setUser(jsonObject.get("User").asJSONObject());
//						LOGGER.info("用户信息:" + jsonObject.getString("User").toString());
						StringBuffer synckey = new StringBuffer();
						JSONArray list = wechatMeta.getSyncKey().get("List").asArray();
						for (int i = 0, len = list.size(); i < len; i++) {
							JSONObject item = list.get(i).asJSONObject();
							synckey.append("|" + item.getInt("Key", 0) + "_" + item.getInt("Val", 0));
						}
						wechatMeta.setSynckey(synckey.substring(1));
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 选择同步线路(从几个线路中选择一个能通的)
	 */
	@Override
	public void choiceSyncLine(WechatMeta wechatMeta) {
		boolean enabled = false;
		for(String syncUrl : Constant.SYNC_HOST){
			
			LOGGER.info("测试线路:" + syncUrl);
			int[] res = this.syncCheck(syncUrl, wechatMeta);
			LOGGER.info("测试结果:arr[0]=" + res[0] + "arr[1]=" + res[1]);

			//这里判断的是retcode 0=正常
			if(res[0] == 0){
				String url = "https://" + syncUrl + "/cgi-bin/mmwebwx-bin";
				wechatMeta.setWebpush_url(url);
				LOGGER.info("选择线路：[{}]", syncUrl);
				enabled = true;
				break;
			}
		}
		if(!enabled){
			throw new WechatException("同步线路不通畅");
		}
	}
	
	/**
	 * 检测心跳
	 */
	@Override
	public int[] syncCheck(WechatMeta wechatMeta){
		return this.syncCheck(null, wechatMeta);
	}
	
	/**
	 * 检测心跳
	 */
	private int[] syncCheck(String url, WechatMeta meta){
		// 如果网络中断，休息10秒
		if(PingUtil.netIsOver()){
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (Exception e){
				LOGGER.error(" === The network connection faild ! === ", e);
			}
		}else{
//			LOGGER.info("=== The network connection succeed ===");
		}

		if(null == url){
			url = meta.getWebpush_url() + "/synccheck";
		} else{
			url = "https://" + url + "/cgi-bin/mmwebwx-bin/synccheck";
		}
		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		
		HttpRequest request = HttpRequest
				.get(url, true, "r", DateKit.getCurrentUnixTime() + StringKit.getRandomNumber(5), "skey",
						meta.getSkey(), "uin", meta.getWxuin(), "sid", meta.getWxsid(), "deviceid",
						meta.getDeviceId(), "synckey", meta.getSynckey(), "_", System.currentTimeMillis())
				.header("Cookie", meta.getCookie());

		LOGGER.debug(request.toString());
		
		String res = request.body(); //这句话执行30秒?
//		LOGGER.info(res);
		request.disconnect();
		
		/*
		 * 初始化数组: [retcode, selector] 
		 * 	retcode:
		 * 	 	0-正常 , 1100-失败/登出微信
		 * 	selector:
		 * 	 	0-正常, 2-新的消息, 7 -进入/离开聊天界面
		 */
		int[] arr = new int[]{-1, -1};
		if (StringKit.isBlank(res)) {
			return arr;
		}
		
		String retcode = Matchers.match("retcode:\"(\\d+)\",", res);
		String selector = Matchers.match("selector:\"(\\d+)\"}", res);
		
		if (null != retcode && null != selector) {
			arr[0] = Integer.parseInt(retcode);
			arr[1] = Integer.parseInt(selector);
			return arr;
		}
		
		return arr;
	}

	/**
	 * 处理消息
	 */
	@Override
	public void handleMsg(WechatMeta wechatMeta, JSONObject data) {
		if (null == data) {
			LOGGER.info(" [ERROR]获取同步数据失败! ");
			return;
		}
		
		if(msgMap.size() > 100){
			LOGGER.info(" [HANDLE]清空消息缓存字典...");
			msgMap.clear();
		}

		JSONArray AddMsgList = data.get("AddMsgList").asArray();
		
		for (int i = 0, len = AddMsgList.size(); i < len; i++) {
//			LOGGER.info("你有新的消息，请注意查收");
			JSONObject msg = AddMsgList.get(i).asJSONObject();//获取一个消息体
//			LOGGER.info("一个消息体" + msg.toString());
			int msgType = msg.getInt("MsgType", 0); //消息类型
			//消息来源人姓名
			String name = getUserRemarkName(msg.getString("FromUserName")); 
			String content = msg.getString("Content"); //发送内容
			
			if (msgType == 51) {
				LOGGER.info("[REMIND] Intercept WeChat init message succeed! ");
			} else if (msgType == 1) { //文字消息
				
				if (Constant.FILTER_USERS.contains(msg.getString("FromUserName"))) {
					continue;//过滤掉系统推送消息
				} else if (msg.getString("FromUserName").equals(wechatMeta.getUser().getString("UserName"))) {
					continue;//过滤掉自己
				} else if (msg.getString("FromUserName").indexOf("@@") != -1) {//群文字消息
					msgMap.put(name, content);
					if(content.contains( "@"+Constant.config.get("tl.rebot_name")+"") ){//且有人@我
						LOGGER.info("---[REMIND]群聊@你:");
						msgMap.put(name, content);
						String ans = robot.Talk(content);
						//消息内容content加随机(0.3概率)emoji处理
						if((new Random().nextInt(10)) < 3){
							ans = EmojiUtil.emoji(EmojiUtil.emojiArr[new Random().nextInt(22)]) + " " + ans;
						}
						webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));
						LOGGER.info("---[REMIND]自动回复:" + ans); 
					}
				} else {
					LOGGER.info("---[REMIND]好友" + name + "说: " + content);
					String ans = "";
					msgMap.put(name, content);
					if(Constant.config.get("spu.remark_name").equals(name)){
						ans = SemanticUtil.semanticHandle(content);
						if(ans == null){
							ans = robot.Talk(content);
						}
					}else {
						ans = robot.Talk(content);
					}
					
					//消息内容content加随机(0.3概率)emoji处理
					if((new Random().nextInt(10)) < 3){
						ans = EmojiUtil.emoji(EmojiUtil.emojiArr[new Random().nextInt(22)]) + " " + ans;
					}
					webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));
					LOGGER.info("---[REMIND]自动回复: " + ans); 
				}
				
			} else if (msgType == 3) {//图片信息
				msgMap.put(name, " 一张小黄图 ");
				String ans = "";
				if (msg.getString("FromUserName").indexOf("@@") != -1) {//来自群
					if(content.contains( "@"+Constant.config.get("tl.rebot_name")+"") ){//且有人@我
						ans = EmojiUtil.emoji(0x1F62D)+"呜哇~"+wechatMeta.getUser().getString("NickName")+"还看不懂图片呢 ~~";
						webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));
					}
				}else {//来自好友
					ans = EmojiUtil.emoji(0x1F62D)+"呜哇~"+wechatMeta.getUser().getString("NickName")+"还看不懂图片呢 ~~";
					webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));
				}
				
			} else if (msgType == 34) { //语音消息
				String ans = "";
				if (msg.getString("FromUserName").indexOf("@@") != -1) {//来自群
					if(content.contains( "@"+Constant.config.get("tl.rebot_name")+"") ){//且有人@我
						ans = EmojiUtil.emoji(0x1F62D)+"呜哇~"+wechatMeta.getUser().getString("NickName")+"听不懂语音呢 ~~";
						webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));
					}
				}else {//来自好友
					ans = EmojiUtil.emoji(0x1F62D)+"呜哇~"+wechatMeta.getUser().getString("NickName")+"听不懂语音呢 ~~";
					webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));
				}
			
			} else if (msgType == 42) { //名片
				LOGGER.info(name + " 发送了一张名片:");
			} else if (msgType == 47){ //自定义表情, 只支持好友消息
				if(msg.getString("FromUserName").indexOf("@@") == -1){
					String ans = "";
					ans = Constant.config.get("spu.cute_content_c");
					webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));					
				}
				
			} else if (msgType == 10002){ //撤回消息
				String groupContent = msgMap.get(name);
				if(msg.getString("FromUserName").indexOf("@@") != -1){//来自群
					groupContent = groupContent.substring(groupContent.indexOf("<br/>") + 5);
				}
				String ans = Constant.config.get("spu.cute_content_b") + ": \n [" + groupContent + "]";
				webwxsendmsg(wechatMeta, ans, msg.getString("FromUserName"));
			}
		}
		
	}
	
	/**
	 * 发送消息
	 */
	private void webwxsendmsg(WechatMeta meta, String content, String to) {
		String url = meta.getBase_uri() + "/webwxsendmsg?lang=zh_CN&pass_ticket=" + meta.getPass_ticket();
		JSONObject body = new JSONObject();
		
		String clientMsgId = DateKit.getCurrentUnixTime() + StringKit.getRandomNumber(5);
		JSONObject Msg = new JSONObject();
		Msg.put("Type", 1);
		Msg.put("Content", content);
		Msg.put("FromUserName", meta.getUser().getString("UserName")); //自己的ID
		Msg.put("ToUserName", to); //好友的ID
		Msg.put("LocalID", clientMsgId);
		Msg.put("ClientMsgId", clientMsgId);

		body.put("BaseRequest", meta.getBaseRequest());
		body.put("Msg", Msg);

		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8")
				.header("Cookie", meta.getCookie()).send(body.toString());

		LOGGER.info("[HANDLE]发送消息...");
		request.body();
		request.disconnect();
	}
	
	/*
	 * 起名字的
	 * 	 1: 如果这人有备注, 名字就叫备注
	 *   2: 如果没有备注, 名字就用昵称 
	 *   3: 如果联系人列表中没这个人, 名字就叫"无名"
	 */
	private String getUserRemarkName(String id) {
		String name = "无名";
		for (int i = 0, len = Constant.CONTACT.getMemberList().size(); i < len; i++) {
			JSONObject member = Constant.CONTACT.getMemberList().get(i).asJSONObject();
			if (member.getString("UserName").equals(id)) {
				
				if (StringKit.isNotBlank(member.getString("RemarkName"))) { 
					name = member.getString("RemarkName");
				} else {
					name = member.getString("NickName");
				}
				return name;
			}
		}
		return name;
	}
	
	/**
	 * 获取同步消息 
	 */
	@Override
	public JSONObject webwxsync(WechatMeta meta){
		
		String url = meta.getBase_uri() + "/webwxsync?skey=" + meta.getSkey() + "&sid=" + meta.getWxsid();
		
		JSONObject body = new JSONObject();
		body.put("BaseRequest", meta.getBaseRequest());
		body.put("SyncKey", meta.getSyncKey());
		body.put("rr", DateKit.getCurrentUnixTime());

		HttpRequest request = HttpRequest.post(url).contentType("application/json;charset=utf-8")
				.header("Cookie", meta.getCookie()).send(body.toString());
		
		LOGGER.debug(request.toString());
		String res = request.body();
		request.disconnect();

		if (StringKit.isBlank(res)) {
			throw new WechatException("同步syncKey失败");
		}
		
		JSONObject jsonObject = JSONKit.parseObject(res);
		JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
		if (null != BaseResponse) {
			int ret = BaseResponse.getInt("Ret", -1);
			if (ret == 0) {
				meta.setSyncKey(jsonObject.get("SyncKey").asJSONObject());
				StringBuffer synckey = new StringBuffer();
				JSONArray list = meta.getSyncKey().get("List").asArray();
				for (int i = 0, len = list.size(); i < len; i++) {
					JSONObject item = list.get(i).asJSONObject();
					synckey.append("|" + item.getInt("Key", 0) + "_" + item.getInt("Val", 0));
				}
				meta.setSynckey(synckey.substring(1));
				return jsonObject;
			}
		}
		return null;
	}
	
	
	
	
  /**
    * <p>功能描述: 给特定联系人定时发送content</p>	
    * @param wechatMeta 用户个人信息(自己)
    * <p>谢发扬</p>
    * @since JDK1.8。
    * <p>创建日期2017年3月28日 上午10:49:42。</p>
    */
	@Override
	public void sayTimer(WechatMeta wechatMeta, String time) {
		
		//从联系人列表中找出username:[@+64位uuid]
		for(int i=0; i<Constant.CONTACT.getContactList().size(); ++i){
			JSONObject obj = Constant.CONTACT.getContactList().get(i).asJSONObject();
			//联系人: where remarkName="ycf"
			if(Constant.config.get("spu.remark_name").equals(obj.getString("RemarkName"))){
				String content = jinsRobot.Talk(time);
				this.webwxsendmsg(wechatMeta, content, obj.getString("UserName"));
				return ;
			}
		}
		return ;
	}
	
}
