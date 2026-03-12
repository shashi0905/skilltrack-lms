# Core Coding Standards

## Purpose
This document establishes universal coding principles that apply to **all** development work, regardless of technology stack. These standards ensure code quality, maintainability, and team collaboration.

**Target Audience**: Developers, code reviewers, and AI coding assistants

---

## Table of Contents
1. [Guiding Philosophy](#guiding-philosophy)
2. [SOLID Principles](#solid-principles)
3. [Method & Class Design](#method--class-design)
4. [Code Smells (Anti-Patterns)](#code-smells-anti-patterns)
5. [Naming Conventions](#naming-conventions)
6. [Comments & Documentation](#comments--documentation)
7. [Error Handling](#error-handling)
8. [Logging Standards](#logging-standards)
9. [Security Guidelines](#security-guidelines)
10. [Testing Standards](#testing-standards)
11. [Performance Guidelines](#performance-guidelines)
12. [Internationalization (i18n)](#internationalization-i18n)
13. [Dependency Management](#dependency-management)
14. [Architectural Principles](#architectural-principles)
15. [Code Review Checklist](#code-review-checklist)

---

## Guiding Philosophy

### Core Tenets
1. **Code is for Humans First** - Write code that other developers (including your future self) can understand in 6 months
2. **Explicit Over Clever** - Clarity beats brevity. If it requires explanation, rewrite it
3. **Small and Focused** - Methods do one thing. Classes have one responsibility
4. **DRY (Don't Repeat Yourself)** - Duplication is a maintenance burden. Extract and reuse
5. **KISS (Keep It Simple, Stupid)** - The simplest solution is usually the best
6. **YAGNI (You Aren't Gonna Need It)** - Don't build for hypothetical future requirements
7. **Boy Scout Rule** - Leave code better than you found it
8. **Consistency is Key** - Follow established patterns in the codebase

### Design Principles

#### Tell, Don't Ask
Promotes encapsulation by telling objects what to do rather than querying their state.

```javascript
// ❌ BAD - Asking for state then making decisions
if (user.getBalance() > 100) {
    user.setBalance(user.getBalance() - 100);
}

// ✅ GOOD - Telling the object what to do
user.deductBalance(100);
```

```java
// ❌ BAD - Feature Envy (asking another object for its data)
public double calculateTotal(Order order) {
    double total = 0;
    for (Item item : order.getItems()) {
        total += item.getPrice() * item.getQuantity();
    }
    return total;
}

// ✅ GOOD - Tell the object to do its job
public double calculateTotal(Order order) {
    return order.calculateTotal();
}
```

#### Encapsulate Boundary Conditions
Boundary logic is error-prone. Centralize it.

```javascript
// ❌ BAD - Scattered boundary checks
if (page > 0 && page <= totalPages) { /* ... */ }
if (index >= 0 && index < items.length) { /* ... */ }

// ✅ GOOD - Encapsulated
function isValidPage(page, totalPages) {
    return page > 0 && page <= totalPages;
}

function isValidIndex(index, collection) {
    return index >= 0 && index < collection.length;
}
```

#### Avoid Logical Dependencies
Methods should not rely on invisible state from their containing class.

```java
// ❌ BAD - Method depends on state set by another method
public class OrderProcessor {
    private Order currentOrder;
    
    public void loadOrder(String orderId) {
        this.currentOrder = orderRepository.findById(orderId);
    }
    
    public void processPayment(double amount) {
        // Fails if loadOrder wasn't called first!
        currentOrder.charge(amount);
    }
}

// ✅ GOOD - Explicit dependencies
public class OrderProcessor {
    public void processPayment(Order order, double amount) {
        order.charge(amount);
    }
}
```

#### Other Principles
- **Avoid Negative Conditionals** - Use `if (isValid)` not `if (!isInvalid)`
- **Keep Configurable Data at High Levels** - Don't hardcode URLs, limits, etc. in business logic
- **Separate Multithreading Code** - Document thread-safety assumptions clearly
- **Prevent Over-Configurability** - Too many config options = maintenance nightmare
- **Use Dependency Injection** - Improves testability and flexibility
- **Externalize Constants** - Messages, regex patterns, magic numbers belong in config files
- **Standardize Special Characters** - Define rules for handling special characters globally

---

## SOLID Principles

### Single Responsibility Principle (SRP)
**Rule**: A class should have only one reason to change.

```javascript
// ❌ BAD - Multiple responsibilities
class UserManager {
    saveUser(user) { /* DB logic */ }
    sendWelcomeEmail(user) { /* Email logic */ }
    generateReport(user) { /* Reporting logic */ }
}

// ✅ GOOD - Separated concerns
class UserRepository {
    saveUser(user) { /* DB logic */ }
}

class EmailService {
    sendWelcomeEmail(user) { /* Email logic */ }
}

class ReportGenerator {
    generateUserReport(user) { /* Reporting logic */ }
}
```

### Open/Closed Principle (OCP)
**Rule**: Open for extension, closed for modification.

```java
// ❌ BAD - Must modify class to add new payment type
public class PaymentProcessor {
    public void processPayment(String type, double amount) {
        if (type.equals("CREDIT_CARD")) {
            // process credit card
        } else if (type.equals("PAYPAL")) {
            // process PayPal
        }
        // Adding new type requires modifying this method
    }
}

// ✅ GOOD - Can extend without modifying
public interface PaymentMethod {
    void process(double amount);
}

public class CreditCardPayment implements PaymentMethod {
    public void process(double amount) { /* ... */ }
}

public class PayPalPayment implements PaymentMethod {
    public void process(double amount) { /* ... */ }
}

public class PaymentProcessor {
    public void processPayment(PaymentMethod method, double amount) {
        method.process(amount); // No modification needed for new types
    }
}
```

### Liskov Substitution Principle (LSP)
**Rule**: Subtypes must be substitutable for their base types without breaking functionality.

```typescript
// ❌ BAD - Rectangle-Square problem
class Rectangle {
    setWidth(width: number) { this.width = width; }
    setHeight(height: number) { this.height = height; }
}

class Square extends Rectangle {
    setWidth(width: number) { 
        this.width = width;
        this.height = width; // Violates LSP
    }
}

// ✅ GOOD - Use composition or separate hierarchies
interface Shape {
    area(): number;
}

class Rectangle implements Shape {
    constructor(private width: number, private height: number) {}
    area() { return this.width * this.height; }
}

class Square implements Shape {
    constructor(private side: number) {}
    area() { return this.side * this.side; }
}
```

### Interface Segregation Principle (ISP)
**Rule**: Clients should not be forced to depend on interfaces they don't use.

```typescript
// ❌ BAD - Fat interface
interface Worker {
    work(): void;
    eat(): void;
    sleep(): void;
}

class Robot implements Worker {
    work() { /* ... */ }
    eat() { /* Robots don't eat! */ }
    sleep() { /* Robots don't sleep! */ }
}

// ✅ GOOD - Segregated interfaces
interface Workable {
    work(): void;
}

interface Eatable {
    eat(): void;
}

interface Sleepable {
    sleep(): void;
}

class Human implements Workable, Eatable, Sleepable {
    work() { /* ... */ }
    eat() { /* ... */ }
    sleep() { /* ... */ }
}

class Robot implements Workable {
    work() { /* ... */ }
}
```

### Dependency Inversion Principle (DIP)
**Rule**: Depend on abstractions, not concretions.

```java
// ❌ BAD - High-level module depends on low-level module
public class OrderService {
    private MySQLDatabase database = new MySQLDatabase(); // Concrete dependency
    
    public void saveOrder(Order order) {
        database.save(order);
    }
}

// ✅ GOOD - Both depend on abstraction
public interface Database {
    void save(Object entity);
}

public class MySQLDatabase implements Database {
    public void save(Object entity) { /* ... */ }
}

public class OrderService {
    private final Database database;
    
    public OrderService(Database database) { // Inject abstraction
        this.database = database;
    }
    
    public void saveOrder(Order order) {
        database.save(order);
    }
}
```

---

## Method & Class Design

### Method Size
**Maximum Lines**: 20-30 (excluding whitespace/braces)  
**Ideal**: 5-15 lines

**Rule**: If a method needs a comment to explain what it does, it should be extracted into smaller methods with descriptive names.

```javascript
// ❌ BAD - Long method doing too much
function processOrder(order) {
    // Validate order
    if (!order.items || order.items.length === 0) {
        throw new Error('Order must have items');
    }
    if (!order.customer) {
        throw new Error('Order must have customer');
    }
    
    // Calculate totals
    let subtotal = 0;
    for (let item of order.items) {
        subtotal += item.price * item.quantity;
    }
    let tax = subtotal * 0.1;
    let total = subtotal + tax;
    
    // Apply discounts
    if (order.customer.isPremium) {
        total = total * 0.9;
    }
    
    // Save to database
    database.save(order);
    
    // Send confirmation
    emailService.send(order.customer.email, `Order confirmed: $${total}`);
}

// ✅ GOOD - Small, focused methods
function processOrder(order) {
    validateOrder(order);
    const total = calculateTotal(order);
    const finalTotal = applyDiscounts(total, order.customer);
    saveOrder(order);
    sendConfirmation(order.customer, finalTotal);
}

function validateOrder(order) {
    if (!order.items?.length) throw new Error('Order must have items');
    if (!order.customer) throw new Error('Order must have customer');
}

function calculateTotal(order) {
    const subtotal = order.items.reduce((sum, item) => 
        sum + item.price * item.quantity, 0);
    const tax = subtotal * TAX_RATE;
    return subtotal + tax;
}

function applyDiscounts(total, customer) {
    return customer.isPremium ? total * PREMIUM_DISCOUNT : total;
}
```

### Class Size
**Maximum Lines**: 300 (strong guideline)  
**Maximum Methods**: 10-15

**Signs a class is too large**:
- Scrolling required to see the whole class
- Multiple unrelated methods
- Name is vague (e.g., "Manager", "Util", "Helper")

```java
// ❌ BAD - God class doing everything
public class OrderManager {
    public void createOrder() { }
    public void validateOrder() { }
    public void calculateTotal() { }
    public void applyDiscounts() { }
    public void processPayment() { }
    public void sendEmail() { }
    public void generateInvoice() { }
    public void updateInventory() { }
    public void trackShipment() { }
    // ... 20 more methods
}

// ✅ GOOD - Separated responsibilities
public class OrderService {
    public Order createOrder(OrderRequest request) { }
    public void validateOrder(Order order) { }
}

public class PricingService {
    public Money calculateTotal(Order order) { }
    public Money applyDiscounts(Money total, Customer customer) { }
}

public class PaymentService {
    public Payment processPayment(Order order) { }
}

public class NotificationService {
    public void sendOrderConfirmation(Order order) { }
}
```

### Parameter Count
**Maximum**: 3 parameters (strict guideline)  
**Ideal**: 0-2 parameters

```typescript
// ❌ BAD - Too many parameters
function createUser(
    firstName: string, 
    lastName: string, 
    email: string, 
    phone: string, 
    address: string, 
    city: string, 
    zipCode: string
) { }

// ✅ GOOD - Use object parameter
interface UserParams {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    address: Address;
}

interface Address {
    street: string;
    city: string;
    zipCode: string;
}

function createUser(params: UserParams) { }
```

---

## Code Smells (Anti-Patterns)

### Critical Code Smells

#### 1. Long Method
**Detection**: Method > 20-30 lines  
**Fix**: Extract smaller methods

#### 2. Large Class
**Detection**: Class > 300 lines or > 15 methods  
**Fix**: Split into multiple classes with single responsibilities

#### 3. Long Parameter List
**Detection**: > 3 parameters  
**Fix**: Introduce parameter object

```python
# ❌ BAD
def send_email(to, from_addr, subject, body, cc, bcc, priority, attachments):
    pass

# ✅ GOOD
class EmailMessage:
    def __init__(self, to, from_addr, subject, body):
        self.to = to
        self.from_addr = from_addr
        self.subject = subject
        self.body = body
        self.cc = []
        self.bcc = []
        self.priority = 'normal'
        self.attachments = []

def send_email(message: EmailMessage):
    pass
```

#### 4. Primitive Obsession
**Detection**: Using primitives instead of small objects  
**Fix**: Create value objects

```java
// ❌ BAD - Primitives everywhere
public class Customer {
    private String email; // What if invalid?
    private String phoneNumber; // What format?
    private double balance; // Can be negative?
}

// ✅ GOOD - Value objects with validation
public class Email {
    private final String value;
    
    public Email(String value) {
        if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.value = value;
    }
}

public class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
        this.currency = currency;
    }
}
```

#### 5. Data Clumps
**Detection**: Same group of parameters appear together repeatedly  
**Fix**: Extract into object

```javascript
// ❌ BAD - Data clump
function createAddress(street, city, state, zip) { }
function validateAddress(street, city, state, zip) { }
function formatAddress(street, city, state, zip) { }

// ✅ GOOD - Address object
class Address {
    constructor(street, city, state, zip) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }
    
    validate() { }
    format() { }
}
```

#### 6. Switch Statements
**Detection**: Large switch/if-else chains based on type  
**Fix**: Use polymorphism

```typescript
// ❌ BAD - Type-based switch
function calculateShipping(type: string, weight: number): number {
    switch(type) {
        case 'STANDARD': return weight * 1.5;
        case 'EXPRESS': return weight * 3.0;
        case 'OVERNIGHT': return weight * 5.0;
        default: throw new Error('Unknown type');
    }
}

// ✅ GOOD - Polymorphism
interface ShippingMethod {
    calculateCost(weight: number): number;
}

class StandardShipping implements ShippingMethod {
    calculateCost(weight: number) { return weight * 1.5; }
}

class ExpressShipping implements ShippingMethod {
    calculateCost(weight: number) { return weight * 3.0; }
}

class OvernightShipping implements ShippingMethod {
    calculateCost(weight: number) { return weight * 5.0; }
}
```

#### 7. Temporary Field
**Detection**: Instance variable only used in certain scenarios  
**Fix**: Extract to separate object or use method parameters

```java
// ❌ BAD - Temporary field
public class Order {
    private String tempCalculationResult; // Only used during calculateTotal()
    
    public Money calculateTotal() {
        tempCalculationResult = "calculating...";
        // ... complex logic
        tempCalculationResult = null;
        return total;
    }
}

// ✅ GOOD - No temporary field
public class Order {
    public Money calculateTotal() {
        TotalCalculator calculator = new TotalCalculator(this);
        return calculator.calculate();
    }
}
```

#### 8. Message Chains
**Detection**: `a.getB().getC().getD()` or `props.user.profile.settings.theme`  
**Fix**: Apply Law of Demeter - only talk to immediate friends

```javascript
// ❌ BAD - Message chain
const theme = user.getProfile().getSettings().getTheme();

// ✅ GOOD - Delegate through domain object
class User {
    getTheme() {
        return this.profile.getTheme(); // User knows how to get theme
    }
}

const theme = user.getTheme();
```

#### 9. Middle Man
**Detection**: Class that just delegates all work to another class  
**Fix**: Remove the middle man

```java
// ❌ BAD - Unnecessary delegation
public class OrderFacade {
    private OrderService orderService;
    
    public void createOrder(Order order) {
        orderService.createOrder(order);
    }
    
    public Order getOrder(String id) {
        return orderService.getOrder(id);
    }
    // Just delegating everything...
}

// ✅ GOOD - Use OrderService directly
// Remove OrderFacade if it adds no value
```

#### 10. Magic Numbers/Literals
**Detection**: Unexplained numeric or string literals in code  
**Fix**: Replace with named constants

```python
# ❌ BAD
if user.age > 18:
    discount = price * 0.15
    
if status == 'A':
    process()

# ✅ GOOD
ADULT_AGE = 18
STUDENT_DISCOUNT_RATE = 0.15
STATUS_ACTIVE = 'A'

if user.age > ADULT_AGE:
    discount = price * STUDENT_DISCOUNT_RATE
    
if status == STATUS_ACTIVE:
    process()
```

#### 11. Duplicated Code
**Detection**: Same code in multiple places  
**Fix**: Extract to shared method/function

#### 12. Dead Code
**Detection**: Unused methods, variables, parameters  
**Fix**: Delete it (version control remembers)

#### 13. Speculative Generality
**Detection**: Code designed for future scenarios that don't exist  
**Fix**: Delete it until you actually need it (YAGNI)

#### 14. Feature Envy
**Detection**: Method uses data from another class more than its own  
**Fix**: Move method to the envied class

```javascript
// ❌ BAD - Feature Envy
class Bill {
    calculateLateFee(customer) {
        return customer.paymentHistory.averageLate() * 
               customer.creditRating.riskMultiplier() * 
               customer.subscription.baseFee();
    }
}

// ✅ GOOD - Move to Customer
class Customer {
    calculateLateFee() {
        return this.paymentHistory.averageLate() * 
               this.creditRating.riskMultiplier() * 
               this.subscription.baseFee();
    }
}
```

#### 15. Inappropriate Intimacy
**Detection**: Classes that know too much about each other's internals  
**Fix**: Reduce coupling, use proper encapsulation

#### 16. Data Class
**Detection**: Class with only getters/setters and no behavior  
**Fix**: Move related behavior into the data class

```java
// ❌ BAD - Anemic domain model
public class Product {
    private String name;
    private double price;
    
    // Only getters and setters
}

public class ProductService {
    public double calculateDiscount(Product product, double rate) {
        return product.getPrice() * rate;
    }
}

// ✅ GOOD - Rich domain model
public class Product {
    private String name;
    private Money price;
    
    public Money calculateDiscount(DiscountRate rate) {
        return price.multiply(rate);
    }
}
```

#### 17. Refused Bequest
**Detection**: Subclass doesn't use inherited methods  
**Fix**: Use composition instead of inheritance

#### 18. Divergent Change
**Detection**: One class changes for multiple different reasons  
**Fix**: Split into multiple classes (SRP)

#### 19. Shotgun Surgery
**Detection**: Single change requires modifications in many classes  
**Fix**: Move related code together

#### 20. Parallel Inheritance Hierarchies
**Detection**: Adding subclass in one hierarchy requires adding in another  
**Fix**: Consider merging hierarchies or using composition

#### 21. Lazy Class
**Detection**: Class that doesn't do enough to justify its existence  
**Fix**: Merge into another class or delete

---

## Naming Conventions

### General Rules
1. **Self-Explanatory Names** - Name should reveal intent without comments
2. **Use Full Words** - Avoid abbreviations (except universally known like `id`, `url`)
3. **Be Consistent** - Follow existing conventions in codebase
4. **Avoid Misleading Names** - Name should match what it does
5. **Searchable Names** - Use meaningful names over single letters (except loop counters)

### Variables

```javascript
// ❌ BAD
const d = 86400000; // What is this?
const tmp = getData(); // Too vague
const list = getUsers(); // What kind of list?

// ✅ GOOD
const MILLISECONDS_PER_DAY = 86400000;
const activeUsers = getActiveUsers();
const pendingOrders = getOrdersByStatus('PENDING');
```

### Functions/Methods

```python
# ❌ BAD
def proc(): pass  # What does it process?
def data(): pass  # Get or set data?
def do_stuff(): pass  # Too vague

# ✅ GOOD
def process_payment(order): pass  # Clear action
def get_user_by_id(user_id): pass  # Clear purpose
def calculate_monthly_interest(balance): pass  # Descriptive
```

**Naming Patterns**:
- Use verbs for methods: `calculateTotal()`, `sendEmail()`, `validateInput()`
- Boolean methods: `isValid()`, `hasPermission()`, `canEdit()`
- Getters: `getUser()`, `getBalance()`
- Setters: `setEmail()`, `updateStatus()`

### Classes

```java
// ❌ BAD
class Manager { }  // Too generic
class DataHandler { }  // Vague
class Util { }  // What utilities?

// ✅ GOOD
class OrderProcessor { }  // Specific purpose
class PaymentGateway { }  // Clear responsibility
class EmailValidator { }  // Single responsibility
```

### Constants

```typescript
// ❌ BAD
const max = 100;
const url = 'https://api.example.com';

// ✅ GOOD
const MAX_RETRY_ATTEMPTS = 100;
const API_BASE_URL = 'https://api.example.com';
const DEFAULT_PAGE_SIZE = 20;
```

---

## Comments & Documentation

### Philosophy
**Best Comment**: No comment needed because code is self-explanatory.  
**Second Best**: Comment explaining *why*, not *what*.

### When to Use Comments

#### ✅ Good Uses

**1. Explain WHY, not WHAT**
```javascript
// ✅ GOOD - Explains reasoning
// Use batch size of 100 to avoid MongoDB cursor timeout on large collections
const BATCH_SIZE = 100;

// ❌ BAD - States the obvious
// Set batch size to 100
const BATCH_SIZE = 100;
```

**2. Document Non-Obvious Business Rules**
```java
// Payment must be processed within 48 hours of order creation
// per compliance requirements (GDPR Article 7)
if (hoursSinceOrder > 48) {
    throw new PaymentExpiredException();
}
```

**3. Warn of Consequences**
```python
# WARNING: Changing this value requires database migration
# See docs/migrations/add-user-roles.md
AVAILABLE_ROLES = ['admin', 'user', 'guest']
```

**4. Document Edge Cases**
```typescript
// Edge case: Empty cart should still create order record
// for analytics tracking per product requirements
if (cart.items.length === 0) {
    return createEmptyOrderRecord(userId);
}
```

**5. TODO/FIXME Tags**
```javascript
// TODO(johndoe): Refactor to use async/await after Node 18 upgrade
// FIXME: Race condition when two users update same record simultaneously
// HACK: Temporary workaround for upstream API bug - remove after fix deployed
```

### When NOT to Use Comments

#### ❌ Bad Uses

**1. Redundant Comments**
```java
// ❌ BAD - Comment adds no value
// Get the user
User user = userRepository.getUser(id);

// ❌ BAD - Obvious
// Increment counter
counter++;
```

**2. Commented-Out Code**
```javascript
// ❌ BAD - Delete instead of commenting out
// function oldImplementation() {
//     // 50 lines of old code
// }

// ✅ GOOD - Just delete it (Git remembers)
```

**3. Closing Brace Comments**
```java
// ❌ BAD - Indicates method is too long
public void processOrder() {
    if (order.isValid()) {
        for (Item item : order.getItems()) {
            // ... 100 lines
        } // end for
    } // end if
} // end processOrder

// ✅ GOOD - Extract methods so braces are obvious
```

**4. Noise Comments**
```javascript
// ❌ BAD - Adds no information
/** Constructor */
constructor() { }

/** The user's name */
private String name;
```

**5. Instead of Comments, Refactor**
```python
# ❌ BAD - Comment explaining complex logic
# Check if user has premium subscription and order total > 100
if user['subscription'] == 'premium' and order['total'] > 100:
    apply_discount()

# ✅ GOOD - Extract to self-documenting method
def qualifies_for_premium_discount(user, order):
    return user.has_premium_subscription() and order.total > 100

if qualifies_for_premium_discount(user, order):
    apply_discount()
```

### Documentation Standards

**Public APIs**: Always document
```java
/**
 * Calculates the settlement amount between two group members.
 * 
 * @param fromMember The member who owes money
 * @param toMember The member who is owed money
 * @param groupId The group in which to calculate settlement
 * @return The settlement amount, or zero if members are balanced
 * @throws MemberNotFoundException if either member not found in group
 */
public Money calculateSettlement(Member fromMember, Member toMember, String groupId) {
    // Implementation
}
```

**Complex Algorithms**: Document approach
```javascript
/**
 * Calculates optimal settlements using graph reduction algorithm.
 * Instead of N*(N-1) transactions, reduces to minimum set of transfers.
 * 
 * Algorithm:
 * 1. Calculate net balance for each member
 * 2. Separate debtors from creditors
 * 3. Match largest debtor with largest creditor
 * 4. Repeat until all balanced
 * 
 * Time Complexity: O(N log N)
 * Space Complexity: O(N)
 */
function optimizeSettlements(balances) {
    // Implementation
}
```

---

## Error Handling

### Exception Hierarchy
Use domain-specific custom exceptions organized in a hierarchy.

```java
// ✅ GOOD - Exception hierarchy
public class BusinessException extends RuntimeException { }

public class ValidationException extends BusinessException { }
public class ResourceNotFoundException extends BusinessException { }
public class InsufficientPermissionException extends BusinessException { }

public class GroupNotFoundException extends ResourceNotFoundException {
    public GroupNotFoundException(String groupId) {
        super("Group not found: " + groupId);
    }
}
```

### Error Messages

**Externalize User-Facing Messages**
```javascript
// ❌ BAD - Hardcoded messages
throw new Error('User not found');

// ✅ GOOD - Externalized with i18n key
throw new NotFoundException('error.user.not_found', { userId: id });
```

**Error Code Mapping**
```java
public enum ErrorCode {
    USER_NOT_FOUND("ERR001", "error.user.not_found"),
    INVALID_EMAIL("ERR002", "error.validation.email"),
    INSUFFICIENT_BALANCE("ERR003", "error.payment.insufficient_balance");
    
    private final String code;
    private final String messageKey;
    
    // Constructor and getters
}
```

### Exception Handling Rules

**1. Don't Swallow Exceptions**
```python
# ❌ BAD - Silent failure
try:
    process_payment(order)
except Exception:
    pass  # Exception lost!

# ✅ GOOD - Log and handle appropriately
try:
    process_payment(order)
except PaymentException as e:
    logger.error(f"Payment failed for order {order.id}", exc_info=e)
    raise OrderProcessingException("Payment failed") from e
```

**2. Catch Specific Exceptions**
```java
// ❌ BAD - Too broad
try {
    processOrder(order);
} catch (Exception e) {
    // Catches everything, including unexpected errors
}

// ✅ GOOD - Specific exceptions
try {
    processOrder(order);
} catch (ValidationException e) {
    // Handle validation issues
} catch (PaymentException e) {
    // Handle payment issues
}
// Let unexpected exceptions propagate
```

**3. Provide Context in Exceptions**
```javascript
// ❌ BAD - No context
throw new Error('Invalid input');

// ✅ GOOD - Rich context
throw new ValidationError('Invalid email format', {
    field: 'email',
    value: email,
    expectedFormat: 'user@domain.com'
});
```

**4. Clean Up Resources**
```java
// ✅ GOOD - Try-with-resources (Java)
try (FileWriter writer = new FileWriter(file)) {
    writer.write(data);
} // Automatically closed even if exception occurs

// ✅ GOOD - Finally block
Connection conn = null;
try {
    conn = getConnection();
    // Use connection
} finally {
    if (conn != null) conn.close();
}
```

---

## Logging Standards

### Log Levels

| Level | When to Use | Examples |
|-------|-------------|----------|
| ERROR | Something failed that requires attention | Payment processing failed, database connection lost |
| WARN | Something unexpected but recoverable | Retry attempt, deprecated API used, high memory usage |
| INFO | Business events and application lifecycle | User login, order created, service started |
| DEBUG | Detailed information for debugging | SQL queries, method entry/exit, variable values |
| TRACE | Very detailed diagnostic information | Loop iterations, detailed state changes |

### Logging Best Practices

**1. Log Business Events, Not Technical Noise**
```java
// ❌ BAD - Too much noise
logger.debug("Entering getUserById method");
logger.debug("Parameter id = " + id);
logger.debug("Calling repository");
User user = repository.findById(id);
logger.debug("Exiting getUserById method");

// ✅ GOOD - Meaningful business event
logger.info("User {} retrieved successfully", user.getId());
```

**2. Use Structured Logging**
```javascript
// ❌ BAD - Unstructured
console.log('User login failed for user john@example.com from IP 192.168.1.1');

// ✅ GOOD - Structured (JSON)
logger.warn('User login failed', {
    userId: user.id,
    email: 'john@example.com',
    ipAddress: '192.168.1.1',
    reason: 'invalid_password',
    timestamp: new Date().toISOString()
});
```

**3. Use Parameterized Logging**
```python
# ❌ BAD - String concatenation
logger.info("Processing order " + order_id + " for user " + user_id)

# ✅ GOOD - Parameterized (more efficient)
logger.info("Processing order %s for user %s", order_id, user_id)
```

**4. Include Context**
```java
// ❌ BAD - No context
logger.error("Payment failed");

// ✅ GOOD - Rich context
logger.error("Payment processing failed: orderId={}, amount={}, gateway={}, reason={}", 
    order.getId(), order.getTotal(), paymentGateway, exception.getMessage(), exception);
```

**5. Never Log Sensitive Information**
```typescript
// ❌ BAD - Leaking sensitive data
logger.debug('User credentials', { password: user.password, ssn: user.ssn });

// ✅ GOOD - Mask or omit sensitive data
logger.debug('User authentication attempt', { 
    userId: user.id, 
    email: user.email,
    passwordProvided: !!user.password // Boolean, not actual password
});
```

**6. Centralize Log Messages**
```java
// ✅ GOOD - Centralized constants
public class LogMessages {
    public static final String ORDER_CREATED = "Order created: orderId={}, userId={}, total={}";
    public static final String PAYMENT_FAILED = "Payment failed: orderId={}, reason={}";
}

// Usage
logger.info(LogMessages.ORDER_CREATED, orderId, userId, total);
```

---

## Security Guidelines

### Secrets Management

**Never Hardcode Secrets**
```yaml
# ❌ BAD - Hardcoded secrets
database:
  url: mongodb://admin:password123@localhost:27017
  
api_keys:
  stripe: sk_live_abc123xyz

# ✅ GOOD - Environment variables
database:
  url: ${MONGODB_URI}
  
api_keys:
  stripe: ${STRIPE_API_KEY}
```

**Use Secret Management Tools**
- AWS Secrets Manager
- HashiCorp Vault
- Azure Key Vault
- Kubernetes Secrets

### Input Validation

**Always Validate User Input**
```javascript
// ❌ BAD - No validation
function createUser(email) {
    database.save({ email: email });
}

// ✅ GOOD - Validation
function createUser(email) {
    if (!isValidEmail(email)) {
        throw new ValidationError('Invalid email format');
    }
    if (email.length > 255) {
        throw new ValidationError('Email too long');
    }
    
    const sanitizedEmail = sanitize(email);
    database.save({ email: sanitizedEmail });
}
```

### Data Protection

**PII (Personally Identifiable Information) Protection**
```java
// ✅ GOOD - Mask PII in logs
public class User {
    private String email;
    private String ssn;
    
    @Override
    public String toString() {
        return String.format("User{email=%s, ssn=%s}", 
            maskEmail(email), 
            "***-**-****");
    }
    
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***" + email;
    }
}
```

### Dependency Security

**Regular Security Audits**
```bash
# JavaScript/Node.js
npm audit
npm audit fix

# Java/Maven
mvn dependency-check:check

# Python
pip-audit
safety check
```

**Version Upgrade Policy**
- **Immediate**: Critical security vulnerabilities (CVE with CVSS >= 9.0)
- **Within 1 week**: High severity vulnerabilities (CVSS 7.0-8.9)
- **Within 1 month**: Medium severity vulnerabilities (CVSS 4.0-6.9)
- **Only patch/minor versions**: Avoid major version upgrades without explicit approval and thorough testing

### SQL Injection Prevention

```python
# ❌ BAD - SQL injection risk
query = f"SELECT * FROM users WHERE email = '{user_input}'"

# ✅ GOOD - Parameterized query
query = "SELECT * FROM users WHERE email = ?"
cursor.execute(query, (user_input,))
```

### XSS Prevention

```javascript
// ❌ BAD - XSS vulnerability
element.innerHTML = userInput;

// ✅ GOOD - Escape user input
element.textContent = userInput; // Automatically escaped
// OR
element.innerHTML = DOMPurify.sanitize(userInput);
```

---

## Testing Standards

### Test Structure: Arrange-Act-Assert

```java
@Test
public void shouldCalculateCorrectTotal() {
    // Arrange - Set up test data
    Order order = new Order();
    order.addItem(new Item("Product A", 10.00, 2));
    order.addItem(new Item("Product B", 15.00, 1));
    
    // Act - Execute the behavior
    Money total = order.calculateTotal();
    
    // Assert - Verify the result
    assertEquals(new Money(35.00), total);
}
```

### Test Naming

```javascript
// ✅ GOOD - Descriptive test names
describe('OrderService', () => {
    describe('calculateTotal', () => {
        it('should return zero for empty order', () => { });
        it('should sum item prices correctly', () => { });
        it('should apply tax rate when specified', () => { });
        it('should throw ValidationError for negative prices', () => { });
    });
});
```

### Test Coverage Rules

**What to Test**:
- ✅ All public methods
- ✅ Edge cases (null, empty, boundary values)
- ✅ Error conditions
- ✅ Business logic

**What NOT to Test**:
- ❌ Private methods (test through public interface)
- ❌ Trivial getters/setters
- ❌ Framework code
- ❌ External libraries

### Test Quality Guidelines

**1. Tests Should Be Fast**
```java
// ❌ BAD - Slow test
@Test
public void testSlowOperation() throws InterruptedException {
    Thread.sleep(5000); // Don't sleep in tests!
    // test logic
}

// ✅ GOOD - Fast test with mocks
@Test
public void testOperation() {
    when(externalService.call()).thenReturn(mockResponse);
    // test logic executes instantly
}
```

**2. Tests Should Be Independent**
```python
# ❌ BAD - Tests depend on execution order
class TestUser:
    user = None
    
    def test_create_user(self):
        self.user = create_user()  # Later tests depend on this
        
    def test_update_user(self):
        self.user.update()  # Fails if test_create_user didn't run

# ✅ GOOD - Each test is independent
class TestUser:
    def test_create_user(self):
        user = create_user()
        assert user is not None
        
    def test_update_user(self):
        user = create_user()  # Fresh user for this test
        user.update()
        assert user.updated
```

**3. Maximum 3 Assertions Per Test**
```javascript
// ❌ BAD - Too many assertions
test('user creation', () => {
    const user = createUser(data);
    expect(user.id).toBeTruthy();
    expect(user.name).toBe(data.name);
    expect(user.email).toBe(data.email);
    expect(user.createdAt).toBeInstanceOf(Date);
    expect(user.status).toBe('active');
    expect(user.role).toBe('user');
});

// ✅ GOOD - Split into focused tests
test('should generate user ID', () => {
    const user = createUser(data);
    expect(user.id).toBeTruthy();
});

test('should set user properties from data', () => {
    const user = createUser(data);
    expect(user.name).toBe(data.name);
    expect(user.email).toBe(data.email);
});

test('should initialize user with default values', () => {
    const user = createUser(data);
    expect(user.status).toBe('active');
    expect(user.role).toBe('user');
});
```

**4. Avoid Test Data Duplication**
```java
// ❌ BAD - Duplicated test data
@Test
public void testScenario1() {
    User user = new User("John", "john@example.com", 30);
    // test logic
}

@Test
public void testScenario2() {
    User user = new User("John", "john@example.com", 30);
    // test logic
}

// ✅ GOOD - Extract common data
public class UserServiceTest {
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        testUser = new User("John", "john@example.com", 30);
    }
    
    @Test
    public void testScenario1() {
        // use testUser
    }
    
    @Test
    public void testScenario2() {
        // use testUser
    }
}
```

**5. One Negative Test Per API**
```typescript
describe('POST /api/orders', () => {
    it('should create order successfully', async () => {
        // Happy path
    });
    
    it('should return 400 for invalid data', async () => {
        // Negative test - covers validation errors
        const invalidData = { items: [] };
        const response = await request(app)
            .post('/api/orders')
            .send(invalidData);
        expect(response.status).toBe(400);
    });
});
```

---

## Performance Guidelines

### Avoid Premature Optimization
**Rule**: Profile first, optimize later. Don't sacrifice readability for unproven performance gains.

```javascript
// ❌ BAD - Premature optimization (harder to read)
const r = a.reduce((c,v,i)=>i%2?c:[...c,v],[]);

// ✅ GOOD - Clear first, optimize if profiling shows bottleneck
const result = array.filter((value, index) => index % 2 === 0);
```

### Algorithm Complexity

**Identify and Fix O(N²) Problems**
```python
# ❌ BAD - O(N²) with nested loops
def find_duplicates(list1, list2):
    duplicates = []
    for item1 in list1:
        for item2 in list2:
            if item1 == item2:
                duplicates.append(item1)
    return duplicates

# ✅ GOOD - O(N+M) with set
def find_duplicates(list1, list2):
    set2 = set(list2)  # O(M)
    duplicates = [item for item in list1 if item in set2]  # O(N)
    return duplicates
```

**Convert Nested Loops to Maps**
```java
// ❌ BAD - O(N*M) lookup
List<Order> findOrdersForUsers(List<User> users, List<Order> allOrders) {
    List<Order> result = new ArrayList<>();
    for (User user : users) {
        for (Order order : allOrders) {
            if (order.getUserId().equals(user.getId())) {
                result.add(order);
            }
        }
    }
    return result;
}

// ✅ GOOD - O(N+M) with map
List<Order> findOrdersForUsers(List<User> users, List<Order> allOrders) {
    Map<String, Order> ordersByUserId = allOrders.stream()
        .collect(Collectors.groupingBy(Order::getUserId));
    
    return users.stream()
        .flatMap(user -> ordersByUserId.getOrDefault(user.getId(), List.of()).stream())
        .collect(Collectors.toList());
}
```

### Batch Operations

**Batch Database Operations**
```javascript
// ❌ BAD - N database calls
for (const order of orders) {
    await database.delete('orders', order.id);
}

// ✅ GOOD - Single batch operation
const orderIds = orders.map(o => o.id);
await database.batchDelete('orders', orderIds);
```

### Caching

**Cache Expensive Operations**
```python
# ✅ GOOD - Cache expensive calculation
from functools import lru_cache

@lru_cache(maxsize=128)
def expensive_calculation(user_id):
    # Complex calculation
    return result

# Result is cached based on user_id parameter
```

---

## Internationalization (i18n)

### Message Externalization

**Backend: Use Message Keys**
```java
// ❌ BAD - Hardcoded messages
throw new ValidationException("Email is required");

// ✅ GOOD - Externalized message
throw new ValidationException("validation.email.required");
```

**Message Properties**
```properties
# messages_en.properties
validation.email.required=Email is required
validation.email.format=Invalid email format
error.user.not_found=User not found: {0}

# messages_de.properties
validation.email.required=E-Mail ist erforderlich
validation.email.format=Ungültiges E-Mail-Format
error.user.not_found=Benutzer nicht gefunden: {0}
```

### Special Character Handling

**German Umlaut Normalization**
```javascript
// ✅ GOOD - Normalize special characters
function normalizeForSystem(text) {
    return text
        .replace(/ä/g, 'ae')
        .replace(/ö/g, 'oe')
        .replace(/ü/g, 'ue')
        .replace(/Ä/g, 'Ae')
        .replace(/Ö/g, 'Oe')
        .replace(/Ü/g, 'Ue')
        .replace(/ß/g, 'ss');
}
```

### Multi-Locale Updates

**Rule**: When updating English (en) messages, update ALL supported locales.

```properties
# ❌ BAD - Only updated English
# messages_en.properties
welcome.message=Welcome to our application

# messages_de.properties
# Missing translation!

# ✅ GOOD - Updated all locales
# messages_en.properties
welcome.message=Welcome to our application

# messages_de.properties
welcome.message=Willkommen in unserer Anwendung

# messages_fr.properties
welcome.message=Bienvenue dans notre application
```

---

## Dependency Management

### Version Control

**Keep Dependencies Updated**
- Review dependencies monthly
- Apply security patches within SLA (see Security Guidelines)
- Document versions in dependency lock files

**Semantic Versioning**
- `MAJOR.MINOR.PATCH` (e.g., `2.5.3`)
- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes (backward compatible)

**Upgrade Policy**
```javascript
// ✅ GOOD - Safe upgrades
{
  "dependencies": {
    "express": "^4.18.2",  // Auto-updates patch and minor
    "mongodb": "~5.6.0"    // Auto-updates patch only
  }
}
```

### Removing Unused Dependencies

**Regular Dependency Audit**
```bash
# JavaScript
npx depcheck

# Java
mvn dependency:analyze

# Python
pip list --not-required
```

### CVE Remediation

**Document Security Fixes**
```markdown
# CHANGELOG.md

## [1.2.3] - 2024-01-15
### Security
- Updated `lodash` from 4.17.20 to 4.17.21
  - CVE-2021-23337: Command injection via template
  - Severity: High (CVSS 7.4)
  - Impact: Mitigated command injection risk in user template processing
```

---

## Architectural Principles

### Architectural Smells

**1. Rigidity**
**Symptom**: Small change causes cascade of changes  
**Fix**: Reduce coupling, use dependency injection

**2. Fragility**
**Symptom**: System breaks in many places from single change  
**Fix**: Improve modularity, increase cohesion

**3. Immobility**
**Symptom**: Can't reuse code in other projects  
**Fix**: Extract reusable components, reduce dependencies

### Layered Architecture

**Typical Layers**:
1. **Presentation Layer**: UI, API controllers
2. **Business Logic Layer**: Services, domain logic
3. **Data Access Layer**: Repositories, database queries
4. **Infrastructure Layer**: External services, utilities

**Rules**:
- Layers depend only on layers below them
- Never skip layers (e.g., Controller should not call Repository directly)
- Domain logic stays in business layer

---

## Code Review Checklist

Use this checklist before submitting code for review:

### Code Quality
- [ ] No code smells (long methods, large classes, duplicated code)
- [ ] SOLID principles applied
- [ ] Methods ≤ 20 lines
- [ ] Classes ≤ 300 lines
- [ ] Max 3 parameters per method
- [ ] Self-explanatory names (variables, methods, classes)
- [ ] No magic numbers/literals
- [ ] DRY principle followed

### Error Handling
- [ ] Custom exceptions for domain errors
- [ ] Error messages externalized (i18n keys)
- [ ] No swallowed exceptions
- [ ] Specific exception catching (not generic `Exception`)
- [ ] Proper error context provided

### Security
- [ ] No hardcoded secrets
- [ ] Input validation present
- [ ] No PII in logs
- [ ] Dependencies up to date
- [ ] No SQL injection risks
- [ ] XSS prevention in place

### Testing
- [ ] Unit tests for new public methods
- [ ] Tests follow Arrange-Act-Assert
- [ ] ≤ 3 assertions per test
- [ ] No test data duplication
- [ ] At least one negative test per API
- [ ] Tests are independent and fast

### Logging
- [ ] Business events logged
- [ ] Structured logging used
- [ ] Appropriate log levels
- [ ] No sensitive data logged
- [ ] Context included in error logs

### Documentation
- [ ] Public APIs documented
- [ ] Complex logic has comments explaining WHY
- [ ] No redundant/obvious comments
- [ ] No commented-out code
- [ ] README updated if needed

### Performance
- [ ] No obvious N+1 queries
- [ ] Batch operations where appropriate
- [ ] No premature optimizations
- [ ] Algorithm complexity considered

### I18n
- [ ] User-facing messages externalized
- [ ] All supported locales updated
- [ ] Special characters handled

---

## Summary

**Remember the Core Principles**:
1. **Readability First** - Code is read 10x more than written
2. **Small & Focused** - Methods and classes do one thing well
3. **No Code Smells** - Actively refactor anti-patterns
4. **Self-Documenting** - Names reveal intent, comments explain why
5. **Test Everything** - Tests are first-class code
6. **Secure by Default** - Never compromise on security
7. **Consistent** - Follow team conventions

**When in Doubt**:
- Would this code make sense to someone unfamiliar with the codebase?
- Could this be simpler?
- Does this follow SOLID principles?
- Is this properly tested?
- Is this secure?

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024-01-15 | Initial comprehensive standards document |

---

**Next**: See language-specific standards:
- [Java & Spring Boot Standards](java-spring-boot-enhanced.md)
- [React & JavaScript Standards](react-javascript-enhanced.md)
