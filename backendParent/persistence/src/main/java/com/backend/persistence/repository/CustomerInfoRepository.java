package com.backend.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.core.entity.Tenant;
import com.backend.persistence.entity.CustomerInfo;

/**
 * @author Muhil
 *
 */
@Repository
public interface CustomerInfoRepository extends JpaRepository<CustomerInfo, Long> {

	String findEmployeeByEmailQuery = "select cus from CustomerInfo cus where cus.emailId = :emailId and cus.tenant = :tenant";
	String findEmployeeByMobileQuery = "select cus from CustomerInfo cus where cus.mobile = :mobile and cus.tenant = :tenant";
	String findEmployeeByIdQuery = "select cus from CustomerInfo cus where cus.customerId = :customerId and cus.tenant = :tenant";

	@Query(findEmployeeByEmailQuery)
	CustomerInfo findEmployeeByEmail(@Param("emailId") String emailId, @Param("tenant") Tenant realm);
	
	@Query(findEmployeeByMobileQuery)
	CustomerInfo findEmployeeByMobile(@Param("mobile") String mobile, @Param("tenant") Tenant realm);
	
	@Query(findEmployeeByIdQuery)
	CustomerInfo findEmployeeById(@Param("customerId") Long customerId, @Param("tenant") Tenant realm);
	
	
	//done by Chris
	String findLimitedCustomersQuery = "select * from  CustomerInfo where tenantid = ?1 limit ?2 offset ?3";
	String findAllCustomersQuery = "select cus from CustomerInfo cus where cus.tenant = :tenant";
	String findAllCustomersCountQuery = "select count(*) from CustomerInfo cus where cus.tenant = :tenant";
	
	@Query(value = findLimitedCustomersQuery, nativeQuery = true)
	List<CustomerInfo> findLimitedCustomers(String tenant, int limit, int offset);
	
	@Query(findAllCustomersQuery)
	List<CustomerInfo> findAllCustomers(@Param("tenant") Tenant realm);
	
	@Query(findAllCustomersCountQuery)
	int findAllCustomersCount(@Param("tenant") Tenant realm);
	
	
				
}
