package com.ocado.paymentoptimiser.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.ocado.paymentoptimiser.service.PaymentOptimiser;

import lombok.Getter;

/**
 * Class representing an order.
 */
public class Order implements ISerializable, Cloneable {
	@Getter private String id;
	
	private String value;
	@Getter private BigDecimal valueAsBigDecimal;
	
	@Getter private List<String> promotions;
	
	public Order(String id, BigDecimal valueAsBigDecimal, List<String> promotions) {
		this.id = id;
		this.valueAsBigDecimal = valueAsBigDecimal;
		this.value = valueAsBigDecimal.toString();
		this.promotions = promotions;
	}

	@Override
	public void afterDeserialization() {
		valueAsBigDecimal = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
		if (promotions == null) {
			promotions = List.of(PaymentOptimiser.POINTS_DISCOUNT);
		} else {
			promotions.add(PaymentOptimiser.POINTS_DISCOUNT);
		}
	}
	
	@Override
	public String toString() {
		return "Order [id=" + id + ", value=" + value + ", promotions=" + promotions + "]";
	}
	
	@Override
	public Order clone() {
        return new Order(id, new BigDecimal(value).setScale(2, RoundingMode.HALF_UP), promotions);
	}
}
