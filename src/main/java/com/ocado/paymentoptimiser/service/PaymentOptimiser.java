package com.ocado.paymentoptimiser.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import com.ocado.paymentoptimiser.model.Order;
import com.ocado.paymentoptimiser.model.PaymentMethod;

import lombok.Getter;

public class PaymentOptimiser {
	
	private List<Order> ordersLeft = new ArrayList<>();
	private List<PaymentMethod> methodsLeft = new ArrayList<>();
	private PaymentMethod pointsMethod = null;
	
	public static final String POINTS_DISCOUNT = "PUNKTY";
	private static final float POINTS_ADDITIONAL_DISCOUNT_THRESHOLD = 0.1f;
	private static final float POINTS_ADDITIONAL_DISCOUNT_PERCENT = 0.1f;
	
	@Getter
	private Map<PaymentMethod, BigDecimal> result = new HashMap<>();
	
	public PaymentOptimiser(List<Order> orders, List<PaymentMethod> methodsList) {
    	if (orders == null || orders.isEmpty()) {
    		throw new IllegalArgumentException("Orders list cannot be null or empty!");
    	} else if (methodsList == null || methodsList.isEmpty()) {
			throw new IllegalArgumentException("Payment methods list cannot be null or empty!");
		}
    	
    	methodsList.sort(Comparator.comparing(PaymentMethod::getDiscountAsBigDecimal).reversed());

    	methodsList.forEach(method -> methodsLeft.add(method.clone()));
    	orders.forEach(order -> ordersLeft.add(order.clone()));
    	
    	pointsMethod = methodsLeft.stream().filter(method -> method.getId().equals(POINTS_DISCOUNT)).findFirst().orElse(null);
    	
	}
	
	/**
	 * Method for optimizing the payment methods for the orders.
	 * @return Optimiser object with the result
	 * @throws NoPaymentMethodAvailableException
	 */
	public PaymentOptimiser optimize() throws NoPaymentMethodAvailableException {
    	
		//Finding the most suitable payment discounts for each order to maximize the discount
    	for (PaymentMethod method : methodsLeft) {
    		
    		result.putIfAbsent(method, new BigDecimal(0).setScale(2, RoundingMode.HALF_UP));
    		
    		List<Order> mostSuitable = findMostSuitableOrder(method);
			while (method.getLimitAsBigDecimal().compareTo(BigDecimal.ZERO) > 0 && !mostSuitable.isEmpty()) {
				Order order = mostSuitable.get(0);
				mostSuitable.remove(0);
				if (method.getLimitAsBigDecimal().compareTo(order.getValueAsBigDecimal()) >= 0) {
					ordersLeft.remove(order);
					
					BigDecimal discountedValue = method.applyDiscount(order.getValueAsBigDecimal());
					
					method.changeLimit(method.getLimitAsBigDecimal().subtract(discountedValue));
					result.put(method, result.get(method).add(discountedValue).setScale(2, RoundingMode.HALF_UP));
				}
			}
    	}
    	
    	//Calculating orders left
		for (Order order : new ArrayList<>(ordersLeft)) {
			
			// Try to apply additional loyalty points discount if possible
			if (canApplyAdditionalDiscount(order)) {
				BigDecimal afterDiscountValue = order.getValueAsBigDecimal().multiply(new BigDecimal(POINTS_ADDITIONAL_DISCOUNT_THRESHOLD)).setScale(2, RoundingMode.HALF_UP);
				BigDecimal pointsLeftToSpare = pointsMethod.getLimitAsBigDecimal();
				BigDecimal afterDiscountValutWithoutPoints = afterDiscountValue.subtract(pointsLeftToSpare);
				
				Optional<PaymentMethod> method = findMethodWithLimitLeft(afterDiscountValutWithoutPoints, pointsLeftToSpare);
				if (method.isPresent()) {
					ordersLeft.remove(order);
					pointsMethod.changeLimit(pointsMethod.getLimitAsBigDecimal().subtract(pointsLeftToSpare));
					method.get().changeLimit(method.get().getLimitAsBigDecimal().subtract(afterDiscountValutWithoutPoints));

					result.put(pointsMethod, result.get(pointsMethod).add(pointsLeftToSpare));
					BigDecimal discountedValue = method.get().applyDiscount(order.getValueAsBigDecimal().multiply(new BigDecimal(POINTS_ADDITIONAL_DISCOUNT_PERCENT)));
					result.put(method.get(), result.get(method.get()).add(discountedValue).setScale(2, RoundingMode.HALF_UP));

					continue;
				}
			}
			
			// Proceed without the discount
			Optional<PaymentMethod> method = findMethodWithLimitLeft(order.getValueAsBigDecimal(), BigDecimal.ZERO);
			if (method.isPresent()) {
				method.get().changeLimit(method.get().getLimitAsBigDecimal().subtract(order.getValueAsBigDecimal()));
				ordersLeft.remove(order);
				result.put(method.get(), result.get(method.get()).add(order.getValueAsBigDecimal()).setScale(2, RoundingMode.HALF_UP));
			} else {
				throw new NoPaymentMethodAvailableException("No payment method available for order: " + order.getId() + " with value: " + order.getValueAsBigDecimal());
			}
			
		}
    	
		return this;
    	
	}
	
	/**
	 * Checking if the order can be discounted with additional points discount
	 * 
	 * @param order
	 * @return true if the order can be discounted with additional points discount,
	 *         false otherwise
	 */
	private boolean canApplyAdditionalDiscount(Order order) {
		if (pointsMethod == null) {
			return false;
		}
		
		BigDecimal minimalPointsValue = order.getValueAsBigDecimal().multiply(new BigDecimal(POINTS_ADDITIONAL_DISCOUNT_THRESHOLD));
		return pointsMethod.getLimitAsBigDecimal().compareTo(minimalPointsValue) >= 0;
	}
	
	/**
	 * Finding the payment method with limit left for the given value
	 * 
	 * @param value
	 * @param pointsLeftToSpare
	 * @return payment method with limit left for the given value
	 */
	private Optional<PaymentMethod> findMethodWithLimitLeft(BigDecimal value, BigDecimal pointsLeftToSpare) {
		return methodsLeft.stream()
				.filter(method -> !method.getId().equals(POINTS_DISCOUNT))
				.filter(method -> method.getLimitAsBigDecimal().compareTo(value) >= 0)
				.findAny();
    }
	

	/**
	 * Finding the most suitable orders for the given payment method
	 * Orders are sorted by value in descending order
	 * 
	 * @param method
	 * @return list of suitable orders diltered by promotions and limit
	 */
	private List<Order> findMostSuitableOrder(PaymentMethod method) {
		List<Order> orders = ordersLeft.stream()
				.filter(order -> order.getPromotions().contains(method.getId()))
				.filter(order -> order.getValueAsBigDecimal().compareTo(method.getLimitAsBigDecimal()) <= 0).collect(Collectors.toList());
		Collections.sort(orders, Comparator.comparing(Order::getValueAsBigDecimal).reversed());
		return orders;
	}
	
	
	/**
	 * Exception thrown when no payment method is available for the order
	 */
	public class NoPaymentMethodAvailableException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public NoPaymentMethodAvailableException(String message) {
			super(message);
		}
	}

}
