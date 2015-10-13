package com.dianping.swallow.web.dao.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.dianping.swallow.web.dao.ConsumerServerStatsDataDao;
import com.dianping.swallow.web.model.stats.ConsumerServerStatsData;

/**
 * 
 * @author qiyin
 *
 *         2015年8月3日 下午2:38:46
 */
@Service("consumerServerStatsDataDao")
public class DefaultConsumerServerStatsDataDao extends AbstractStatsDao implements ConsumerServerStatsDataDao {

	private static final String CONSUMERSERVERSTATSDATA_COLLECTION = "CONSUMER_SERVER_STATS_DATA";

	private static final String TIMEKEY_FIELD = "timeKey";

	private static final String IP_FIELD = "ip";

	@Override
	public boolean insert(ConsumerServerStatsData serverStatsData) {
		try {
			mongoTemplate.save(serverStatsData, CONSUMERSERVERSTATSDATA_COLLECTION);
			return true;
		} catch (Exception e) {
			logger.error("Error when save consumer server statis dao " + serverStatsData, e);
		}
		return false;
	}

	@Override
	public boolean update(ConsumerServerStatsData serverStatsData) {
		return insert(serverStatsData);
	}

	@Override
	public List<ConsumerServerStatsData> findSectionData(String ip, long startKey, long endKey) {
		Query query = new Query(Criteria.where(IP_FIELD).is(ip).and(TIMEKEY_FIELD).gte(startKey).lte(endKey));
		List<ConsumerServerStatsData> serverStatisDatas = mongoTemplate.find(query, ConsumerServerStatsData.class,
				CONSUMERSERVERSTATSDATA_COLLECTION);
		return serverStatisDatas;
	}

	@Override
	public List<ConsumerServerStatsData> findSectionData(long startKey, long endKey) {
		Query query = new Query(Criteria.where(TIMEKEY_FIELD).gte(startKey).lte(endKey)).with(new Sort(new Sort.Order(
				Direction.ASC, TIMEKEY_FIELD)));
		List<ConsumerServerStatsData> serverStatisDatas = mongoTemplate.find(query, ConsumerServerStatsData.class,
				CONSUMERSERVERSTATSDATA_COLLECTION);
		return serverStatisDatas;
	}

}
