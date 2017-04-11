package me.biezhi.wechat.listener;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.kit.json.JSONObject;

import me.biezhi.wechat.model.WechatMeta;
import me.biezhi.wechat.service.WechatService;

public class WechatListener { 

	private static final Logger LOGGER = LoggerFactory.getLogger(WechatListener.class);
	
	int playWeChat = 0;
	
	/**
	 * 消息监听
	 */
	public void start(final WechatService wechatService, final WechatMeta wechatMeta){
		new Thread(new Runnable() {
			@Override
			public void run() {
				LOGGER.info(" ========= Message Monitor Beginning ===========");
				wechatService.choiceSyncLine(wechatMeta);
				
				//Polling Message
				while(true){
					int[] arr = wechatService.syncCheck(wechatMeta);
					LOGGER.info("[POLLING]: retcode={}, selector={}", arr[0], arr[1]);
					//retcode: 1100-失败/登出
					if(arr[0] == 1100){
						LOGGER.info("retcode=1100, Failed! Logout Wechat");
						break;
					}
					
					//retcode: 0=正常
					if(arr[0] == 0){
						timerSay(wechatService, wechatMeta);//自动发送消息
						
						//selector: 0-正常, 2(6)-新的消息, 7 -进入/离开聊天界面
						if(arr[1] == 0){
							continue;
						}else if( (arr[1] == 2) || (arr[1] == 6) ){
							JSONObject data = wechatService.webwxsync(wechatMeta);
							wechatService.handleMsg(wechatMeta, data);
						} else if(arr[1] == 7){
							playWeChat += 1;
							LOGGER.info("You play Wechat count{} ", playWeChat);
							wechatService.webwxsync(wechatMeta);
						} else if(arr[1] == 3){
							LOGGER.info("selector=3, I don't know what's happen, The network is fiald!");
							//TODO 失败! 重新选择线路?
						} 
						
					} else { LOGGER.info("========[retcode="+arr[0]+"], 与服务器通信异常!========");}
					
					try {
						LOGGER.info("wait 2000ms..."); 
						Thread.sleep(2000);
					} catch (InterruptedException e) {e.printStackTrace();}
				} // end polling
				
			}
		}, "wechat-listener-thread").start();
	}
	
	
	/**
	 * 定时消息
	 */
	public static void timerSay(WechatService wechatService, WechatMeta wechatMeta) {
		
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		
		//上午9点3
		if(hour==8 && min==45){
			try {
				wechatService.sayTimer(wechatMeta, "AM");
				LOGGER.info("Send good morning~, please wait 60s...");
				Thread.sleep(60000);//睡眠60秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		//下午23.47
		}else if(hour==23 && min==45){ 
			try {
				wechatService.sayTimer(wechatMeta, "PM");
				LOGGER.info("Send goot night~, Please wait 60s...");
				Thread.sleep(60000);//睡眠60秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
