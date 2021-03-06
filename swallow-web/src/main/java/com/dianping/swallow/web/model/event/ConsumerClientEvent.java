package com.dianping.swallow.web.model.event;

import org.apache.commons.lang.StringUtils;

import com.dianping.swallow.web.model.alarm.AlarmMeta;
import com.dianping.swallow.web.model.alarm.AlarmType;
import com.dianping.swallow.web.util.DateUtil;

public class ConsumerClientEvent extends ClientEvent {

    private String consumerId;

    public String getConsumerId() {
        return consumerId;
    }

    public ConsumerClientEvent setConsumerId(String consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    @Override
    public String getMessage(String template) {
        String message = template;
        if (StringUtils.isNotBlank(message)) {
            message = StringUtils.replace(message, AlarmMeta.TOPIC_TEMPLATE, getTopicName());
            message = StringUtils.replace(message, AlarmMeta.CONSUMERID_TEMPLATE, getConsumerId());
            message = StringUtils.replace(message, AlarmMeta.IP_TEMPLATE, getIp());
            message = StringUtils.replace(message, AlarmMeta.DATE_TEMPLATE, DateUtil.getDefaulFormat());
            message = StringUtils.replace(message, AlarmMeta.CHECKINTERVAL_TEMPLATE, getCheckIntervalBySecends());
        }
        return message;
    }

    @Override
    public boolean isSendAlarm(AlarmType alarmType, AlarmMeta alarmMeta) {
        String key = getTopicName() + KEY_SPLIT + getConsumerId() + KEY_SPLIT + getIp() + KEY_SPLIT
                + alarmType.getNumber();
        return isAlarm(lastAlarms, key, alarmMeta);
    }

    @Override
    public String toString() {
        return "ConsumerClientEvent [consumerId=" + consumerId + super.toString() + "]";
    }
}
