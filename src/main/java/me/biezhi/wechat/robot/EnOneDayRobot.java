package me.biezhi.wechat.robot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blade.kit.StringKit;
import com.blade.kit.http.HttpRequest;
import com.google.gson.Gson;

import me.biezhi.wechat.Constant;
import me.biezhi.wechat.util.EmojiUtil;


 /**
  *<p>类描述：每日定时发送内容</p>
  * @author 谢发扬 </p>
  * @version v1.0.0.1。
  * @since JDK1.8。
  *<p>创建日期：2017年3月24日 下午1:24:30。</p>
  */
public class EnOneDayRobot implements Robot {

	public EnOneDayRobot() {}

	@Override
	public String Talk(String time) {
		StringBuilder result = new StringBuilder(); 
		if("AM".equals(time)){ //say good morning + 天气预报
			result.append(EmojiUtil.emoji(0x2600))
			.append(Constant.config.get("js.say_good_morning"))
			.append(EmojiUtil.emoji(0x1F61D)).append("\n")
			.append(this.weather2(Constant.config.get("spu.location")));
		}else if("PM".equals(time)){//say good night + 每日一句
			SimpleDateFormat sdf = new SimpleDateFormat(Constant.config.get("js.say_good_night"));
			result.append(EmojiUtil.emoji(0x1F319))
			.append(sdf.format(new Date()))
			.append(EmojiUtil.emoji(0x1F437))
			.append("\n\n").append(this.english());
		}
		
		return result.toString();
	}
	
	
	 /**
	  * <p>功能描述：金山英语API获取每日一句</p>
	  */
	private String english(){
		String url = "http://open.iciba.com/dsapi/";
		String result = HttpRequest.get(url).connectTimeout(3000).body();
		int start = result.indexOf("\"content\":\"");
		int end = result .indexOf("\"note\"", start);
		result = result.substring(start+11, end-2); //英文
		return result;
	}
	 
	 /**
	  * <p>功能描述：茉莉机器人获取天气预报</p>
	  */
	private String weather1(String question){
		String apiUrl = "";
		String api_key = Constant.config.get("itpk.api_key");
		String api_secret = Constant.config.get("itpk.api_secret");
		if(StringKit.isNotBlank(api_key) && StringKit.isNotBlank(api_secret)){
			apiUrl = Constant.ITPK_API + "?api_key=" + api_key + "&api_secret=" + api_secret;
		}
		try {
			question = URLEncoder.encode(question, "utf-8");
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		String url = apiUrl + "&question=" + question;
		String result = "";
		try {
			 result = HttpRequest.get(url).connectTimeout(5000).body();
			 result = result.substring(result.indexOf("\n"), result.lastIndexOf("\n"));
		} catch (Exception e) {
			result = " Sorry~　天气预报 Invoke Exception! TIMEOUT! ";
			return result;
		}
		return result;
	}
	
	
	 /**
	  * <p>功能描述：茉莉天气调不通, 暂时换这个开源的</p>
	  */
	@SuppressWarnings("unchecked")
	private String weather2(String city){
		try {
			city = URLEncoder.encode(city, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = "http://api.jirengu.com/weather.php?city=" + city;
		String result = "";
		try {
			result = HttpRequest.get(url).connectTimeout(4000).body();	
		} catch (Exception e) {
			result = " Sorry~　天气预报 Invoke Exception! TIMEOUT! ";
			return result;
		}
		
		StringBuilder sb = new StringBuilder();
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();
		map = gson.fromJson(result, map.getClass());
		List list = (List)map.get("results");
		map = (Map<String, Object>)list.get(0);
		list = (List)map.get("weather_data");
		map = (Map<String, Object>)list.get(0);
		String date = (String) map.get("date");
		String weather = (String) map.get("weather");
		String wind = (String) map.get("wind");
		String temperature = (String) map.get("temperature");
		sb.append(date + ", ");
		sb.append(weather + ", ");
		if(weather.contains("雨")){
			sb.append("妈的今天又下雨啊! ");
		}
		sb.append(wind + ", 温度:");
		sb.append(temperature);
		return sb.toString();
	}
	
}
