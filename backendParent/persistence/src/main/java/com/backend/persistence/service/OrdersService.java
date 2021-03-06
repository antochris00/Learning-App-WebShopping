package com.backend.persistence.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.backend.persistence.entity.OrderDetails;
import com.backend.persistence.entity.Orders;

public interface OrdersService {

	void save(Orders order);

	void saveAndFlush(Orders order);

	void saveAndFlush(OrderDetails orderDetail);

	List<Integer> getAllUnassignedOrders() throws Exception;

	List<Orders> getOrders(int limit, int offset);

	void updateOrderStatus(String status, Long orderId) throws Exception;

	int getAllUnassignedOrdersCount() throws Exception;

	List<Orders> getOrdersAssignedForEmployee(String status);

	List<Orders> getOrders(String limit, String offset, String condition, long date, String status) throws Exception;

	Map<String, BigDecimal> ordersWeeklyReport() throws Exception;

	List<Orders> getCustomerOrders();

	void createCustomerOrder(Long couponId, int paymentMode, Long addressId, int deliveryCharge) throws Exception;

	int couponAppliedCount(long couponId);

	File getOrderInvoice(Long id) throws Exception;

	int getOrdersCount(String limit, String offset, String condition, long date, String status) throws Exception;

}
