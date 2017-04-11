package me.biezhi.wechat.model;

import com.blade.kit.json.JSONArray;

public class WechatContact {

	// 所有的好友信息
	private JSONArray memberList;
	
	// 主要用来表示联系人（此列表不全，只包括了类似通讯录助手、文件助手、微信团队和一些公众帐号等)
	private JSONArray contactList;
	private JSONArray groupList;
	
	public WechatContact() {
		// TODO Auto-generated constructor stub
	}

	public JSONArray getMemberList() {
		return memberList;
	}

	public void setMemberList(JSONArray memberList) {
		this.memberList = memberList;
	}

	public JSONArray getContactList() {
		return contactList;
	}

	public void setContactList(JSONArray contactList) {
		this.contactList = contactList;
	}

	public JSONArray getGroupList() {
		return groupList;
	}

	public void setGroupList(JSONArray groupList) {
		this.groupList = groupList;
	}
	
}
