package org.example.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Payment {
// Set your secret key
static {
    try {
        Properties props = new Properties();
        props.load(new FileInputStream(".env"));
        Stripe.apiKey = props.getProperty("STRIPE_SECRET_KEY");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public String createCheckoutSession(long priceLong) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setSuccessUrl("https://your-site.com/success")
                    .setCancelUrl("https://your-site.com/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(priceLong) // Amount in cents
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Abonnement")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();

        } catch (StripeException e) {
            e.printStackTrace();
            return null;
        }
    }
}
