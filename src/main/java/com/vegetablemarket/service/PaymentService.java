package com.vegetablemarket.service;

import com.vegetablemarket.dto.PaymentResponse;
import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.OrderStatus;
import com.vegetablemarket.entity.Payment;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.OrderRepository;
import com.vegetablemarket.repository.PaymentRepository;
import com.vegetablemarket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(UserRepository userRepository,
                          OrderRepository orderRepository,
                          PaymentRepository paymentRepository,
                          PaymentGateway paymentGateway) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResponse createMockPayment(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        validateOrderAccessAndPaymentState(user, order);

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Payment already exists for this order");
        }

        PaymentGatewayResult result = paymentGateway.createPayment(
                order.getId(), order.getTotalAmount());

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setProvider("MOCK");
        payment.setStatus(result.status());
        payment.setTransactionId(result.transactionId());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getPayment(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toResponse(payment);
    }

    private void validateOrderAccessAndPaymentState(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cancelled orders cannot be paid");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Delivered orders cannot be paid");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getOrder().getId(),
                payment.getAmount(), payment.getStatus(), payment.getProvider(),
                payment.getTransactionId(), payment.getRazorpayOrderId());
    }
}
