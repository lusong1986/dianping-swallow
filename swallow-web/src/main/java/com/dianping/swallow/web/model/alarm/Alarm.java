package com.dianping.swallow.web.model.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author qiyin
 *
 *         2015年9月8日 下午4:22:39
 */
@Document(collection = "ALARM")
public class Alarm implements Cloneable {

	@Id
	private String id;

	@Indexed(name = "IX_EVENTID", direction = IndexDirection.DESCENDING)
	private long eventId;

	private int number;

	private AlarmLevelType type;

	private String title;

	private String body;

	@Indexed(name = "IX_RELATED", direction = IndexDirection.ASCENDING)
	private String related;

	private String subRelated;

	private RelatedType relatedType;

	private List<SendInfo> sendInfos;

	@Indexed(name = "IX_CREATETIME", direction = IndexDirection.DESCENDING)
	private Date createTime;

	private String sourceIp;

	public Alarm() {

	}

	public String getId() {
		return id;
	}

	public Alarm setId(String id) {
		this.id = id;
		return this;
	}

	public AlarmLevelType getType() {
		return type;
	}

	public Alarm setType(AlarmLevelType type) {
		this.type = type;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Alarm setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getBody() {
		return body;
	}

	public Alarm setBody(String body) {
		this.body = body;
		return this;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public Alarm setCreateTime(Date createTime) {
		this.createTime = createTime;
		return this;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public Alarm setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
		return this;
	}

	public long getEventId() {
		return eventId;
	}

	public Alarm setEventId(long eventId) {
		this.eventId = eventId;
		return this;
	}

	public int getNumber() {
		return number;
	}

	public Alarm setNumber(int number) {
		this.number = number;
		return this;
	}

	public String getRelated() {
		return related;
	}

	public Alarm setRelated(String related) {
		this.related = related;
		return this;
	}

	public RelatedType getRelatedType() {
		return relatedType;
	}

	public Alarm setRelatedType(RelatedType relatedType) {
		this.relatedType = relatedType;
		return this;
	}

	public String getSubRelated() {
		return subRelated;
	}

	public Alarm setSubRelated(String subRelated) {
		this.subRelated = subRelated;
		return this;
	}

	public List<SendInfo> getSendInfos() {
		return sendInfos;
	}

	public void setSendInfos(List<SendInfo> sendInfos) {
		this.sendInfos = sendInfos;
	}

	public void addSendInfo(SendInfo sendInfo) {
		if (this.sendInfos == null) {
			synchronized (this) {
				if (this.sendInfos == null) {
					this.sendInfos = new ArrayList<SendInfo>();
				}
			}
		}
		this.sendInfos.add(sendInfo);
	}

	@Override
	public String toString() {
		return "Alarm [id=" + id + ", eventId=" + eventId + ", number=" + number + ", type=" + type + ", title="
				+ title + ", body=" + body + ", related=" + related + ", subRelated=" + subRelated + ", relatedType="
				+ relatedType + ", sendInfos=" + sendInfos + ", createTime=" + createTime + ", sourceIp=" + sourceIp
				+ "]";
	}

}
