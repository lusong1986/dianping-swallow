package com.dianping.swallow.web.service;

import com.dianping.swallow.web.common.Pair;
import com.dianping.swallow.web.model.resource.TopicApplyResource;

import java.util.List;

/**
 * Author   mingdongli
 * 15/11/20  上午11:44.
 */
public interface TopicApplyService {

    boolean insert(TopicApplyResource topicApplyResource);

    List<TopicApplyResource> find(String topic, int offset, int limit);

    Pair<Long, List<TopicApplyResource>> findTopicApplyResourcePage(int offset, int limit);
}
