package com.backend.persistence.serviceImpl;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.core.service.BaseService;
import com.backend.persistence.dao.PosDao;
import com.backend.persistence.helper.POSData;
import com.backend.persistence.service.POSService;

/**
 * @author Muhil
 *
 */
@Service
public class POSServiceImpl implements POSService {
	
	@Autowired
	private BaseService baseService;
	
	@Autowired
	private PosDao posDao;
	
	@Override
	public void createPOS(POSData data) throws Exception{
		JSONObject json = new JSONObject(data);
		json.put("tenantId", baseService.getTenantInfo().getTenantID());
		json.put("primaryKey", posDao.getPOSKEY());
		json.put("timeCreated", new Date().getTime());
		posDao.createPOS(json);
	}
	
	@Override
	public List<POSData> getPOSDATA (String mobile) throws Exception{
		return posDao.getPOS(mobile, baseService.getTenantInfo().getTenantID());
	}

}