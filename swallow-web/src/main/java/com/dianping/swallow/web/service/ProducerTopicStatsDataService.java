package com.dianping.swallow.web.service;

import java.util.List;
import java.util.NavigableMap;

import com.dianping.swallow.web.model.stats.ProducerTopicStatsData;

/**
 * 
 * @author qiyin
 *
 *         2015年8月3日 下午2:39:30
 */
public interface ProducerTopicStatsDataService extends StatsDataService {

	boolean insert(ProducerTopicStatsData topicStatsData);
	
	boolean insert(List<ProducerTopicStatsData> topicStatsDatas);

	List<ProducerTopicStatsData> findByTopic(String topicName, int offset, int limit);

	List<ProducerTopicStatsData> findSectionData(String topicName, long startKey, long endKey);

	NavigableMap<Long, Long> findSectionQpsData(String topicName, long startKey, long endKey);

	NavigableMap<Long, Long> findSectionDelayData(String topicName, long startKey, long endKey);

	ProducerTopicStatsData findOneByTopicAndTime(String topicName, long startKey, long endKey, boolean isGt);

}
