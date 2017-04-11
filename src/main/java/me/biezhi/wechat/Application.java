package me.biezhi.wechat;

import com.blade.kit.base.Config;

public class Application { 
	
	public static void main(String[] args) {
		try {
			
			Constant.config = Config.load("classpath:config.properties");
			
			WechatRobot wechatRobot = new WechatRobot();
			//显示二维码
			wechatRobot.showQrCode();
			
			//轮询等待扫描二维码登录
			while(!Constant.HTTP_OK.equals(wechatRobot.waitForLogin())){
				Thread.sleep(2000);
			}
			
			//关闭二维码窗口
			wechatRobot.closeQrWindow();
			wechatRobot.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}