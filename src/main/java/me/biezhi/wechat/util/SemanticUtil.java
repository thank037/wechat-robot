package me.biezhi.wechat.util;

import java.util.Random;

import me.biezhi.wechat.Constant;

public class SemanticUtil {
	 
	public static String semanticHandle(String content){
		if( content.contains("豆丁") || content.contains("在") ){
			if((!content.contains("安")) && (!content.contains("好"))){
				if(new Random().nextBoolean()){
					return Constant.config.get("spu.cute_content_douding1");	
				}else{
					return Constant.config.get("spu.cute_content_douding2");
				}
			}
		}else if(content.contains("男朋友") || content.contains("男票")) {
			return Constant.config.get("spu.cute_content_boyfriend");
		}else if(content.contains("无聊")) {
			return Constant.config.get("spu.cute_content_finddad");
		}else if(content.contains("陪我") || content.contains("不开心") || content.contains("烦") || content.contains("难受")){
			return Constant.config.get("spu.cute_content_nothappy");
		}else if(content.contains("我是谁") || (content.contains("认识")&&content.contains("我"))){
			return Constant.config.get("spu.cute_content_whoami");
		}else if(content.contains("乖")){
			return " 嘻嘻 在妈妈面前小豆丁最乖啦~ ";
		}
		return null;
	}
	
}
