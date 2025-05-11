package com.ocado.paymentoptimiser;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ocado.paymentoptimiser.model.Order;
import com.ocado.paymentoptimiser.model.PaymentMethod;
import com.ocado.paymentoptimiser.service.JsonReader;
import com.ocado.paymentoptimiser.service.PaymentOptimiser;
import com.ocado.paymentoptimiser.service.PaymentOptimiser.NoPaymentMethodAvailableException;

public class App {

	public static void main(String[] args) {
		
        if (args.length < 2) {
            System.err.println("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            System.exit(1);
        }
        
        String ordersPath = args[0];
        String methodsPath = args[1];
        
		List<Order> orders = new JsonReader<Order>().readJsonOrNull(ordersPath, Order.class);
        List<PaymentMethod> methods = new JsonReader<PaymentMethod>().readJsonOrNull(methodsPath, PaymentMethod.class);

        PaymentOptimiser optimizer = new PaymentOptimiser(orders, methods);

        try {
            Map<PaymentMethod, BigDecimal> result = optimizer.optimize().getResult();
            result.forEach((method, amount) -> System.out.println(method.getId() + " " + amount));
        } catch (NoPaymentMethodAvailableException e) {
        	System.out.println(e.getMessage());
        }

	}

}
