package com.dianping.swallow.web.monitor.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.swallow.common.internal.action.SwallowAction;
import com.dianping.swallow.common.internal.action.SwallowActionWrapper;
import com.dianping.swallow.common.internal.action.impl.CatActionWrapper;
import com.dianping.swallow.common.internal.dao.MessageDAO;
import com.dianping.swallow.common.internal.exception.SwallowException;
import com.dianping.swallow.common.internal.threadfactory.MQThreadFactory;
import com.dianping.swallow.common.internal.util.CommonUtils;
import com.dianping.swallow.common.internal.util.MapUtil;
import com.dianping.swallow.common.server.monitor.data.StatisDetailType;
import com.dianping.swallow.web.monitor.AccumulationRetriever;
import com.dianping.swallow.web.monitor.StatsData;
import com.dianping.swallow.web.monitor.StatsDataDesc;
import com.dianping.swallow.web.task.TopicScanner;

/**
 * @author mengwenchao
 *
 * 2015年5月28日 下午3:06:46
 */
@Component
public class DefaultAccumulationRetriever extends AbstractRetriever implements AccumulationRetriever{

	private Map<String, TopicAccumulation> topics = new ConcurrentHashMap<String, DefaultAccumulationRetriever.TopicAccumulation>();
	
	@Autowired
	private TopicScanner  topicScanner;
	
	@Autowired
	private MessageDAO messageDao;
	
	private ExecutorService executors = Executors.newFixedThreadPool(CommonUtils.DEFAULT_CPU_COUNT*5, new MQThreadFactory("ACCUMULATION_RETRIEVER-"));

	@Override
	protected void doBuild() {
		
		SwallowActionWrapper actionWrapper = new CatActionWrapper("DefaultAccumulationRetriever", "doBuild");
		
		actionWrapper.doAction(new SwallowAction() {
			@Override
			public void doAction() throws SwallowException {
				
				buildAllAccumulations();
			}
		});
	}

	protected void buildAllAccumulations() {
		
		Map<String, Set<String>> topics = topicScanner.getTopics();
		
		if(logger.isDebugEnabled()){
			logger.debug("[buildAllAccumulations]" + topics);
		}
		
		final CountDownLatch latch = new CountDownLatch(topics.size());
		for(Entry<String, Set<String>> entry : topics.entrySet()){
			
			final String topicName = entry.getKey();
			final Set<String> consumerIds = entry.getValue();
			
			executors.execute(new Runnable(){

				@Override
				public void run() {
					try{
						putAccumulation(topicName, consumerIds);
					}finally{
						latch.countDown();
					}
				}
				
			});
		}
		
		try {
			boolean result = latch.await(getDefaultInterval(), TimeUnit.SECONDS);
			if(!result){
				logger.error("[buildAllAccumulations][wait returned, but task has not finished yet!]");
			}
		} catch (InterruptedException e) {
			logger.error("[buildAllAccumulations]", e);
		}
	}
	
	protected void putAccumulation(String topicName, Set<String> consumerIds) {
		
		
		for(String consumerId : consumerIds){
			
			long size = messageDao.getAccumulation(topicName, consumerId);
			
			TopicAccumulation topicAccumulation = MapUtil.getOrCreate(topics, topicName, TopicAccumulation.class);
			topicAccumulation.addConsumerId(consumerId, size);
		}
		
	}

	@Override
	protected void doRemove(long toKey) {

		for(TopicAccumulation topicAccumulation : topics.values()){
			topicAccumulation.removeBefore(toKey);
		}
		
	}

	@Override
	protected Set<String> getTopicsInMemory(long start, long end) {
		
		return topics.keySet();
	}

	@Override
	public Map<String, StatsData> getAccumulationForAllConsumerId(String topic,
			long start, long end) {
		
		if(dataExistInMemory(start, end)){
			return getAccumulationForAllConsumerIdInMemory(topic, start, end);
		}
		
		return getAccumulationForAllConsumerIdInDb(topic, start, end);

	}

	private Map<String, StatsData> getAccumulationForAllConsumerIdInDb(
			String topic, long start, long end) {
		return getAccumulationForAllConsumerIdInMemory(topic, start, end);
	}

	private Map<String, StatsData> getAccumulationForAllConsumerIdInMemory(
			String topic, long start, long end) {
		
		Map<String, StatsData> result = new HashMap<String, StatsData>();
		TopicAccumulation topicAccumulation = topics.get(topic);
		for(Entry<String, ConsumerIdAccumulation> entry : topicAccumulation.consumers.entrySet()){
			
			String consumerId = entry.getKey();
			ConsumerIdAccumulation consumerIdAccumulation = entry.getValue();
			
			StatsDataDesc desc = new ConsumerStatsDataDesc(topic, consumerId, StatisDetailType.ACCUMULATION);
			result.put(consumerId, createStatsData(desc, consumerIdAccumulation.accumulations, start, end));
		}
		
		return result;
	}

	@Override
	public Map<String, StatsData> getAccumulationForAllConsumerId(String topic){
		
		return getAccumulationForAllConsumerId(topic, getDefaultStart(), getDefaultEnd());
	}

	public static class TopicAccumulation{
		
		private Map<String, ConsumerIdAccumulation> consumers = new ConcurrentHashMap<String, DefaultAccumulationRetriever.ConsumerIdAccumulation>();
		
		public void addConsumerId(String consumerId, long accumulation){
			
			ConsumerIdAccumulation consumerIdAccumulation = MapUtil.getOrCreate(consumers, consumerId, ConsumerIdAccumulation.class);
			consumerIdAccumulation.add(accumulation);
		}
		
		public void removeBefore(Long toKey){
			
			for(ConsumerIdAccumulation consumer : consumers.values()){
				
				consumer.removeBefore(toKey);
			}
		}
		
		public Map<String, ConsumerIdAccumulation> consumers(){
			return consumers;
		}
	}
	
	public static class ConsumerIdAccumulation{
		
		private NavigableMap<Long, Long> accumulations = new ConcurrentSkipListMap<Long, Long>();
		
		protected final Logger logger     = LoggerFactory.getLogger(getClass());

		public void add(long accumulation) {
			
			
			Long key = getKey(System.currentTimeMillis());
			if(logger.isDebugEnabled()){
				logger.debug("[add]" + key + ":" + accumulation);
			}
			accumulations.put(key, accumulation);
		}
		
		public List<Long> data() {
			
			List<Long> result = new LinkedList<Long>();
			result.addAll(accumulations.values());
			return result;
		}

		public void removeBefore(Long toKey){
			
			Map<Long, Long> toDelete = accumulations.headMap(toKey);
			for(Long key : toDelete.keySet()){
				if(logger.isDebugEnabled()){
					logger.debug("[removeBefore]" + key);
				}
				accumulations.remove(key);
			}
		}
	}
}