package com.vegetablemarket.service;

import com.vegetablemarket.dto.PaymentResponse;
import com.vegetablemarket.dto.RazorpayOrderResponse;
import com.vegetablemarket.dto.RazorpayVerifyRequest;
import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.OrderItem;
import com.vegetablemarket.entity.OrderItemStatus;
import com.vegetablemarket.entity.OrderStatus;
import com.vegetablemarket.entity.Payment;
import com.vegetablemarket.entity.PaymentStatus;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.OrderRepository;
import com.vegetablemarket.repository.PaymentRepository;
import com.vegetablemarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@Service
public class RazorpayService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayService(UserRepository userRepository,
                           OrderRepository orderRepository,
                           PaymentRepository paymentRepository,
                           @Value("${razorpay.key-id:}") String keyId,
                           @Value("${razorpay.key-secret:}") String keySecret,
                           RestClient.Builder restClientBuilder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.restClient = restClientBuilder.baseUrl("https://api.razorpay.com/v1").build();
    }

    @Transactional
    public RazorpayOrderResponse createOrder(String email, Long orderId) {
        requireConfigured();

        User user = findUser(email);
        Order order = findOrder(orderId);
        validateOrderAccess(user, order);

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException("Only placed orders can be paid");
        }

        Payment existingPayment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (existingPayment != null) {
            if (existingPayment.getStatus() == PaymentStatus.SUCCESS) {
                throw new RuntimeException("Order is already paid");
            }
            if (existingPayment.getRazorpayOrderId() != null) {
                return new RazorpayOrderResponse(
                        existingPayment.getId(), orderId, existingPayment.getRazorpayOrderId(),
                        toPaise(existingPayment.getAmount()), "INR", keyId, existingPayment.getStatus().name());
            }
            throw new RuntimeException("Payment already exists for this order");
        }

        long amountInPaise = toPaise(order.getTotalAmount());
        String receipt = "VEG-" + orderId + "-" + System.currentTimeMillis();

        Map<String, Object> request = Map.of(
                "amount", amountInPaise,
                "currency", "INR",
                "receipt", receipt
        );

        final Map<?, ?> response;
        try {
            response = restClient.post()
                    .uri("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .body(request)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Razorpay order creation failed (HTTP "
                    + e.getStatusCode().value() + "): " + e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to connect to Razorpay while creating the order: "
                    + (e.getMessage() == null ? "unknown error" : e.getMessage()));
        }

        if (response == null || response.get("id") == null) {
            throw new RuntimeException("Razorpay did not return an order ID");
        }

        String razorpayOrderId = response.get("id").toString();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setProvider("RAZORPAY");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        try {
            paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Razorpay order was created (" + razorpayOrderId
                    + "), but the payment could not be saved in the database. "
                    + "Check the payments table constraints.");
        }

        return new RazorpayOrderResponse(
                payment.getId(), orderId, razorpayOrderId, amountInPaise, "INR", keyId, "created");
    }

    @Transactional
    public PaymentResponse verifyPayment(String email, RazorpayVerifyRequest request) {
        requireConfigured();

        User user = findUser(email);

        // Locate the local payment using the Razorpay order ID that was created
        // server-side. We still compare it explicitly below before verification.
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found for Razorpay order"));

        Order order = payment.getOrder();
        validateOrderAccess(user, order);

        String storedRazorpayOrderId = payment.getRazorpayOrderId();
        if (storedRazorpayOrderId == null || storedRazorpayOrderId.isBlank()) {
            throw new RuntimeException("Razorpay order ID is missing for this payment");
        }

        if (!storedRazorpayOrderId.equals(request.getRazorpayOrderId())) {
            throw new RuntimeException("Razorpay order ID mismatch");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (request.getRazorpayPaymentId().equals(payment.getTransactionId())) {
                return toResponse(payment);
            }
            throw new RuntimeException("Payment is already completed");
        }

        if (!isValidSignature(storedRazorpayOrderId,
                request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            throw new RuntimeException("Invalid Razorpay payment signature");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(request.getRazorpayPaymentId());
        payment.setUpdatedAt(LocalDateTime.now());

        // A successful payment confirms every still-active item in the order.
        // Seller-specific shipping/delivery status is then managed independently.
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() == null || item.getStatus() == OrderItemStatus.PLACED) {
                item.setStatus(OrderItemStatus.CONFIRMED);
            }
        }
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return toResponse(paymentRepository.save(payment));
    }

    private boolean isValidSignature(String razorpayOrderId, String paymentId, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((razorpayOrderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(digest);
            return java.security.MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Unable to verify Razorpay payment signature");
        }
    }

    private void requireConfigured() {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new RuntimeException("Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private void validateOrderAccess(User user, Order order) {
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

    private long toPaise(Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Invalid order amount");
        }
        return Math.round(amount * 100.0);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getOrder().getId(), payment.getAmount(),
                payment.getStatus(), payment.getProvider(), payment.getTransactionId(),
                payment.getRazorpayOrderId());
    }
}
