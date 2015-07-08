package com.dianping.swallow.web.model.alarm;

import java.util.List;

import org.springframework.data.annotation.Id;

public class ConsumerIdAlarmSetting {
	
	@Id
	private String id;
	
	private List<String> whiteList;
	
	private ConsumerBaseAlarmSetting consumerAlarmSetting;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(List<String> whiteList) {
		this.whiteList = whiteList;
	}

	public ConsumerBaseAlarmSetting getConsumerAlarmSetting() {
		return consumerAlarmSetting;
	}

	public void setConsumerAlarmSetting(ConsumerBaseAlarmSetting consumerAlarmSetting) {
		this.consumerAlarmSetting = consumerAlarmSetting;
	}
}
