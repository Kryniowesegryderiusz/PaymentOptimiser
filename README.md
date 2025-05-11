# Payment Optimiser

Java application for optimizing payment distribution across multiple payment methods.

## Features
- Processes orders with different payment methods
- Applies discounts and respects payment limits
- Special handling for loyalty points (loyalty points prioritised, 10% discount if 10% of order was paid with loyalty points)
- Input via JSON files
- Outputs optimized payment allocation

## Technology Stack
- **Programming Language**: Java 17  
- **Build Tool**: Maven  
- **Testing Framework**: JUnit 5  
- **JSON Parsing**: Gson  
- **Boilerplate Reduction**: Lombok
- **Runtime Environment**: Command Line Interface (CLI) application

## Testing
This project uses **JUnit 5** for unit testing. The tests cover:
- **JSON Deserialization**:  
  The `JsonReaderTest` ensures that JSON files are properly parsed and invalid or missing files are handled correctly.

- **Payment Logic Validation**:  
  The `PaymentOptimiserTest` verifies that:
    - No payment method is used beyond its allowed limit.
    - The system correctly throws exceptions for missing inputs.
    - The special points-based discount is applied only when the threshold conditions are met.

## Build & Run
```bash
mvn clean package
java -jar app.jar <orders.json> <paymentmethods.json>
```

## Example inputs and output

Example orders.json

```json
[
  {
    "id": "ORDER1",
    "value": "100.00",
    "promotions": [
      "mZysk"
    ]
  },
  {
    "id": "ORDER2",
    "value": "200.00",
    "promotions": [
      "BosBankrut"
    ]
  },
  {
    "id": "ORDER3",
    "value": "150.00",
    "promotions": [
      "mZysk",
      "BosBankrut"
    ]
  },
  {
    "id": "ORDER4",
    "value": "50.00"
  }
]
```

Example paymentmethods.json

```json
[
  {
    "id": "PUNKTY",
    "discount": "15",
    "limit": "100.00"
  },
  {
    "id": "mZysk",
    "discount": "10",
    "limit": "180.00"
  },
  {
    "id": "BosBankrut",
    "discount": "5",
    "limit": "200.00"
  }
]
```

Example output

```bash
mZysk 139.50
BosBankrut 190.00
PUNKTY 100.00
```
