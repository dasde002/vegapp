package com.vegetablemarket.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Keeps the payments table compatible with both the legacy mock-payment rows
 * and the Razorpay integration. This is intentionally idempotent so it is safe
 * to run on every application startup.
 */
@Component
public class PaymentSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public PaymentSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute(
                "ALTER TABLE payments ADD COLUMN IF NOT EXISTS razorpay_order_id VARCHAR(255)"
        );

        // Legacy/mock payment rows may not have a Razorpay order ID.
        jdbcTemplate.execute(
                "ALTER TABLE payments ALTER COLUMN razorpay_order_id DROP NOT NULL"
        );

        // Legacy/mock payment rows may not have a transaction ID.
        jdbcTemplate.execute(
                "ALTER TABLE payments ALTER COLUMN transaction_id DROP NOT NULL"
        );

        // Allow multiple NULL values while enforcing uniqueness for real Razorpay orders.
        jdbcTemplate.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_razorpay_order_id "
                        + "ON payments (razorpay_order_id) "
                        + "WHERE razorpay_order_id IS NOT NULL"
        );
    }
}
