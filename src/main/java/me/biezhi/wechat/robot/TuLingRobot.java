package me.biezhi.wechat.robot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.http.HttpRequest;

import me.biezhi.wechat.Constant;



 /**
  *<p>类描述：图灵机器人</p>
  * @author 谢发扬 </p>
  * @version v1.0.0.1。
  * @since JDK1.8。
  *<p>创建日期：2017年3月24日 下午1:24:30。</p>
  */
public class TuLingRobot implements Robot {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TuLingRobot.class);
	
	public TuLingRobot() {}

	@Override
	public String Talk(String info) {
		info = this.handleMsg(info);
//		LOGGER.info(info);
		try {
			//不转码会导致调用图灵机器人时汉字变乱码
			info = URLEncoder.encode(info, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Constant.TL_API).append("?key=").append(Constant.TL_ROBOT_KTY)
		.append("&info=").append(info);
		String tulResult = HttpRequest.get(sb.toString()).connectTimeout(3000).body();
		int start = tulResult.indexOf("\"text\":\"");
		int end = tulResult.indexOf("\"}");
		String result = "";
		
		try {
			result = tulResult.substring(start+8, end);
		} catch (Exception e) {
			return "消息解析失败!";
		}
		
		return handleOther(result);
	}
	
	//处理群聊信息content乱
	public String handleMsg(String info){
		String rebot_name = Constant.config.get("tl.rebot_name");
		if(info.contains("@") && info.contains("<br/>")){
			if(info.contains(rebot_name)){
				info = info.substring(info.indexOf("<br/>@"+rebot_name+"")+10);
			}else {
				info = info.substring(info.indexOf("<br/>")+5);	
			}
		}
		return info;
	}
	
	
	//"图灵机器人"水印处理 + 语气词随机添加
	private String handleOther(String content){
		
		if(content.contains("图灵机器人")){
			content = content.replace("图灵机器人", Constant.config.get("tl.rebot_name"));
			content = content + " " + Constant.config.get("spu.cute_content_a");
		}
		
		if(content.length() < 6){
			if( (new Random().nextInt(4)) < 3){
				if(new Random().nextBoolean()){
					content = "这... " + content;
				}else{
					content = "嘻嘻 " + content;
				}
			}
		}	
		
		return content;
	}

}
