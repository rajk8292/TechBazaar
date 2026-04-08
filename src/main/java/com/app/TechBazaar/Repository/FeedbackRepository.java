package com.app.TechBazaar.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.TechBazaar.Model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

	List<Feedback> findByOrderId(Long orderId);

}
