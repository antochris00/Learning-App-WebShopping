package com.backend.persistence.serviceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.commons.service.EmailService;
import com.backend.commons.util.CommonUtil;
import com.backend.commons.util.FileUtil;
import com.backend.commons.util.OrdersUtil;
import com.backend.core.configuration.PaymentModes;
import com.backend.core.entity.EmployeeInfo;
import com.backend.core.service.BaseService;
import com.backend.core.util.DashboardStatusUtil;
import com.backend.persistence.dao.CartDao;
import com.backend.persistence.dao.OrdersDao;
import com.backend.persistence.entity.Coupons;
import com.backend.persistence.entity.CustomerCart;
import com.backend.persistence.entity.CustomerInfo;
import com.backend.persistence.entity.OrderDetails;
import com.backend.persistence.entity.OrderInvoice;
import com.backend.persistence.entity.Orders;
import com.backend.persistence.entity.Product;
import com.backend.persistence.repository.OrderDetailsRepository;
import com.backend.persistence.repository.OrdersRepository;
import com.backend.persistence.repository.ProductRepository;
import com.backend.persistence.service.CouponsService;
import com.backend.persistence.service.CustomerInfoService;
import com.backend.persistence.service.InvoiceService;
import com.backend.persistence.service.OrdersService;
import com.backend.persistence.service.ProductNotificationService;

/**
 * @author Muhil
 *
 */
@Service
@Transactional
public class OrdersServiceImpl implements OrdersService {
	
	private Logger logger = LoggerFactory.getLogger(OrdersService.class);

	@Autowired
	private BaseService baseService;

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private OrdersRepository ordersRepo;

	@Autowired
	private OrderDetailsRepository orderDetailsRepo;

	@Autowired
	private CouponsService couponService;

	@Autowired
	private CartDao cartDao;
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private CustomerInfoService customerService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ProductNotificationService productNotification;

	@Override
	public void save(Orders order) {
		ordersRepo.save(order);
	}

	@Override
	public void saveAndFlush(Orders order) {
		ordersRepo.saveAndFlush(order);
	}

	@Override
	public void saveAndFlush(OrderDetails orderDetail) {
		orderDetailsRepo.saveAndFlush(orderDetail);
	}

	private void createUnassignedOrder(Long orderId) throws Exception {
		ordersDao.insertUnassignedOrder(orderId);
	}

	private void removeUnassignedOrder(Long orderId) throws Exception {
		ordersDao.removeUnassignedOrders(orderId);
	}

	@Override
	public List<Integer> getAllUnassignedOrders() throws Exception {
		return ordersDao.getUnassignedOrders();
	}
	
	@Override
	public int getAllUnassignedOrdersCount() throws Exception{
		return ordersDao.getUnassignedOrdersCount();
	}

	@Override
	public void createCustomerOrder(Long couponId, int paymentMode, Long addressId, int deliveryCharge) throws Exception {
		Coupons coupon = couponService.findCouponById(couponId);
		CustomerInfo customer = (CustomerInfo) baseService.getUserInfo();
		// create initial order object
		Orders order = new Orders();
		order.setOrderDate(CommonUtil.convertToUTC(new Date().getTime()));
		order.setStatus(OrdersUtil.orderStatus.Pending.toString());
		order.setPaymentModeId(paymentMode);
		order.setCustomerId(customer.getCustomerId());
		order.setCustomerAddressId(addressId);
		order.setDeliveryCharge(deliveryCharge);
		order.setTenant(baseService.getTenantInfo());
		if (coupon != null) {
			order.setCouponId(coupon.getCouponId());
			order.setCouponDiscount(coupon.getDiscount());
			order.setCouponapplied(true);
		} else {
			order.setCouponapplied(false);
		}
		ordersRepo.saveAndFlush(order);

		List<CustomerCart> cartItems = cartDao.userCartItems(customer.getCustomerId());
		BigDecimal subTotal = new BigDecimal(0);
		List<OrderDetails> orderDetails = new ArrayList<OrderDetails>();
		for (CustomerCart item : cartItems) {
			OrderDetails detail = new OrderDetails();
			detail.setOrder(order);
			detail.setProduct(item.getProduct());
			detail.setQuantity(item.getQuantity());
			detail.setTenant(baseService.getTenantInfo());
			orderDetailsRepo.save(detail);
			Product product = item.getProduct();
			BigDecimal total = new BigDecimal(0);
			total = product.getSellingCost().multiply(new BigDecimal(item.getQuantity()));
			subTotal = subTotal.add(total);
			orderDetails.add(detail);
			product.setQuantityInStock(product.getQuantityInStock()-item.getQuantity());
			productRepo.save(product);
			if (product.getQuantityInStock() < 1) {
				productNotification.createNotification(product.getProductName() + " Ran Out of Stock !",
						product.getProductId(), 0L, null);
			}
			else if (product.getQuantityInStock() <= 3) {
				productNotification.createNotification(product.getProductName() + " Running Out of Stock ! ("
						+ product.getQuantityInStock() + ")", product.getProductId(), 0L, null);
			}
		}
		if (coupon != null) {
			BigDecimal offerAmout = subTotal.multiply(new BigDecimal(coupon.getDiscount())).divide(new BigDecimal(100));
			if (offerAmout.compareTo(new BigDecimal(coupon.getMaxDiscountLimit())) > 0) {
				subTotal = subTotal.subtract(new BigDecimal(coupon.getMaxDiscountLimit()));
			} else {
				subTotal = subTotal.subtract(offerAmout);
			}
		}
		subTotal = subTotal.add(new BigDecimal(deliveryCharge));
		order.setSubTotal(subTotal.setScale(2, RoundingMode.CEILING));
		order.setOrderDetails(orderDetails);
		ordersRepo.saveAndFlush(order);
		// create unassigned order to be picked up by a employee.
		createUnassignedOrder(order.getOrderId());
		customerService.clearCustomerCart();
		OrderInvoice invoice = invoiceService.createOrderInvoice(order);
		customerService.updateLoyalityPointByCustomerMobile(customer.getMobile(), subTotal.floatValue());
		emailService.sendOrderStatusEmail(String.valueOf(order.getOrderId()), order.getStatus(), order.getSubTotal().toString(),
				order.getOrderDate(), PaymentModes.paymentModes.get(order.getPaymentModeId()), customer.getEmailId(),
				customer.getFirstName(), customer.getLastName(), baseService.getOrigin(), invoice.getDocument());
		emailService.sendOrderAlertMailToAdmin();
		DashboardStatusUtil.incremenOnlineCount(baseService.getTenantInfo());
	}

	@Override
	public List<Orders> getOrders(int limit, int offset) {
		return ordersRepo.findLimitedOrders(baseService.getTenantInfo().getTenantID(), limit, offset);
	}
	
	@Override
	public int getOrdersCount(String limit, String offset, String condition, long date, String status)
			throws Exception {
		if (date == 0L) {
			condition = "def";
		}
		return ordersDao.getOrdersCount(baseService.getTenantInfo().getTenantID(), limit, offset, condition, date,
				status);
	}
	
	@Override
	public List<Orders> getOrders(String limit, String offset, String condition, long date, String status) throws Exception {
		if(date == 0L) {
			condition = "def";
		}
		List<Long> orderIds = ordersDao.getOrders(baseService.getTenantInfo().getTenantID(), limit, offset, condition, date, status);
		List<Orders> orders = new ArrayList<Orders>();
		orderIds.stream().forEach(orderId -> {
			orders.add(ordersRepo.findOrdersById(baseService.getTenantInfo(), orderId));
		});
		return orders;
	}
	
	@Override
	public List<Orders> getOrdersAssignedForEmployee(String status) {
		return ordersRepo.findOrdersByStatusForEmployee(baseService.getTenantInfo(),
				((EmployeeInfo) baseService.getUserInfo()).getEmployeeId(), status);
	}
	
	@Override
	public List<Orders> getCustomerOrders() {
		return ordersRepo.findCustomerOrders(baseService.getTenantInfo().getTenantID(),
				((CustomerInfo) baseService.getUserInfo()).getCustomerId());
	}
	
	@Override
	public void updateOrderStatus(String status, Long orderId) throws Exception {
		Orders order = ordersRepo.findOrdersById(baseService.getTenantInfo(), orderId);
		if (order != null) {
			// incase of order accepted by a employee assign the task to that employee
			if (order.getEmployeeId() == null) {
				EmployeeInfo emp = (EmployeeInfo) baseService.getUserInfo();
				order.setEmployeeId(emp.getEmployeeId());
				ordersRepo.saveAndFlush(order);
				removeUnassignedOrder(order.getOrderId());
			}
			switch (status.toLowerCase()) {
			case "accepted":
				order.setStatus(OrdersUtil.orderStatus.Accepted.toString());
				break;
			case "cancelled":
				order.setStatus(OrdersUtil.orderStatus.Cancelled.toString());
				DashboardStatusUtil.decrementOnlineCount(baseService.getTenantInfo());
				break;
			case "outfordelivery":
				order.setStatus(OrdersUtil.orderStatus.OutForDelivery.toString());
				break;
			case "shipped":
				order.setStatus(OrdersUtil.orderStatus.Shipped.toString());
				break;
			case "delayed":
				order.setStatus(OrdersUtil.orderStatus.Delayed.toString());
				break;
			case "delivered":
				order.setStatus(OrdersUtil.orderStatus.Delivered.toString());
				break;
			default:
				order.setStatus(OrdersUtil.orderStatus.Pending.toString());
				break;
			}
			ordersRepo.saveAndFlush(order);
			CustomerInfo customer = customerService.getCustomerById(order.getCustomerId());
			Blob invoiceBlob= null;
			//send invoice again incase of delivered status.
			if (order.getStatus().equals(OrdersUtil.orderStatus.Delivered.toString())) {
				OrderInvoice invoice = invoiceService.getInvoiceByOrder(order);
				invoiceBlob = invoice != null ? invoice.getDocument() : null;
			}
			emailService.sendOrderStatusEmail(String.valueOf(order.getOrderId()), order.getStatus(),
					order.getSubTotal().toString(), order.getOrderDate(),
					PaymentModes.paymentModes.get(order.getPaymentModeId()), customer.getEmailId(),
					customer.getFirstName(), customer.getLastName(), baseService.getOrigin(), invoiceBlob);
		}
	}
	
	@Override
	public Map<String, BigDecimal> ordersWeeklyReport() throws Exception{
		return ordersDao.getOrdersWeeklyTotal(baseService.getTenantInfo().getTenantID());
	}
	
	@Override
	public int couponAppliedCount(long couponId) {
		CustomerInfo customer = (CustomerInfo)baseService.getUserInfo();
		return ordersRepo.findCouponAppliedCount(baseService.getTenantInfo(), customer.getCustomerId(), couponId);
	}

	@Override
	public File getOrderInvoice(Long id) throws Exception {
		Orders order = ordersRepo.findOrdersById(baseService.getTenantInfo(), id);
		if (order != null) {
			OrderInvoice invoice = invoiceService.getInvoiceByOrder(order);
			File tempFile = File.createTempFile("Invoice-" + order.getOrderId(), CommonUtil.Document_Extention);
			InputStream in = invoice.getDocument().getBinaryStream();
			OutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(in, out);
			File pdfFile = FileUtil.convertDocToPDF(tempFile);
			//flush
			CommonUtil.deleteDirectoryOrFile(tempFile);
			return pdfFile;
		}
		return null;
	}
	
}
