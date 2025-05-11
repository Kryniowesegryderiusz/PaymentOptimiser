package com.ocado.paymentoptimiser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ocado.paymentoptimiser.model.Order;
import com.ocado.paymentoptimiser.model.PaymentMethod;
import com.ocado.paymentoptimiser.service.JsonReader;
import com.ocado.paymentoptimiser.service.PaymentOptimiser;

public class PaymentOptimiserTest {
	
    private List<Order> orders;
    private List<PaymentMethod> methods;

	
    @BeforeEach
    public void setup() {
		orders = new JsonReader<Order>().readJsonOrNull(Paths.get("src/test/resources/orders.json").toString(), Order.class);
        methods = new JsonReader<PaymentMethod>().readJsonOrNull(Paths.get("src/test/resources/paymentmethods.json").toString(), PaymentMethod.class);
    }
    
    @Test
    public void testNull() {
    	assertThrows(IllegalArgumentException.class, () -> new PaymentOptimiser(null, methods));
    	assertThrows(IllegalArgumentException.class, () -> new PaymentOptimiser(orders, null));
    }
	
	@Test
	public void testResultDoesNotExceedMethodLimits() {		
        PaymentOptimiser optimizer = new PaymentOptimiser(orders, methods);
        Map<PaymentMethod, BigDecimal> result = optimizer.optimize().getResult();

		for (Map.Entry<PaymentMethod, BigDecimal> entry : result.entrySet()) {
			
			PaymentMethod method = entry.getKey();
			BigDecimal total = entry.getValue();

			PaymentMethod originalMethod = methods.stream().filter(m -> method.getId().equals(m.getId())).findFirst().get();
			assertTrue(originalMethod.getLimitAsBigDecimal().compareTo(total) >= 0);
		}
	}
    
    @Test
    public void testPointsDiscountAppliedWhenThresholdMet() {
        List<Order> orders = new ArrayList<>(Arrays.asList(
                new Order("1", new BigDecimal("50.00"), Arrays.asList("PUNKTY"))
            ));

            List<PaymentMethod> methods = new ArrayList<>(Arrays.asList(
                new PaymentMethod("PUNKTY", new BigDecimal("0.05"), new BigDecimal("10.00")),
                new PaymentMethod("CARD", new BigDecimal("0.10"), new BigDecimal("50.00"))
            ));

        PaymentOptimiser optimiser = new PaymentOptimiser(orders, methods);
        optimiser.optimize();

        Map<PaymentMethod, BigDecimal> result = optimiser.getResult();
        assertTrue(result.keySet().stream().anyMatch(method -> method.getId().equals("PUNKTY")));
    }

}
