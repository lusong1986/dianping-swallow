package com.dianping.swallow.web.dao;

import java.util.List;

import com.dianping.swallow.web.model.alarm.AlarmMeta;

public interface AlarmMetaDao {

	public boolean insert(AlarmMeta alarmMeta);

	public boolean update(AlarmMeta alarmMeta);
	
	public int deleteById(String id);
	
	public int deleteByMetaId(int metaId);
	
	public AlarmMeta findById(String id);
	
	public AlarmMeta findByMetaId(int metaId);
	
	public List<AlarmMeta> findByPage(int offset,int limit);

}
