# Java & Spring Boot Coding Standards

## Purpose
This document defines **Java and Spring Boot specific** coding standards for backend development. These guidelines **complement** the [Core Standards](core-standards.md) and must be followed alongside them.

**Target Audience**: Backend developers, code reviewers, AI coding assistants

**Scope**: Java 17+, Spring Boot 3.x, MongoDB with Spring Data

---

## Table of Contents
1. [Java Language Standards](#java-language-standards)
2. [Spring Boot Fundamentals](#spring-boot-fundamentals)
3. [Project Structure](#project-structure)
4. [Controller Layer](#controller-layer)
5. [Service Layer](#service-layer)
6. [Repository Layer](#repository-layer)
7. [Domain Models & Entities](#domain-models--entities)
8. [DTOs & Validation](#dtos--validation)
9. [Exception Handling](#exception-handling)
10. [MongoDB Best Practices](#mongodb-best-practices)
11. [Configuration Management](#configuration-management)
12. [Testing](#testing)
13. [Logging](#logging)
14. [Security](#security)
15. [Performance](#performance)
16. [API Documentation](#api-documentation)
17. [Common Patterns](#common-patterns)
18. [Anti-Patterns to Avoid](#anti-patterns-to-avoid)

---

## Java Language Standards

### Java Version
- **Target**: Java 21
- **Modern Features**: Leverage Records, Text Blocks, Pattern Matching, Switch Expressions
- **Deprecation**: Avoid all deprecated APIs

### Code Style

```java
// Formatting Rules
// - Line length: 120 characters maximum
// - Indentation: 4 spaces (never tabs)
// - Braces: Always use, even for single-line blocks
// - Imports: No wildcards, group by package

// ✅ GOOD - Proper formatting
public class UserService {
    
    public Optional<User> findUserById(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return userRepository.findById(userId);
    }
}

// ❌ BAD - Poor formatting
public class UserService{
public Optional<User> findUserById(String userId){if(userId==null)throw new IllegalArgumentException("userId cannot be null");return userRepository.findById(userId);}}
```

### Type System

#### Explicit Types vs var

```java
// ✅ GOOD - Explicit type when not obvious
List<MemberDTO> members = groupService.getMembers(groupId);
Map<String, List<Expense>> expensesByGroup = buildExpenseMap(expenses);

// ✅ GOOD - var when type is obvious from constructor/factory
var group = new Group();
var builder = GroupDTO.builder();
var connection = dataSource.getConnection();

// ❌ BAD - var when type is not obvious
var members = groupService.getMembers(groupId); // What type is this?
var result = process(data); // Unclear return type
```

#### Prefer Immutability

```java
// ✅ GOOD - Immutable value object using Record
public record MemberId(String value) {
    public MemberId {
        Objects.requireNonNull(value, "MemberId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("MemberId cannot be blank");
        }
    }
}

// ✅ GOOD - Traditional immutable class
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount);
        this.currency = Objects.requireNonNull(currency);
    }
    
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    // Getters only, no setters
}

// ❌ BAD - Mutable value object
public class Money {
    private BigDecimal amount; // Can be changed!
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
```

#### Collections Best Practices

```java
// ✅ GOOD - Interface types for flexibility
public class GroupService {
    public List<Expense> getExpenses(String groupId) {
        return new ArrayList<>(expenses); // Return copy for safety
    }
    
    public Map<String, Member> getMemberMap() {
        return new HashMap<>(memberMap);
    }
}

// ✅ GOOD - Immutable collections when possible
public class Group {
    private final List<Member> members;
    
    public Group(List<Member> members) {
        this.members = List.copyOf(members); // Defensive copy
    }
    
    public List<Member> getMembers() {
        return members; // Already immutable
    }
}

// ✅ GOOD - Factory methods for clarity
List<String> memberIds = List.of("id1", "id2", "id3");
Set<String> uniqueIds = Set.of("id1", "id2", "id3");
Map<String, String> config = Map.of("key1", "value1", "key2", "value2");

// ❌ BAD - Exposing mutable internal state
public class Group {
    private List<Member> members = new ArrayList<>();
    
    public List<Member> getMembers() {
        return members; // Caller can modify!
    }
}

// ❌ BAD - Using concrete implementation
public ArrayList<Expense> getExpenses() { // Should return List
    return expenses;
}
```

### Null Safety

#### Optional for Return Types

```java
// ✅ GOOD - Optional indicates possibility of absence
public Optional<Group> findGroupById(String id) {
    return groupRepository.findById(id);
}

// Usage
Optional<Group> groupOpt = findGroupById(id);
groupOpt.ifPresent(group -> processGroup(group));

Group group = findGroupById(id)
    .orElseThrow(() -> new GroupNotFoundException(id));

// ❌ BAD - Returning null
public Group findGroupById(String id) {
    return groupRepository.findById(id).orElse(null); // Null prone!
}

// ❌ BAD - Optional for parameters (anti-pattern)
public void updateGroup(Optional<String> groupId) { // Don't do this!
    // Caller should handle Optional before calling
}

// ✅ GOOD - @Nullable annotation for parameters when necessary
public void updateGroup(@Nullable String notes) {
    if (notes != null) {
        // Process notes
    }
}
```

#### Parameter Validation

```java
// ✅ GOOD - Fail fast with Objects.requireNonNull
public void addMember(String groupId, Member member) {
    Objects.requireNonNull(groupId, "groupId must not be null");
    Objects.requireNonNull(member, "member must not be null");
    
    if (groupId.isBlank()) {
        throw new IllegalArgumentException("groupId must not be blank");
    }
    
    // Implementation
}

// ✅ GOOD - Validation in constructor
public class CreateGroupRequest {
    private final String name;
    private final List<String> memberIds;
    
    public CreateGroupRequest(String name, List<String> memberIds) {
        this.name = Objects.requireNonNull(name, "name is required");
        this.memberIds = List.copyOf(
            Objects.requireNonNull(memberIds, "memberIds is required")
        );
        
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
    }
}
```

#### Spring Annotations for Null Safety

```java
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class GroupService {
    
    // IDE will warn if null is passed
    public void processGroup(@NonNull Group group, @Nullable String notes) {
        // group is guaranteed non-null by IDE and runtime checks
        // notes can be null
    }
    
    @NonNull
    public Group getGroup(String id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new GroupNotFoundException(id));
    }
}
```

### Streams API

#### Effective Stream Usage

```java
// ✅ GOOD - Clear stream pipeline
public List<String> getActiveMemberNames(String groupId) {
    return groupRepository.findById(groupId)
        .map(Group::getMembers)
        .orElse(List.of())
        .stream()
        .filter(Member::isActive)
        .map(Member::getName)
        .sorted()
        .collect(Collectors.toList());
}

// ✅ GOOD - Complex operation split for readability
public Map<String, Double> calculateBalancesByMember(List<Expense> expenses) {
    // Group expenses by payer
    Map<String, List<Expense>> expensesByPayer = expenses.stream()
        .collect(Collectors.groupingBy(Expense::getPaidBy));
    
    // Calculate total per member
    return expensesByPayer.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> calculateTotal(entry.getValue())
        ));
}

// ❌ BAD - Overly complex one-liner
public Map<String, Double> calculateBalancesByMember(List<Expense> expenses) {
    return expenses.stream().collect(Collectors.groupingBy(Expense::getPaidBy))
        .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, 
        entry -> entry.getValue().stream().mapToDouble(Expense::getAmount).sum()));
}

// ✅ GOOD - Traditional loop when more readable
public double calculateTotalForMember(String memberId, List<Expense> expenses) {
    double total = 0.0;
    for (Expense expense : expenses) {
        if (expense.isOwedByMember(memberId)) {
            total += expense.getAmountOwedBy(memberId);
        }
    }
    return total;
}
```

#### Stream Guidelines

**When to use Streams**:
- Simple transformations (map, filter)
- Aggregations (sum, count, max)
- 2-4 operations in pipeline

**When to use traditional loops**:
- Complex logic inside operations
- Need to break/continue based on conditions
- > 4 operations in pipeline
- Better performance needed (after profiling)

#### Avoid Side Effects in Streams

```java
// ❌ BAD - Modifying external state in stream
List<Expense> expenses = new ArrayList<>();
double totalAmount = 0.0; // Mutable state

group.getMembers().stream().forEach(member -> {
    Expense expense = new Expense(member.getId(), 100.0);
    expenses.add(expense); // Side effect
    totalAmount += expense.getAmount(); // Modifying external variable
});

// ✅ GOOD - Pure functional operations
List<Expense> expenses = group.getMembers().stream()
    .map(member -> new Expense(member.getId(), 100.0))
    .collect(Collectors.toList());

double totalAmount = expenses.stream()
    .mapToDouble(Expense::getAmount)
    .sum();
```

### Exception Handling

#### Prefer Unchecked Exceptions

```java
// ✅ GOOD - Business exceptions are unchecked
public class InsufficientBalanceException extends RuntimeException {
    private final String memberId;
    private final Money required;
    private final Money available;
    
    public InsufficientBalanceException(String memberId, Money required, Money available) {
        super(String.format("Insufficient balance for member %s: required %s, available %s",
            memberId, required, available));
        this.memberId = memberId;
        this.required = required;
        this.available = available;
    }
    
    // Getters for error details
}

// ❌ BAD - Checked exception for business logic
public class InsufficientBalanceException extends Exception { // Checked exception
    // Forces every caller to handle or declare
}
```

#### Proper Exception Handling

```java
// ✅ GOOD - Specific exception handling with context
public void processExpense(String groupId, ExpenseRequest request) {
    try {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        validateExpense(request);
        
        Expense expense = createExpense(group, request);
        expenseRepository.save(expense);
        
        log.info("Expense created successfully: groupId={}, expenseId={}", 
            groupId, expense.getId());
            
    } catch (ValidationException e) {
        log.error("Expense validation failed: groupId={}, reason={}", 
            groupId, e.getMessage(), e);
        throw new BadRequestException("Invalid expense data", e);
    } catch (DataAccessException e) {
        log.error("Database error while saving expense: groupId={}", groupId, e);
        throw new ServiceException("Failed to save expense", e);
    }
}

// ❌ BAD - Catching generic Exception
public void processExpense(String groupId, ExpenseRequest request) {
    try {
        // ... implementation
    } catch (Exception e) { // Too broad!
        log.error("Error", e);
        throw new RuntimeException(e);
    }
}

// ❌ BAD - Swallowing exceptions
public void processExpense(String groupId, ExpenseRequest request) {
    try {
        // ... implementation
    } catch (ValidationException e) {
        // Silent failure - exception lost!
    }
}
```

---

## Spring Boot Fundamentals

### Dependency Injection

#### Constructor Injection (Mandatory)

```java
// ✅ GOOD - Constructor injection with final fields
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final BalanceCalculator balanceCalculator;
    
    // No @Autowired needed in modern Spring
    public ExpenseService(
            ExpenseRepository expenseRepository,
            GroupRepository groupRepository,
            BalanceCalculator balanceCalculator) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.balanceCalculator = balanceCalculator;
    }
    
    // Methods
}

// ✅ GOOD - Lombok @RequiredArgsConstructor for brevity
@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final BalanceCalculator balanceCalculator;
    
    // Constructor auto-generated by Lombok
}

// ❌ BAD - Field injection (harder to test, hidden dependencies)
@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    // Harder to mock in tests
}

// ❌ BAD - Setter injection (allows mutation)
@Service
public class ExpenseService {
    private ExpenseRepository expenseRepository;
    
    @Autowired
    public void setExpenseRepository(ExpenseRepository repository) {
        this.expenseRepository = repository;
    }
}
```

**Benefits of Constructor Injection**:
1. **Immutability**: Dependencies are `final`
2. **Testability**: Easy to pass mocks in unit tests
3. **Explicit**: All dependencies visible in constructor
4. **Null-Safety**: Cannot create instance without dependencies
5. **No Reflection**: No need for `@Autowired` annotation

#### Avoiding Circular Dependencies

```java
// ❌ BAD - Circular dependency
@Service
public class GroupService {
    private final ExpenseService expenseService;
    
    public GroupService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
}

@Service
public class ExpenseService {
    private final GroupService groupService; // Circular!
    
    public ExpenseService(GroupService groupService) {
        this.groupService = groupService;
    }
}

// ✅ GOOD - Extract shared logic to separate service
@Service
public class BalanceCalculationService {
    private final ExpenseRepository expenseRepository;
    
    public BalanceCalculationService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    
    public Map<String, Double> calculateBalances(String groupId) {
        // Shared calculation logic
    }
}

@Service
public class GroupService {
    private final BalanceCalculationService balanceService;
    // No dependency on ExpenseService
}

@Service
public class ExpenseService {
    private final BalanceCalculationService balanceService;
    // No dependency on GroupService
}
```

### Component Annotations

#### Use Specific Stereotypes

```java
// ✅ GOOD - Specific annotations clarify intent
@RestController
@RequestMapping("/api/groups")
public class GroupController {
    // REST endpoint handling
}

@Service
public class GroupService {
    // Business logic
}

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    // Data access
}

@Configuration
public class SecurityConfig {
    // Configuration
}

// ❌ BAD - Generic @Component everywhere
@Component
public class GroupController { } // Use @RestController

@Component
public class GroupService { } // Use @Service

@Component
public class GroupRepository { } // Use @Repository
```

#### Component Naming Conventions

```java
// ✅ GOOD - Descriptive, specific names
@Service
public class ExpenseCalculationService { }

@Service
public class BalanceSettlementService { }

@Service
public class GroupMembershipService { }

@Component
public class EmailTemplateRenderer { }

// ❌ BAD - Generic, vague names
@Service
public class ExpenseService { } // Too generic, does everything?

@Service
public class ExpenseManager { } // Avoid "Manager" suffix

@Service
public class ExpenseHelper { } // Avoid "Helper" suffix

@Service
public class ExpenseUtil { } // Avoid "Util" suffix
```

---

## Project Structure

### Package Organization

#### Layer-Based Structure (Current/Simple Projects)

```
com.company.splitwise/
├── config/              # Spring configuration classes
│   ├── SecurityConfig.java
│   ├── MongoConfig.java
│   └── WebMvcConfig.java
├── controller/          # REST controllers
│   ├── GroupController.java
│   ├── ExpenseController.java
│   └── MemberController.java
├── service/             # Business logic
│   ├── GroupService.java
│   ├── ExpenseService.java
│   └── BalanceCalculationService.java
├── repository/          # Data access
│   ├── GroupRepository.java
│   ├── ExpenseRepository.java
│   └── MemberRepository.java
├── model/               # Domain entities
│   ├── Group.java
│   ├── Expense.java
│   └── Member.java
├── dto/                 # Data transfer objects
│   ├── request/
│   │   ├── CreateGroupRequest.java
│   │   └── AddExpenseRequest.java
│   └── response/
│       ├── GroupDTO.java
│       └── ExpenseDTO.java
├── exception/           # Custom exceptions
│   ├── GroupNotFoundException.java
│   ├── ValidationException.java
│   └── GlobalExceptionHandler.java
├── util/                # Utility classes
│   ├── DateUtils.java
│   └── ValidationUtils.java
└── SplitwiseApplication.java
```

#### Feature-Based Structure (Recommended for Growth)

```
com.company.splitwise/
├── group/
│   ├── GroupController.java
│   ├── GroupService.java
│   ├── GroupRepository.java
│   ├── Group.java (entity)
│   ├── GroupDTO.java
│   ├── CreateGroupRequest.java
│   └── GroupNotFoundException.java
├── expense/
│   ├── ExpenseController.java
│   ├── ExpenseService.java
│   ├── ExpenseRepository.java
│   ├── Expense.java
│   ├── ExpenseDTO.java
│   └── AddExpenseRequest.java
├── member/
│   ├── MemberService.java
│   ├── Member.java
│   └── MemberDTO.java
├── settlement/
│   ├── SettlementService.java
│   ├── BalanceCalculator.java
│   └── Settlement.java
├── common/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── MongoConfig.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   └── util/
│       └── DateUtils.java
└── SplitwiseApplication.java
```

**Benefits of Feature-Based**:
- Related code is co-located
- Easier to understand feature scope
- Better encapsulation
- Easier to extract into microservices later

---

## Controller Layer

### Controller Responsibilities

**MUST**:
- Handle HTTP request/response
- Validate request (with `@Valid`)
- Delegate to service layer
- Map service results to DTOs
- Return appropriate HTTP status codes

**MUST NOT**:
- Contain business logic
- Access repository directly
- Handle transactions
- Perform complex data transformations

### Controller Best Practices

```java
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
@Validated
public class GroupController {
    
    private final GroupService groupService;
    
    // ✅ GOOD - Clean controller method
    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        log.info("Creating group: name={}, members={}", 
            request.getName(), request.getInitialMembers().size());
        
        GroupDTO group = groupService.createGroup(request);
        
        return ResponseEntity
            .created(URI.create("/api/groups/" + group.getId()))
            .body(group);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable String id) {
        log.debug("Fetching group: id={}", id);
        
        return groupService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GroupDTO> updateGroup(
            @PathVariable String id,
            @Valid @RequestBody UpdateGroupRequest request) {
        
        GroupDTO updated = groupService.updateGroup(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    public ResponseEntity<Page<GroupDTO>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate,desc") String sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort.split(",")));
        Page<GroupDTO> groups = groupService.findAll(pageable);
        
        return ResponseEntity.ok(groups);
    }
}

// ❌ BAD - Business logic in controller
@RestController
public class GroupController {
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @PostMapping("/api/groups")
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest request) {
        // ❌ Business logic should be in service
        if (groupRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Group already exists");
        }
        
        // ❌ Direct repository access
        Group group = new Group();
        group.setName(request.getName());
        
        // ❌ Complex logic in controller
        List<Member> members = new ArrayList<>();
        for (String memberId : request.getMemberIds()) {
            Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
            members.add(member);
        }
        group.setMembers(members);
        
        Group saved = groupRepository.save(group);
        return ResponseEntity.ok(saved); // ❌ Exposing entity instead of DTO
    }
}
```

### HTTP Status Codes

```java
// ✅ GOOD - Appropriate status codes
public class GroupController {
    
    @PostMapping
    public ResponseEntity<GroupDTO> create(@Valid @RequestBody CreateGroupRequest request) {
        GroupDTO group = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group); // 201
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> get(@PathVariable String id) {
        return service.findById(id)
            .map(ResponseEntity::ok) // 200
            .orElse(ResponseEntity.notFound().build()); // 404
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GroupDTO> update(
            @PathVariable String id, 
            @Valid @RequestBody UpdateGroupRequest request) {
        GroupDTO updated = service.update(id, request);
        return ResponseEntity.ok(updated); // 200
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
    
    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable String id,
            @Valid @RequestBody AddMemberRequest request) {
        service.addMember(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
    }
}
```

**Common Status Codes**:
- `200 OK` - Successful GET, PUT
- `201 CREATED` - Successful POST with resource creation
- `204 NO CONTENT` - Successful DELETE or operation with no response body
- `400 BAD REQUEST` - Validation error, malformed request
- `404 NOT FOUND` - Resource doesn't exist
- `409 CONFLICT` - Business rule violation (e.g., duplicate)
- `500 INTERNAL SERVER ERROR` - Unexpected server error

### Request Validation

```java
// DTO with validation annotations
public class CreateGroupRequest {
    
    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 100, message = "Group name must be between 3 and 100 characters")
    private String name;
    
    @NotNull(message = "Initial members list is required")
    @Size(min = 2, message = "Group must have at least 2 members")
    private List<@NotBlank String> initialMembers;
    
    @Email(message = "Invalid email format")
    @NotBlank
    private String createdBy;
    
    // Constructors, getters
}

// Controller using @Valid
@PostMapping
public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody CreateGroupRequest request) {
    // Validation happens automatically before this method is called
    // If validation fails, MethodArgumentNotValidException is thrown
    GroupDTO group = groupService.createGroup(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(group);
}
```

---

## Service Layer

### Service Responsibilities

**MUST**:
- Contain business logic
- Coordinate between repositories
- Handle transactions
- Validate business rules
- Transform entities to DTOs

**MUST NOT**:
- Handle HTTP concerns (status codes, headers)
- Know about request/response objects
- Depend on controller layer

### Service Best Practices

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMapper groupMapper;
    
    // ✅ GOOD - Clear, focused service method
    @Transactional
    public GroupDTO createGroup(CreateGroupRequest request) {
        log.info("Creating group: name={}", request.getName());
        
        // Business validation
        validateUniqueGroupName(request.getName());
        validateMembers(request.getInitialMembers());
        
        // Create entity
        Group group = new Group();
        group.setName(request.getName());
        group.setCreatedDate(Instant.now());
        group.setCreatedBy(request.getCreatedBy());
        
        // Add members
        List<Member> members = loadMembers(request.getInitialMembers());
        group.setMembers(members);
        
        // Save
        Group saved = groupRepository.save(group);
        
        log.info("Group created successfully: id={}, name={}", saved.getId(), saved.getName());
        
        return groupMapper.toDTO(saved);
    }
    
    public Optional<GroupDTO> findById(String id) {
        return groupRepository.findById(id)
            .map(groupMapper::toDTO);
    }
    
    @Transactional
    public GroupDTO updateGroup(String id, UpdateGroupRequest request) {
        Group group = groupRepository.findById(id)
            .orElseThrow(() -> new GroupNotFoundException(id));
        
        if (request.getName() != null) {
            validateUniqueGroupName(request.getName(), id);
            group.setName(request.getName());
        }
        
        Group updated = groupRepository.save(group);
        return groupMapper.toDTO(updated);
    }
    
    @Transactional
    public void deleteGroup(String id) {
        if (!groupRepository.existsById(id)) {
            throw new GroupNotFoundException(id);
        }
        groupRepository.deleteById(id);
        log.info("Group deleted: id={}", id);
    }
    
    // Private helper methods
    private void validateUniqueGroupName(String name) {
        validateUniqueGroupName(name, null);
    }
    
    private void validateUniqueGroupName(String name, String excludeId) {
        boolean exists = groupRepository.existsByNameAndIdNot(name, excludeId);
        if (exists) {
            throw new DuplicateGroupNameException(name);
        }
    }
    
    private void validateMembers(List<String> memberIds) {
        List<Member> members = memberRepository.findAllById(memberIds);
        if (members.size() != memberIds.size()) {
            throw new MemberNotFoundException("One or more members not found");
        }
    }
    
    private List<Member> loadMembers(List<String> memberIds) {
        return memberRepository.findAllById(memberIds);
    }
}
```

### Transaction Management

```java
// ✅ GOOD - @Transactional on methods that modify data
@Service
public class ExpenseService {
    
    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpenses(String groupId) {
        // Read-only transaction
        return expenseRepository.findByGroupId(groupId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ExpenseDTO createExpense(String groupId, AddExpenseRequest request) {
        // Write transaction
        Group group = getGroupOrThrow(groupId);
        Expense expense = buildExpense(group, request);
        Expense saved = expenseRepository.save(expense);
        updateGroupBalances(group, expense); // Atomic with save
        return toDTO(saved);
    }
    
    @Transactional
    public void deleteExpense(String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        
        expenseRepository.deleteById(expenseId);
        recalculateGroupBalances(expense.getGroupId()); // Atomic with delete
    }
}

// ❌ BAD - Missing @Transactional
public void createExpense(AddExpenseRequest request) {
    Expense expense = buildExpense(request);
    expenseRepository.save(expense); // Not in transaction
    updateBalances(expense); // Could fail, leaving data inconsistent
}
```

**Transaction Guidelines**:
- Use `@Transactional(readOnly = true)` for queries
- Use `@Transactional` for operations that modify data
- Keep transactions short - don't perform expensive operations in transactions
- Avoid nested transactions when possible
- Don't catch exceptions in transactional methods without rethrowing

---

## Repository Layer

### Repository Basics

```java
// ✅ GOOD - Simple repository interface
@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    
    Optional<Group> findByName(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, String excludeId);
    
    List<Group> findByCreatedBy(String userId);
    
    @Query("{'members.id': ?0}")
    List<Group> findByMemberId(String memberId);
}

// ✅ GOOD - Custom query methods
@Repository
public interface ExpenseRepository extends MongoRepository<Expense, String> {
    
    List<Expense> findByGroupId(String groupId);
    
    List<Expense> findByGroupIdOrderByCreatedDateDesc(String groupId);
    
    @Query("{'groupId': ?0, 'amount': {$gte: ?1}}")
    List<Expense> findByGroupIdAndAmountGreaterThan(String groupId, double amount);
    
    @Query(value = "{'groupId': ?0}", fields = "{'description': 1, 'amount': 1}")
    List<Expense> findExpenseSummaryByGroupId(String groupId);
    
    void deleteByGroupId(String groupId);
}
```

### Query Methods Naming Convention

Spring Data MongoDB derives queries from method names:

```java
// Property equality
findByName(String name)
findByGroupIdAndPaidBy(String groupId, String paidBy)

// Comparison
findByAmountGreaterThan(double amount)
findByAmountLessThanEqual(double amount)
findByCreatedDateBetween(Instant start, Instant end)

// String operations
findByNameContaining(String keyword)
findByNameStartingWith(String prefix)
findByNameIgnoreCase(String name)

// Collections
findByMembersContaining(Member member)
findByGroupIdIn(List<String> groupIds)

// Boolean
existsByName(String name)
existsByGroupIdAndActive(String groupId, boolean active)

// Sorting
findByGroupIdOrderByCreatedDateDesc(String groupId)

// Limiting
findTop10ByGroupIdOrderByAmountDesc(String groupId)
```

---

## Domain Models & Entities

### Entity Design

```java
// ✅ GOOD - Well-designed MongoDB entity
@Document(collection = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String name;
    
    @DBRef
    private List<Member> members;
    
    private String createdBy;
    
    @Indexed
    private Instant createdDate;
    
    private Instant lastModifiedDate;
    
    private boolean active;
    
    @Version
    private Long version; // Optimistic locking
    
    // Business methods
    public void addMember(Member member) {
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(member);
    }
    
    public boolean hasMember(String memberId) {
        return members != null && members.stream()
            .anyMatch(m -> m.getId().equals(memberId));
    }
    
    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = Instant.now();
        }
        active = true;
    }
    
    @PreUpdate
    public void preUpdate() {
        lastModifiedDate = Instant.now();
    }
}
```

### Value Objects with Records

```java
// ✅ GOOD - Value object for money
public record Money(BigDecimal amount, String currency) {
    
    public Money {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }
    
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(double factor) {
        return new Money(
            this.amount.multiply(BigDecimal.valueOf(factor)), 
            this.currency
        );
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot operate on different currencies: " + 
                this.currency + " and " + other.currency
            );
        }
    }
}
```

---

## DTOs & Validation

### DTO Design

```java
// ✅ GOOD - Request DTO with validation
public class CreateGroupRequest {
    
    @NotBlank(message = "validation.group.name.required")
    @Size(min = 3, max = 100, message = "validation.group.name.size")
    private String name;
    
    @NotNull(message = "validation.group.members.required")
    @Size(min = 2, message = "validation.group.members.min")
    private List<@NotBlank(message = "validation.member.id.required") String> initialMembers;
    
    @NotBlank(message = "validation.created_by.required")
    @Email(message = "validation.email.format")
    private String createdBy;
    
    // Getters, setters, constructors
}

// ✅ GOOD - Response DTO
@Data
@Builder
public class GroupDTO {
    private String id;
    private String name;
    private List<MemberDTO> members;
    private String createdBy;
    private Instant createdDate;
    private boolean active;
}

// ✅ GOOD - Nested DTO
@Data
@Builder
public class MemberDTO {
    private String id;
    private String name;
    private String email;
}
```

### Mapping Entities to DTOs

```java
// ✅ GOOD - MapStruct mapper
@Mapper(componentModel = "spring")
public interface GroupMapper {
    
    GroupDTO toDTO(Group group);
    
    List<GroupDTO> toDTOList(List<Group> groups);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    Group toEntity(CreateGroupRequest request);
}

// ✅ GOOD - Manual mapper when MapStruct is not available
@Component
public class GroupMapper {
    
    private final MemberMapper memberMapper;
    
    public GroupMapper(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }
    
    public GroupDTO toDTO(Group group) {
        if (group == null) {
            return null;
        }
        
        return GroupDTO.builder()
            .id(group.getId())
            .name(group.getName())
            .members(group.getMembers().stream()
                .map(memberMapper::toDTO)
                .collect(Collectors.toList()))
            .createdBy(group.getCreatedBy())
            .createdDate(group.getCreatedDate())
            .active(group.isActive())
            .build();
    }
    
    public List<GroupDTO> toDTOList(List<Group> groups) {
        return groups.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}
```

### Custom Validators

```java
// ✅ GOOD - Custom annotation
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidCurrencyValidator.class)
public @interface ValidCurrency {
    String message() default "Invalid currency code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator implementation
public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    
    private static final Set<String> VALID_CURRENCIES = Set.of("USD", "EUR", "GBP", "INR");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null check
        }
        return VALID_CURRENCIES.contains(value.toUpperCase());
    }
}

// Usage
public class AddExpenseRequest {
    @NotNull
    private Double amount;
    
    @NotBlank
    @ValidCurrency
    private String currency;
    
    // Other fields
}
```

---

## Exception Handling

### Exception Hierarchy

```java
// ✅ GOOD - Domain exception hierarchy
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    
    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super("ERR_NOT_FOUND", 
            String.format("%s not found: %s", resourceType, resourceId));
    }
}

public class GroupNotFoundException extends ResourceNotFoundException {
    public GroupNotFoundException(String groupId) {
        super("Group", groupId);
    }
}

public class ValidationException extends BusinessException {
    private final List<String> validationErrors;
    
    public ValidationException(String message, List<String> errors) {
        super("ERR_VALIDATION", message);
        this.validationErrors = List.copyOf(errors);
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceType, String identifier) {
        super("ERR_DUPLICATE",
            String.format("%s already exists: %s", resourceType, identifier));
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .path(getPath(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex,
            WebRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .path(getPath(request))
            .validationErrors(ex.getValidationErrors())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request data")
            .errorCode("ERR_VALIDATION")
            .path(getPath(request))
            .validationErrors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex,
            WebRequest request) {
        
        log.error("Duplicate resource: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .path(getPath(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .errorCode("ERR_INTERNAL")
            .path(getPath(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    private String getPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}

@Data
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String errorCode;
    private String path;
    private List<String> validationErrors;
}
```

---

## MongoDB Best Practices

### Indexing

```java
// ✅ GOOD - Proper indexing
@Document(collection = "groups")
@CompoundIndex(def = "{'name': 1, 'createdBy': 1}")
public class Group {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String name;
    
    @Indexed
    private String createdBy;
    
    @Indexed
    private Instant createdDate;
}

// ✅ GOOD - Programmatic index creation
@Configuration
public class MongoIndexConfig {
    
    @Bean
    public CommandLineRunner initIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            // Create index on frequently queried fields
            mongoTemplate.indexOps(Expense.class)
                .ensureIndex(new Index()
                    .on("groupId", Sort.Direction.ASC)
                    .on("createdDate", Sort.Direction.DESC)
                );
            
            // Text index for search
            mongoTemplate.indexOps(Group.class)
                .ensureIndex(new Index()
                    .on("name", Sort.Direction.ASC)
                    .unique()
                );
        };
    }
}
```

### Avoiding N+1 Queries

```java
// ❌ BAD - N+1 query problem
public List<GroupWithExpensesDTO> getAllGroupsWithExpenses() {
    List<Group> groups = groupRepository.findAll(); // 1 query
    
    return groups.stream()
        .map(group -> {
            // N queries (one per group!)
            List<Expense> expenses = expenseRepository.findByGroupId(group.getId());
            return new GroupWithExpensesDTO(group, expenses);
        })
        .collect(Collectors.toList());
}

// ✅ GOOD - Batch fetch to avoid N+1
public List<GroupWithExpensesDTO> getAllGroupsWithExpenses() {
    List<Group> groups = groupRepository.findAll(); // 1 query
    
    // Extract group IDs
    List<String> groupIds = groups.stream()
        .map(Group::getId)
        .collect(Collectors.toList());
    
    // Single query for all expenses
    List<Expense> allExpenses = expenseRepository.findByGroupIdIn(groupIds); // 1 query
    
    // Group expenses by groupId
    Map<String, List<Expense>> expensesByGroup = allExpenses.stream()
        .collect(Collectors.groupingBy(Expense::getGroupId));
    
    // Combine groups with their expenses
    return groups.stream()
        .map(group -> new GroupWithExpensesDTO(
            group,
            expensesByGroup.getOrDefault(group.getId(), List.of())
        ))
        .collect(Collectors.toList());
}
```

### Aggregation Pipeline

```java
// ✅ GOOD - MongoDB aggregation for complex queries
@Repository
public interface ExpenseRepository extends MongoRepository<Expense, String> {
    
    @Aggregation(pipeline = {
        "{ $match: { 'groupId': ?0 } }",
        "{ $group: { _id: '$paidBy', total: { $sum: '$amount' } } }",
        "{ $sort: { total: -1 } }"
    })
    List<MemberExpenseSummary> getMemberExpenseSummary(String groupId);
}

public interface MemberExpenseSummary {
    String get_id(); // MongoDB aggregation result
    Double getTotal();
}
```

---

## Configuration Management

### Application Properties

```yaml
# application.yml

spring:
  application:
    name: splitwise-backend
  
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/splitwise}
      auto-index-creation: true
  
  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
      indent-output: true

server:
  port: ${SERVER_PORT:8080}
  error:
    include-message: always
    include-stacktrace: on_param

logging:
  level:
    root: INFO
    com.company.splitwise: DEBUG
    org.springframework.data.mongodb: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

app:
  max-group-members: 50
  default-currency: USD
  supported-currencies: USD,EUR,GBP,INR
```

### Profile-Specific Configuration

```yaml
# application-dev.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/splitwise_dev

logging:
  level:
    com.company.splitwise: DEBUG

# application-prod.yml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}

logging:
  level:
    com.company.splitwise: INFO
```

### Configuration Properties Class

```java
// ✅ GOOD - Type-safe configuration
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
@Data
public class AppConfig {
    
    @Min(2)
    @Max(100)
    private int maxGroupMembers = 50;
    
    @NotBlank
    private String defaultCurrency = "USD";
    
    @NotNull
    private List<String> supportedCurrencies = List.of("USD", "EUR", "GBP", "INR");
    
    // Usage in service
    @Service
    public class GroupService {
        private final AppConfig appConfig;
        
        public void validateGroupSize(int memberCount) {
            if (memberCount > appConfig.getMaxGroupMembers()) {
                throw new ValidationException(
                    "Group cannot have more than " + 
                    appConfig.getMaxGroupMembers() + " members"
                );
            }
        }
    }
}
```

---

## Testing

### Unit Testing Services

```java
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
    
    @Mock
    private GroupRepository groupRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private GroupMapper groupMapper;
    
    @InjectMocks
    private GroupService groupService;
    
    @Test
    void createGroup_ShouldCreateSuccessfully_WhenValidRequest() {
        // Arrange
        CreateGroupRequest request = CreateGroupRequest.builder()
            .name("Test Group")
            .initialMembers(List.of("member1", "member2"))
            .createdBy("user@example.com")
            .build();
        
        when(groupRepository.existsByName("Test Group")).thenReturn(false);
        
        List<Member> members = List.of(
            new Member("member1", "Alice"),
            new Member("member2", "Bob")
        );
        when(memberRepository.findAllById(anyList())).thenReturn(members);
        
        Group savedGroup = Group.builder()
            .id("group1")
            .name("Test Group")
            .members(members)
            .build();
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);
        
        GroupDTO expectedDTO = GroupDTO.builder()
            .id("group1")
            .name("Test Group")
            .build();
        when(groupMapper.toDTO(savedGroup)).thenReturn(expectedDTO);
        
        // Act
        GroupDTO result = groupService.createGroup(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("group1", result.getId());
        assertEquals("Test Group", result.getName());
        
        verify(groupRepository).existsByName("Test Group");
        verify(memberRepository).findAllById(List.of("member1", "member2"));
        verify(groupRepository).save(any(Group.class));
    }
    
    @Test
    void createGroup_ShouldThrowException_WhenDuplicateName() {
        // Arrange
        CreateGroupRequest request = CreateGroupRequest.builder()
            .name("Duplicate Group")
            .initialMembers(List.of("member1"))
            .build();
        
        when(groupRepository.existsByName("Duplicate Group")).thenReturn(true);
        
        // Act & Assert
        assertThrows(DuplicateGroupNameException.class, 
            () -> groupService.createGroup(request));
        
        verify(groupRepository).existsByName("Duplicate Group");
        verify(groupRepository, never()).save(any());
    }
    
    @Test
    void findById_ShouldReturnEmpty_WhenGroupNotFound() {
        // Arrange
        when(groupRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Optional<GroupDTO> result = groupService.findById("nonexistent");
        
        // Assert
        assertFalse(result.isPresent());
    }
}
```

### Integration Testing with Testcontainers

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class GroupRepositoryIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Autowired
    private GroupRepository groupRepository;
    
    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();
    }
    
    @Test
    void shouldSaveAndRetrieveGroup() {
        // Arrange
        Group group = Group.builder()
            .name("Test Group")
            .createdBy("user@example.com")
            .createdDate(Instant.now())
            .members(new ArrayList<>())
            .active(true)
            .build();
        
        // Act
        Group saved = groupRepository.save(group);
        Optional<Group> retrieved = groupRepository.findById(saved.getId());
        
        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("Test Group", retrieved.get().getName());
        assertEquals("user@example.com", retrieved.get().getCreatedBy());
    }
    
    @Test
    void shouldFindGroupByName() {
        // Arrange
        Group group = Group.builder()
            .name("Unique Group")
            .createdBy("user@example.com")
            .members(new ArrayList<>())
            .build();
        groupRepository.save(group);
        
        // Act
        Optional<Group> found = groupRepository.findByName("Unique Group");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("Unique Group", found.get().getName());
    }
    
    @Test
    void shouldEnforceUniqueNameConstraint() {
        // Arrange
        Group group1 = Group.builder().name("Same Name").build();
        Group group2 = Group.builder().name("Same Name").build();
        
        groupRepository.save(group1);
        
        // Act & Assert
        assertThrows(DuplicateKeyException.class, () -> groupRepository.save(group2));
    }
}
```

### Controller Testing

```java
@WebMvcTest(GroupController.class)
class GroupControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GroupService groupService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createGroup_ShouldReturn201_WhenValidRequest() throws Exception {
        // Arrange
        CreateGroupRequest request = CreateGroupRequest.builder()
            .name("Test Group")
            .initialMembers(List.of("member1", "member2"))
            .createdBy("user@example.com")
            .build();
        
        GroupDTO response = GroupDTO.builder()
            .id("group1")
            .name("Test Group")
            .members(List.of())
            .build();
        
        when(groupService.createGroup(any())).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("group1"))
            .andExpect(jsonPath("$.name").value("Test Group"));
    }
    
    @Test
    void createGroup_ShouldReturn400_WhenInvalidRequest() throws Exception {
        // Arrange - Invalid request (no name)
        CreateGroupRequest request = CreateGroupRequest.builder()
            .initialMembers(List.of("member1"))
            .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void getGroup_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(groupService.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/groups/nonexistent"))
            .andExpect(status().isNotFound());
    }
}
```

---

## Logging

### Logging Best Practices

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    public ExpenseDTO createExpense(String groupId, AddExpenseRequest request) {
        // ✅ GOOD - Parameterized logging
        log.info("Creating expense: groupId={}, amount={}, description={}", 
            groupId, request.getAmount(), request.getDescription());
        
        try {
            validateExpense(request);
            Expense expense = buildExpense(groupId, request);
            Expense saved = expenseRepository.save(expense);
            
            log.info("Expense created successfully: id={}, groupId={}, amount={}", 
                saved.getId(), groupId, saved.getAmount());
            
            return toDTO(saved);
            
        } catch (ValidationException e) {
            log.error("Expense validation failed: groupId={}, reason={}", 
                groupId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating expense: groupId={}", groupId, e);
            throw new ServiceException("Failed to create expense", e);
        }
    }
    
    public void deleteExpense(String expenseId) {
        log.debug("Deleting expense: id={}", expenseId);
        
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> {
                log.warn("Attempted to delete non-existent expense: id={}", expenseId);
                return new ExpenseNotFoundException(expenseId);
            });
        
        expenseRepository.deleteById(expenseId);
        log.info("Expense deleted: id={}, groupId={}", expenseId, expense.getGroupId());
    }
}
```

### Structured Logging

```java
// ✅ GOOD - Structured logging with context
import org.slf4j.MDC;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("userId", extractUserId(request));
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

// Now all logs will include requestId and userId
log.info("Processing request"); 
// Output: [requestId=abc-123] [userId=user@example.com] Processing request
```

---

## Security

### Input Sanitization

```java
// ✅ GOOD - Sanitize user input
@Service
public class GroupService {
    
    public GroupDTO createGroup(CreateGroupRequest request) {
        // Sanitize name
        String sanitizedName = sanitizeInput(request.getName());
        
        // Validate after sanitization
        if (sanitizedName.isBlank()) {
            throw new ValidationException("Group name cannot be empty after sanitization");
        }
        
        // Continue processing
    }
    
    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        return input
            .trim()
            .replaceAll("[<>\"']", "") // Remove potential XSS characters
            .replaceAll("\\s+", " "); // Normalize whitespace
    }
}
```

### Secrets Management

```yaml
# ❌ BAD - Hardcoded secrets
spring:
  data:
    mongodb:
      uri: mongodb://admin:password123@localhost:27017

# ✅ GOOD - Environment variables
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

---

## Performance

### Caching

```java
// ✅ GOOD - Caching configuration
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
            new ConcurrentMapCache("groups"),
            new ConcurrentMapCache("expenses")
        ));
        return cacheManager;
    }
}

// Service with caching
@Service
public class GroupService {
    
    @Cacheable(value = "groups", key = "#id")
    public Optional<GroupDTO> findById(String id) {
        return groupRepository.findById(id).map(this::toDTO);
    }
    
    @CacheEvict(value = "groups", key = "#id")
    public void deleteGroup(String id) {
        groupRepository.deleteById(id);
    }
    
    @CachePut(value = "groups", key = "#result.id")
    public GroupDTO updateGroup(String id, UpdateGroupRequest request) {
        // Update logic
        return updatedGroup;
    }
}
```

---

## Common Patterns

### Builder Pattern

```java
// ✅ GOOD - Using Lombok @Builder
@Data
@Builder
public class Expense {
    private String id;
    private String groupId;
    private String description;
    private double amount;
    private String paidBy;
    private List<String> splitBetween;
    private Instant createdDate;
}

// Usage
Expense expense = Expense.builder()
    .groupId("group1")
    .description("Dinner")
    .amount(100.0)
    .paidBy("user1")
    .splitBetween(List.of("user1", "user2"))
    .createdDate(Instant.now())
    .build();
```

---

## Anti-Patterns to Avoid

### Common Mistakes

```java
// ❌ BAD - God service
@Service
public class GroupService {
    // 50+ methods handling groups, expenses, members, settlements, etc.
}

// ✅ GOOD - Separate services
@Service
public class GroupService { }

@Service
public class ExpenseService { }

@Service
public class MembershipService { }

@Service
public class SettlementService { }
```

---

## Code Review Checklist

- [ ] Constructor injection used (no field injection)
- [ ] Controllers are thin (no business logic)
- [ ] Services contain business logic
- [ ] DTOs used (not exposing entities)
- [ ] Bean Validation annotations present
- [ ] Custom exceptions with global handler
- [ ] @Transactional on write operations
- [ ] Optional for return types, not parameters
- [ ] Proper logging (INFO for events, ERROR for failures)
- [ ] No hardcoded secrets
- [ ] MongoDB indexes on queried fields
- [ ] Unit tests for services
- [ ] Integration tests for repositories

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 2.0 | 2024-02-03 | Enhanced comprehensive version with better examples |
| 1.0 | 2024-01-15 | Initial version |

---

**See Also**:
- [Core Coding Standards](core-standards-enhanced.md)
- [React & JavaScript Standards](react-javascript-enhanced.md)
