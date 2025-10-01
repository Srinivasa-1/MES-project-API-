package com.mes.project.repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mes.project.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
	Page findByStatus(@Param("status") Order.OrderStatus status, Pageable pageable);

	@Query(value = "SELECT * FROM orders o WHERE o.created_at BETWEEN :startDate AND :endDate "
			+ "AND o.quantity > :minQuantity", nativeQuery = true)
	List<Order> findOrdersByDateRangeAndQuantity(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate, @Param("minQuantity") Integer minQuantity);

	Page findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);

	@Query("SELECT o FROM Order o WHERE o.priority = :priority AND o.status IN :statuses")
	Page findByPriorityAndStatusIn(@Param("priority") String priority,
			@Param("statuses") List<Order.OrderStatus> statuses, Pageable pageable);
}
