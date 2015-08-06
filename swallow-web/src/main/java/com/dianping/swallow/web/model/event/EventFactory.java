package com.dianping.swallow.web.model.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.swallow.web.container.AlarmMetaContainer;
import com.dianping.swallow.web.manager.IPDescManager;
import com.dianping.swallow.web.service.AlarmService;
import com.dianping.swallow.web.service.IPCollectorService;
import com.dianping.swallow.web.service.SeqGeneratorService;

/**
 * 
 * @author qiyin
 *
 *         2015年8月3日 上午11:13:05
 */
@Service("eventFactory")
public class EventFactory {

	@Autowired
	private AlarmService alarmService;

	@Autowired
	private IPDescManager ipDescManager;

	@Autowired
	private AlarmMetaContainer alarmMetaContainer;

	@Autowired
	protected IPCollectorService ipCollectorService;

	@Autowired
	private SeqGeneratorService seqGeneratorService;

	private void setComponent(Event event) {
		event.setAlarmService(alarmService);
		event.setIPDescManager(ipDescManager);
		event.setAlarmMetaContainer(alarmMetaContainer);
		event.setIPCollectorService(ipCollectorService);
		event.setSeqGeneratorService(seqGeneratorService);
	}

	public TopicEvent createTopicEvent() {
		TopicEvent topicEvent = new TopicEvent();
		setComponent(topicEvent);
		return topicEvent;
	}

	public ServerEvent createServerEvent() {
		ServerEvent serverEvent = new ServerEvent();
		setComponent(serverEvent);
		return serverEvent;
	}

	public ServerStatisEvent createServerStatisEvent() {
		ServerStatisEvent serverStatisEvent = new ServerStatisEvent();
		setComponent(serverStatisEvent);
		return serverStatisEvent;
	}

	public ConsumerIdEvent createConsumerIdEvent() {
		ConsumerIdEvent consumerIdEvent = new ConsumerIdEvent();
		setComponent(consumerIdEvent);
		return consumerIdEvent;
	}
}