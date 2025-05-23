﻿# Payment Method Optimizer
## Task Description

The goal of this task is to develop a 
Java-based application that assigns
optimal payment methods for a list of customer orders.
The aim is to fully pay for all orders while maximizing the total discount,
following a set of business rules.


Each order can be paid using:
- a single traditional method (e.g., credit/debit card),
- loyalty points entirely,
- or a combination: partially with loyalty points and the rest using one traditional method.

### Promotion Rules
1. Orders may have associated promotion IDs (corresponding to specific bank cards).
2. A discount is applied if an order is fully paid using a qualifying promotion method (e.g., a bank card).
3. If at least 10% of the order is paid using loyalty points, a flat 10% discount is applied.
4. If the entire order is paid with loyalty points, a specific discount from the "PUNKTY" method is applied instead.

### Input
The application takes two JSON files:
- `orders.json` — a list of up to 10,000 orders, each with an ID, value, and optional list of eligible promotions.
- `paymentmethods.json` — a list of up to 1,000 available payment methods with associated discount percentages and usage limits.

### Output
The application outputs, to standard output, the total amount paid using each method, summed across all orders.

### Solution approach
The core of the solution is a greedy algorithm that tries to assign the most beneficial discounts first while respecting all constraints.

1. The application filters out any input that doesn't contain at least one traditional payment method and at least one loyalty point (`PUNKTY`).
2. It calculates all possible discount options for every order, including:
    - full card payment (if eligible),
    - full loyalty point payment,
    - partial loyalty point payment (≥10%).
3. It sorts the discount options by the amount of promo method limit used (descending).
4. It greedily assigns promotions, updating the remaining method limits accordingly.
5. Remaining orders are covered using loyalty points partially or entirely, or fall back to a default payment method.

## Technologies Used

- **Java 17**
- **Gradle** (build tool)
- **JUnit** (unit testing)
- **Spring Boot** (for component management)
- **Jackson** (for JSON parsing)
  
### Unit Tests
Comprehensive unit tests have been implemented to ensure the application's reliability and correctness. The tests cover core functionality, edge cases, and various payment optimization scenarios.


## Build Instructions

To build the project and create a fat JAR:
```bash
./gradlew build
```
## Run Instructions
To run the application:

```bash
java -jar build\libs\PaymentOptimizer-1.0.jar /absolute/path/to/orders.json /absolute/path/to/paymentmethods.json
```

