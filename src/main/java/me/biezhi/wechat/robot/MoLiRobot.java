package me.biezhi.wechat.robot;

import com.blade.kit.StringKit;
import com.blade.kit.http.HttpRequest;

import me.biezhi.wechat.Constant;
 


 /**
  *<p>类描述：茉莉机器人</p>
  * @author 谢发扬 </p>
  * @version v1.0.0.1。
  * @since JDK1.8。
  *<p>创建日期：2017年3月24日 下午1:32:25。</p>
  */
 
public class MoLiRobot implements Robot {

	private String apiUrl;
	
	public MoLiRobot() {
		String api_key = Constant.config.get("itpk.api_key");
		String api_secret = Constant.config.get("itpk.api_secret");
		if(StringKit.isNotBlank(api_key) && StringKit.isNotBlank(api_secret)){
			this.apiUrl = Constant.ITPK_API + "?api_key=" + api_key + "&api_secret=" + api_secret;
		}
	}

	@Override
	public String Talk(String msg) {
		if(null == this.apiUrl){
			return "机器人未配置";
		}
		String url = apiUrl + "&question=" + msg;
		String result = HttpRequest.get(url).connectTimeout(3000).body();
		return result;
	}

}
