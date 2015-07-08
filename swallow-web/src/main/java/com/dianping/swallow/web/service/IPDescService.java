package com.dianping.swallow.web.service;

import java.util.List;

import com.dianping.swallow.web.model.cmdb.IPDesc;

/**
 * 
 * @author qiyin
 *
 */
public interface IPDescService {

	/**
	 * insert
	 * 
	 * @param ipDesc
	 * @return
	 */
	public boolean insert(IPDesc ipDesc);

	/**
	 * update
	 * 
	 * @param ipDesc
	 * @return
	 */
	public boolean update(IPDesc ipDesc);

	/**
	 * delete by id
	 * 
	 * @param ipDesc
	 * @return
	 */
	public int deleteById(String id);

	/**
	 * delete by ip
	 * 
	 * @param ipDesc
	 * @return
	 */
	public int deleteByIp(String id);

	/**
	 * find by ip
	 * 
	 * @param ipDesc
	 * @return
	 */
	public IPDesc findByIp(String ip);

	/**
	 * find by id
	 * 
	 * @param ipDesc
	 * @return
	 */
	public IPDesc findById(String id);

	/**
	 * find all
	 * 
	 * @param ipDesc
	 * @return
	 */
	public List<IPDesc> findAll();

}
