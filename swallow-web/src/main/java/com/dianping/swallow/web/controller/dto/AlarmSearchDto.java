package com.dianping.swallow.web.controller.dto;
/**
 * 
 * @author qiyin
 *
 * 2015年8月9日 下午4:11:00
 */
public class AlarmSearchDto {
	private int offset;

	private int limit;

	private String receiver;

	private RelatedTypeDto relatedType;

	private String relatedInfo;

	private String startTime;

	private String endTime;

	public AlarmSearchDto() {

	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public RelatedTypeDto getRelatedType() {
		return relatedType;
	}

	public void setRelatedType(RelatedTypeDto relatedType) {
		this.relatedType = relatedType;
	}

	public String getRelatedInfo() {
		return relatedInfo;
	}

	public void setRelatedInfo(String relatedInfo) {
		this.relatedInfo = relatedInfo;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
