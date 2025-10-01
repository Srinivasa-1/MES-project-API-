package com.mes.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mes.project.entity.Order;
import com.mes.project.exception.OrderNotFoundException;
import com.mes.project.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Lombok: Generates logger field - DEBUG FOCUS: Centralized logging
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;

	// DEBUG FOCUS: @Value injection - check property loading and type conversion
	@Value("${app.orders.page-size:10}")
	private int defaultPageSize;

	@Value("${app.max-retry-attempts:3}")
	private int maxRetryAttempts;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public Order createOrder(Order order) {
		log.debug("Attempting to create order with number: {}", order.getOrderNumber());

		// DEBUG FOCUS: Business logic validation
		if (order.getQuantity() <= 0) {
			log.warn("Invalid quantity {} for order {}", order.getQuantity(), order.getOrderNumber());
			throw new IllegalArgumentException("Order quantity must be positive");
		}

		try {
			Order savedOrder = orderRepository.save(order);
			log.info("Successfully created order: {} with ID: {}", savedOrder.getOrderNumber(), savedOrder.getId());
			return savedOrder;
		} catch (Exception e) {
			log.error("Failed to create order: {}", order.getOrderNumber(), e);
			throw new RuntimeException("Order creation failed", e);
		}
	}

	@Transactional(readOnly = true)
	public Page<Order> getAllOrders(int page, int size, String sortBy, String sortDir) {
		log.debug("Fetching orders - page: {}, size: {}, sort: {}", page, size, sortBy);

		// DEBUG FOCUS: Pagination parameters validation
		int pageSize = (size > 0) ? size : defaultPageSize;
		int pageNumber = Math.max(page, 0);

		Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

		Page<Order> orders = orderRepository.findAll(pageable);
		log.info("Retrieved {} orders out of total {}", orders.getNumberOfElements(), orders.getTotalElements());

		return orders;
	}

	@Transactional(readOnly = true)
	public Order getOrderById(Long id) {
		log.debug("Searching for order with ID: {}", id);

		// DEBUG FOCUS: Exception handling and optional value processing
		return orderRepository.findById(id).orElseThrow(() -> {
			log.warn("Order not found with ID: {}", id);
			return new OrderNotFoundException("Order not found with id: " + id);
		});
	}

	public Order updateOrder(Long id, Order orderDetails) {
		log.debug("Updating order with ID: {}", id);

		Order existingOrder = getOrderById(id);

		// DEBUG FOCUS: State change tracking
		log.info("Order {} status changing from {} to {}", existingOrder.getOrderNumber(), existingOrder.getStatus(),
				orderDetails.getStatus());

		existingOrder.setProductName(orderDetails.getProductName());
		existingOrder.setQuantity(orderDetails.getQuantity());
		existingOrder.setStatus(orderDetails.getStatus());
		existingOrder.setCustomerName(orderDetails.getCustomerName());
		existingOrder.setPriority(orderDetails.getPriority());

		Order updatedOrder = orderRepository.save(existingOrder);
		log.info("Successfully updated order: {}", updatedOrder.getOrderNumber());

		return updatedOrder;
	}

	public void deleteOrder(Long id) {
		log.debug("Attempting to delete order with ID: {}", id);

		if (!orderRepository.existsById(id)) {
			log.error("Delete failed: Order with ID {} not found", id);
			throw new OrderNotFoundException("Order not found with id: " + id);
		}

		orderRepository.deleteById(id);
		log.info("Successfully deleted order with ID: {}", id);
	}

	// DEBUG FOCUS: Complex business logic with logging
	public Page<Order> getOrdersByStatus(Order.OrderStatus status, int page, int size) {
		log.debug("Fetching {} orders with status: {}, page: {}", size, status, page);

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Order> orders = orderRepository.findByStatus(status, pageable);

		log.info("Found {} orders with status {}", orders.getTotalElements(), status);
		return orders;
	}
}