package com.dianping.swallow.web.dao.impl.backup;

import java.util.List;

import com.dianping.swallow.web.dao.backup.TopicAlarmSettingDao;
import com.dianping.swallow.web.dao.impl.AbstractWriteDao;
import com.dianping.swallow.web.model.alarm.backup.TopicAlarmSetting;

public class DefaultTopicAlarmSettingDao extends AbstractWriteDao implements TopicAlarmSettingDao {

	@Override
	public boolean insert(TopicAlarmSetting setting) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(TopicAlarmSetting setting) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int deleteById(String id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TopicAlarmSetting findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TopicAlarmSetting findByTopic(String topicName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TopicAlarmSetting> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}