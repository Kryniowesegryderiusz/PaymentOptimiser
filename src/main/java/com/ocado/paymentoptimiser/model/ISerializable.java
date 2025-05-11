package com.ocado.paymentoptimiser.model;

/**
 * Interface for classes that need to perform actions after deserialization.
 */
public interface ISerializable {

	void afterDeserialization();

}
