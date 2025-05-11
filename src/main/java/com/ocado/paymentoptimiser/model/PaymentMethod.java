package com.ocado.paymentoptimiser.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Getter;


/**
 * Class representing a payment method.
 */
public class PaymentMethod implements ISerializable, Cloneable {
	@Getter
    private String id;
	
	private String discount;
	@Getter private BigDecimal discountAsBigDecimal;
	
	private String limit;
	@Getter private BigDecimal limitAsBigDecimal;
	
	public PaymentMethod(String id, BigDecimal discountAsBigDecimal, BigDecimal limitAsBigDecimal) {
		this.id = id;
		this.discountAsBigDecimal = discountAsBigDecimal;
		this.discount = discountAsBigDecimal.toString();
		this.limitAsBigDecimal = limitAsBigDecimal;
		this.limit = limitAsBigDecimal.toString();
	}

	@Override
	public void afterDeserialization() {
		discountAsBigDecimal = new BigDecimal(discount.replaceAll("[^0-9.]", "")).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
		limitAsBigDecimal = new BigDecimal(limit.replaceAll("[^0-9.]", "")).setScale(2, RoundingMode.HALF_UP);
	}
	
	/**
	 * Method for changing the limit of the payment method.
	 * @param newLimit
	 */
	public void changeLimit(BigDecimal newLimit) {
		this.limitAsBigDecimal = newLimit.setScale(2, RoundingMode.HALF_UP);
		this.limit = newLimit.toString();
	}
	
	/**
	 * Method for applying discount to the value.
	 * @param value
	 * @return discounted value
	 */
	public BigDecimal applyDiscount(BigDecimal value) {
		return value.subtract(value.multiply(discountAsBigDecimal));
	}
	
	
	@Override
	public String toString() {
		return "PaymentMethod [id=" + id + ", discount=" + discount + ", limit=" + limit + "]";
	}
	
	@Override
	public PaymentMethod clone() {
		return new PaymentMethod(id, new BigDecimal(discount).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP), new BigDecimal(limit).setScale(2, RoundingMode.HALF_UP));
	}
}
