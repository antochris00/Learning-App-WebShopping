package com.backend.core.serviceImpl;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author Muhil
 * 
 * Can be used as temporary cache
 *
 */
public class CacheService {
	
	private static Logger logger = LoggerFactory.getLogger(CacheService.class);
	
	private static LoadingCache<Long, Date> loggedInStatusCache;
	
	private static LoadingCache<String, Map<String, Integer>> ipCache;
	
	static{
		loggedInStatusCache = CacheBuilder.newBuilder()
							   .build(new CacheLoader<Long, Date>() {
									@Override
									public Date load(Long key) throws Exception {
										// TODO Auto-generated method stub
										return null;
									}
								});
		
		ipCache = CacheBuilder.newBuilder()
				   .build(new CacheLoader<String, Map<String, Integer>>() {
						@Override
						public Map<String, Integer> load(String key) throws Exception {
							// TODO Auto-generated method stub
							return null;
						}
					});
	}
	
	public static void setLoggedInSatus(Long obj, Date date) {
		loggedInStatusCache.put(obj, date);
	}
	
	public static Date getLoggedInSatus(Long key) {
		try {
			return loggedInStatusCache.get(key);
		} catch (Exception e) {
			logger.error("Requested Item released from Cache - " + e.getMessage());
			return null;
		}
	}
	
	public static Map<Long, Date> getLoggedInStatusCacheMap() {
		return loggedInStatusCache.asMap();
	}
	
	public static void clearLoggedInStatus(Object key) {
		loggedInStatusCache.invalidate(key);
	}
	
//	public static void setIpCache(String ip, String requestURI, Integer count) {
//		
//		ipCache.put(ip, value);
//	}
	
	public static Integer getIpCacheCount(String ip, String requestURI) {
		int count = 0;
		try {
			Map<String, Integer> uriMap = ipCache.get(ip);
			if(uriMap != null && uriMap.containsKey(requestURI)) {
				count = uriMap.get(requestURI);
				uriMap.put(requestURI, count++);
			}
		} catch (Exception e) {
			logger.error("Requested Item released from Cache - " + e.getMessage());
			return count;
		}
		return count;
	}
	

}
