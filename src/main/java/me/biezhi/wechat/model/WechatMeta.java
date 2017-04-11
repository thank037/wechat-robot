package me.biezhi.wechat.model;

import com.blade.kit.DateKit;
import com.blade.kit.json.JSONObject;

import me.biezhi.wechat.Constant;

public class WechatMeta {
	
	private String base_uri, redirect_uri, webpush_url = Constant.BASE_URL;
	
	/*
	 * 微信Web版本不使用用户名和密码登录，而是采用二维码登录，
	 * 所以服务器需要首先分配一个唯一的会话ID，用来标识当前的一次登录
	 */
	private String uuid;
	
	private String skey;
	
	private String yechangfang;
	
	//SyncKey中的list, 例如: {'Val': 636214192, 'Key': 1}, ...
	private String synckey;
	private String wxsid;
	private String wxuin;
	private String pass_ticket;
	private String deviceId = "e" + DateKit.getCurrentUnixTime();
	
	private String cookie;
	
	private JSONObject baseRequest;
	private JSONObject SyncKey;
	private JSONObject User;
	
	public WechatMeta() {
		
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getSynckey() {
		return synckey;
	}

	public void setSynckey(String synckey) {
		this.synckey = synckey;
	}

	public String getWxsid() {
		return wxsid;
	}

	public void setWxsid(String wxsid) {
		this.wxsid = wxsid;
	}

	public String getWxuin() {
		return wxuin;
	}

	public void setWxuin(String wxuin) {
		this.wxuin = wxuin;
	}

	public String getPass_ticket() {
		return pass_ticket;
	}

	public void setPass_ticket(String pass_ticket) {
		this.pass_ticket = pass_ticket;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public JSONObject getBaseRequest() {
		return baseRequest;
	}

	public void setBaseRequest(JSONObject baseRequest) {
		this.baseRequest = baseRequest;
	}

	public JSONObject getSyncKey() {
		return SyncKey;
	}

	public void setSyncKey(JSONObject syncKey) {
		SyncKey = syncKey;
	}

	public JSONObject getUser() {
		return User;
	}

	public void setUser(JSONObject user) {
		User = user;
	}

	public String getBase_uri() {
		return base_uri;
	}

	public void setBase_uri(String base_uri) {
		this.base_uri = base_uri;
	}

	public String getRedirect_uri() {
		return redirect_uri;
	}

	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}

	public String getWebpush_url() {
		return webpush_url;
	}

	public void setWebpush_url(String webpush_url) {
		this.webpush_url = webpush_url;
	}

	public String getYechangfang() {
		return yechangfang;
	}

	public void setYechangfang(String yechangfang) {
		this.yechangfang = yechangfang;
	}
	
}
