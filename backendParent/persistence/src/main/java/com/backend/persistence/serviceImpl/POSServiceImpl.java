package com.backend.persistence.serviceImpl;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.commons.util.CommonUtil;
import com.backend.core.service.BaseService;
import com.backend.core.util.DBUtil;
import com.backend.core.util.TenantUtil;
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
		data.setTimeCreated(CommonUtil.convertToUTC(data.getTimeCreated()));
		JSONObject json = new JSONObject(data);
		json.put(TenantUtil.Key_TenantId, baseService.getTenantInfo().getTenantID());
		json.put(DBUtil.Key_PrimaryKey, posDao.getPOSKEY());
		posDao.createPOS(json);
	}
	
	@Override
	public List<POSData> getPOSDATA (String mobile) throws Exception{
		return posDao.getPOS(mobile, baseService.getTenantInfo().getTenantID());
	}
	
	@Override
	public List<POSData> getPOSDATA (String limit, String offset) throws Exception{
		return posDao.getPOS(baseService.getTenantInfo().getTenantID(), limit, offset);
	}

}
