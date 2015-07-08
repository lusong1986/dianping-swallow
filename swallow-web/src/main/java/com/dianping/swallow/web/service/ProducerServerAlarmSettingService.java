package com.dianping.swallow.web.service;

import java.util.List;

import com.dianping.swallow.web.model.alarm.ProducerServerAlarmSetting;

public interface ProducerServerAlarmSettingService {

	public boolean insert(ProducerServerAlarmSetting setting);

	public boolean update(ProducerServerAlarmSetting setting);

	public int deleteById(String id);

	public ProducerServerAlarmSetting findById(String id);

	public List<ProducerServerAlarmSetting> findAll();
	
}
