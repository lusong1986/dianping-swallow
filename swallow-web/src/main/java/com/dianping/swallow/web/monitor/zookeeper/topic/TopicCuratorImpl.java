package com.dianping.swallow.web.monitor.zookeeper.topic;

import com.dianping.swallow.web.model.event.Event;
import com.dianping.swallow.web.model.event.ServerType;
import com.dianping.swallow.web.model.resource.KafkaServerResource;
import com.dianping.swallow.web.monitor.jmx.event.BrokerKafkaEvent;
import com.dianping.swallow.web.monitor.jmx.event.KafkaEvent;
import com.dianping.swallow.web.monitor.zookeeper.AbstractCuratorAware;
import com.dianping.swallow.web.monitor.zookeeper.event.TopicCuratorEvent;
import com.dianping.swallow.web.service.KafkaServerResourceService;
import com.yammer.metrics.core.MetricName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * Author   mingdongli
 * 16/2/22  下午6:47.
 */
@Component
public class TopicCuratorImpl extends AbstractCuratorAware implements TopicCurator {

    private static final String TOPIC_DESCRIPTION = "/brokers/topics";

    private Map<TopicPartitionKey, Boolean> topicPartitionKeyMap = new HashMap<TopicPartitionKey, Boolean>();

    private Set<String> downBrokers = new HashSet<String>();

    private Map<IdKey, String> brokerId2Ip = new HashMap<IdKey, String>();

    private Set<String> exceptionZkServer = new HashSet<String>();

    @Value("${swallow.web.monitor.zk.underreplica.threshold}")
    private int THRESHHOLD;

    @Resource(name = "kafkaServerResourceService")
    protected KafkaServerResourceService kafkaServerResourceService;

    @Override
    protected void doFetchZkData() {
        Map<Integer, String> zkClusters = loadKafkaZkClusters();
        for (Map.Entry<Integer, String> entry : zkClusters.entrySet()) {
            int groupId = entry.getKey();
            String zkServers = entry.getValue();
            int stopAlarmNum = 0;
            CuratorFramework curator = getCurator(zkServers);
            try {
                List<String> topics = getTopics(curator);
                Collections.shuffle(topics);
                for (String topic : topics) {
                    TopicDescription topicDescription = getTopicDescription(curator, topic);
                    if (topicDescription == null) {
                        continue;
                    }
                    Map<Integer, List<Integer>> part2Replica = topicDescription.getPartitions();
                    for (Map.Entry<Integer, List<Integer>> partreplica : part2Replica.entrySet()) {
                        int partition = partreplica.getKey();
                        List<Integer> replica = partreplica.getValue();
                        PartitionDescription partitionDescription = getPartitionDescription(curator, topic, partition);
                        if (partitionDescription == null) {
                            continue;
                        }
                        List<Integer> isr = partitionDescription.getIsr();

                        if (isr != null && replica != null && isr.size() < replica.size()) {

                            List<String> transformedBrokerId = downIpList(replica, isr, groupId);
                            if (CollectionUtils.containsAny(downBrokers, transformedBrokerId)) {
                                Collection<String> intersection = CollectionUtils.intersection(downBrokers, transformedBrokerId);
                                if (CollectionUtils.containsAny(exceptionZkServer, intersection)) {
                                    continue;
                                }
                                stopAlarmNum++;
                                if (stopAlarmNum >= THRESHHOLD) {
                                    exceptionZkServer.addAll(intersection);
                                    throw new Exception("Stop alarm under replica due to broker down.");
                                }
                            }
                            boolean isPass = checkReplicaSize(groupId, replica);
                            if (isPass) {
                                reportTopicCuratorWrongEvent(groupId, topic, partition, isr, replica);
                            }
                        } else {
                            if (replica != null) {
                                for (int r : replica) {
                                    String ip = brokerId2Ip.get(new IdKey(r, groupId));
                                    if (StringUtils.isNotBlank(ip)) {
                                        exceptionZkServer.remove(ip);
                                    }
                                }
                            }
                            reportTopicCuratorOKEvent(groupId, topic, partition);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Get data from zk error", e);
            }

        }
    }

    @Override
    protected String baseZkPath() {
        return TOPIC_DESCRIPTION;
    }

    @Override
    public List<String> getTopics(CuratorFramework curator) throws Exception {
        return curator.getChildren().forPath(TOPIC_DESCRIPTION);
    }

    @Override
    public TopicDescription getTopicDescription(CuratorFramework curator, String topic) {
        try {
            byte[] topicDescription = curator.getData().forPath(zkPath(topic));
            return jsonBinder.fromJson(new String(topicDescription), TopicDescription.class);
        } catch (Exception e) {
            logger.error(String.format("Error when get data for topic %s", topic));
            return null;
        }
    }

    @Override
    public PartitionDescription getPartitionDescription(CuratorFramework curator, String topic, int partition) {
        try {
            byte[] partitionDescription = curator.getData().forPath(topicPartitionStatePath(topic, partition));
            return jsonBinder.fromJson(new String(partitionDescription), PartitionDescription.class);
        } catch (Exception e) {
            logger.error(String.format("Error when get data for topic %s of partition %d", topic, partition));
            return null;
        }
    }

    private String topicPartitionStatePath(String topic, int partition) {
        String path = new StringBuilder().append(topic).append(BACK_SLASH).append("partitions").append(BACK_SLASH)
                .append(partition).append(BACK_SLASH).append("state").toString();
        return zkPath(path);
    }

    @Override
    protected int getInterval() {
        return 60000;
    }

    @Override
    protected int getDelay() {
        return 10750;   //减小与brokerstate检测的时间差，增加先检测到broker宕机的概率
    }

    @Override
    protected KafkaEvent createEvent() {
        return eventFactory.createTopicCuratorEvent();
    }

    private List<String> downIpList(final List<Integer> replica, final List<Integer> isr, int groupId) {

        Collection<Integer> compare;

        if (isr == null) {
            compare = Collections.unmodifiableCollection(replica);
        } else {
            compare = CollectionUtils.subtract(replica, isr);
        }

        return transformBrokerId(compare, groupId);
    }

    private Map<Integer, String> loadKafkaZkClusters() {

        Map<Integer, String> groupId2KafkaZkCluster = new HashMap<Integer, String>();
        List<KafkaServerResource> kafkaServerResources = kafkaServerResourceService.findAll();
        for (KafkaServerResource kafkaServerResource : kafkaServerResources) {

            int brokerId = kafkaServerResource.getBrokerId();
            String brokerIp = kafkaServerResource.getIp();
            int groupId = kafkaServerResource.getGroupId();
            brokerId2Ip.put(new IdKey(brokerId, groupId), brokerIp);

            String zkIps = groupId2KafkaZkCluster.get(groupId);
            if (zkIps == null) {
                zkIps = kafkaServerResource.getZkServers();
                groupId2KafkaZkCluster.put(groupId, zkIps);
            }
        }
        return groupId2KafkaZkCluster;

    }

    private void reportTopicCuratorWrongEvent(int groupId, String topic, int partition, List<Integer> isr, List<Integer> replica) {

        TopicPartitionKey topicPartitionKey = new TopicPartitionKey(groupId, topic, partition);
        topicPartitionKeyMap.put(topicPartitionKey, Boolean.TRUE);
        TopicCuratorEvent topicCuratorEvent = (TopicCuratorEvent) createEvent();
        topicCuratorEvent.setServerType(ServerType.UNDERREPLICA_PARTITION_STATE);
        topicCuratorEvent.setTopic(topic);
        topicCuratorEvent.setPartition(partition);
        topicCuratorEvent.setReplica(replica);
        topicCuratorEvent.setIsr(isr);
        report(topicCuratorEvent);
    }

    private void reportTopicCuratorOKEvent(int groupId, String topic, int partition) {

        TopicPartitionKey topicPartitionKey = new TopicPartitionKey(groupId, topic, partition);
        Boolean wentWrong = topicPartitionKeyMap.get(topicPartitionKey);
        if (wentWrong != null && wentWrong) { //恢复
            TopicCuratorEvent topicCuratorEvent = (TopicCuratorEvent) createEvent();
            topicCuratorEvent.setServerType(ServerType.UNDERREPLICA_PARTITION_STATE_OK);
            topicCuratorEvent.setTopic(topic);
            topicCuratorEvent.setPartition(partition);
            report(topicCuratorEvent);
        }
        topicPartitionKeyMap.put(topicPartitionKey, Boolean.FALSE);
    }

    @Override
    public Object getMBeanValue(String host, int port, MetricName metricName, Class<?> clazz) throws IOException {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public boolean isReport(Event event) {

        if (event instanceof TopicCuratorEvent) {

            TopicCuratorEvent topicCuratorEvent = (TopicCuratorEvent) event;
            List<Integer> replica = topicCuratorEvent.getReplica();
            List<Integer> isr = topicCuratorEvent.getIsr();
            int groupId = topicCuratorEvent.getGroupId();

            if(replica == null && isr == null){
                return true;
            }

            boolean alarm = false;
            List<String> downIpList = downIpList(replica, isr, groupId);

            for (String ip : downIpList) {
                KafkaServerResource kafkaServerResource = kafkaServerResourceService.findByIp(ip);
                if (kafkaServerResource != null) {
                    alarm = alarm || kafkaServerResource.isAlarm();
                }
            }
            return alarm;
        }

        return super.isReport(event);

    }

    @Override
    public void onKafkaEvent(KafkaEvent event) {

        if (event instanceof BrokerKafkaEvent) {
            BrokerKafkaEvent brokerKafkaEvent = (BrokerKafkaEvent) event;
            List<String> downBrokerIps = brokerKafkaEvent.getDownBrokerIps();

            if (downBrokerIps == null || downBrokerIps.isEmpty()) { //ok
                String ip = event.getIp();
                String[] ips = ip.split(KafkaEvent.DELIMITOR);
                for (int i = 0; i < ips.length; ++i) {
                    downBrokers.remove(ips[i]);
                }
            } else {
                downBrokers.addAll(downBrokerIps);
            }
        }

    }

    private boolean checkReplicaSize(int groupId, List<Integer> replica) {
        List<KafkaServerResource> kafkaServerResources = kafkaServerResourceService.findByGroupId(groupId);
        if (kafkaServerResources == null) {
            return false;
        }
        return kafkaServerResources.size() >= replica.size();
    }

    private List<String> transformBrokerId(Collection<Integer> compare, int groupId) {
        List<String> ips = new ArrayList<String>();
        for (int brokerId : compare) {
            String ip = brokerId2Ip.get(new IdKey(brokerId, groupId));
            if (StringUtils.isNotBlank(ip)) {
                ips.add(ip);
            }
        }
        return ips;
    }

    private static class IdKey {

        private int groupId;

        private int brokerId;

        public IdKey(int brokerId, int groupId) {
            this.groupId = groupId;
            this.brokerId = brokerId;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public int getBrokerId() {
            return brokerId;
        }

        public void setBrokerId(int brokerId) {
            this.brokerId = brokerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IdKey idKey = (IdKey) o;

            if (groupId != idKey.groupId) return false;
            return brokerId == idKey.brokerId;

        }

        @Override
        public int hashCode() {
            int result = groupId;
            result = 31 * result + brokerId;
            return result;
        }
    }

}
