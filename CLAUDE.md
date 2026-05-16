# iam-operations Coding Style

> **See also:** [`architecture.md`](architecture.md) — module role and package structure

---

## Domain Model POJOs

### Annotations

Every domain POJO must have:
- `@JsonIgnoreProperties(ignoreUnknown = true)` at the class level — resilient JSON deserialization

Bean Validation (`jakarta.validation`) annotations on fields as appropriate:
- `@NotEmpty` for required String and Collection fields
- `@NotNull` for required objects
- `@Email` for email address fields
- `@Size` for length-constrained fields (e.g. password minimum length)

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String userId;

    @NotEmpty
    private String username;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String passwordHash;

    private List<Role> roles;
```

### No Lombok

**Do not add Lombok.** Write all getters, setters, and `toString()` manually. No `@Data`, `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`.

### Constructors

- Provide a no-arg constructor
- Provide focused parameterized constructors for common initialization patterns:

  ```java
  public Token(String userId, String tokenValue) {
      this.userId = userId;
      this.tokenValue = tokenValue;
  }
  ```

### Getters and Setters

Standard JavaBean style — always use `this.field = field` in setters:

```java
public String getUserId() {
    return userId;
}

public void setUserId(String userId) {
    this.userId = userId;
}
```

### `toString()`

Concatenation format using `ClassName{field='value', ...}`:

```java
@Override
public String toString() {
    return "User{" +
            "userId='" + userId + '\'' +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            '}';
}
```

### No Spring Data MongoDB Annotations

Do **not** add `@Document`, `@Field`, or `@Id` to model classes. Document binding is done via `ObjectMapperUtil` (Jackson serialization).

---

## Enums

Use enums for all closed-value sets. Enums live in the same sub-package as their domain:

```java
public enum AuthResponseStatus { SUCCESS, FAILURE }
public enum AuthMessageType { INFORMATION, WARNING, ERROR }
public enum TokenStatus { ACTIVE, REVOKED, EXPIRED }
public enum AuthOperation { CREATE, MODIFY, DELETE }
```

For enums that need a display label, add a field + constructor + getter:

```java
public enum AuthOperation {
    CREATE("Create"), MODIFY("Modify"), DELETE("Delete");

    private final String displayName;

    AuthOperation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

---

## Constants Classes

Constants live in **plain classes** (not interfaces) with `public static final` fields.

- Group logically related constants in one class (`AuthMessageConstants` for messages, `IAMFieldNameConstants` for MongoDB field names)
- Use `/* Comment */` block comments to section constants within a class
- Constants only relevant to one class go on that class as `private static final`

```java
public class AuthMessageConstants {

    /* Error Messages */
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String USER_CREATION_FAILURE = "User Creation Failed";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";

    /* Success Messages */
    public static final String USER_CREATION_SUCCESS = "User Registered Successfully";
    public static final String LOGIN_SUCCESS = "Login Successful";
    public static final String LOGOUT_SUCCESS = "Logged Out Successfully";
}
```

---

## IAM Service Interfaces

Service interfaces define the IAM contract that DAO classes must implement.

- Methods throw `IAMException` for infrastructure failures
- Use `{@code true}` / `{@code false}` in return JavaDoc
- Full `@param`, `@return`, `@throws` on every method

```java
public interface UserIAMService {

    /**
     * Persists a new user record in the data store.
     *
     * @param user the user to create
     * @return {@code true} if the creation was successful, {@code false} otherwise
     * @throws IAMException if a data store error occurs
     */
    boolean createUser(User user) throws IAMException;

    /**
     * Retrieves a user by their username.
     *
     * @param username the unique username to look up
     * @return the matching {@link User}, or {@code null} if not found
     * @throws IAMException if a data store error occurs
     */
    User findByUsername(String username) throws IAMException;
}
```

---

## IAMException

`IAMException` is the single custom exception for all IAM and infrastructure errors. It must mirror all `RuntimeException` constructor overloads:

```java
public class IAMException extends RuntimeException {
    public IAMException() { super(); }
    public IAMException(String message) { super(message); }
    public IAMException(String message, Throwable cause) { super(message, cause); }
    public IAMException(Throwable cause) { super(cause); }
    protected IAMException(String message, Throwable cause,
                            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

---

## Response Model

### `AuthResponseStructure`

Universal response wrapper. The `payload` field is `Object` (generic — holds any domain object or `null`):

```java
public class AuthResponseStructure {
    private AuthResponseStatus responseStatus;   // SUCCESS or FAILURE
    private AuthResponseMessages messages;
    private Object payload;
}
```

### `AuthResponseMessages`

Groups messages by severity. Fields are `List<String>`:

```java
public class AuthResponseMessages {
    List<String> informationMessages;
    List<String> warningMessages;
    List<String> errorMessages;
}
```

All responses are built via `AuthResponseStructureUtil.generateResponseStructure(...)` — never construct `AuthResponseStructure` or `AuthResponseMessages` directly in business code.

---

## Utility Classes

Utility classes are **static-only** — no Spring annotation, no instantiation:

- All methods are `public static` (or `private static` for helpers)
- Logger declared as `public static final Logger logger = LogManager.getLogger(ClassName.class)` (Log4j2)

`EnvironmentUtil` is the single access point for all env var / system property resolution:

```java
public static String getEnvironmentVariable(String key) {
    return Optional.ofNullable(System.getProperty(key)).orElse(System.getenv(key));
}
```

- JVM system property takes priority over OS environment variable
- All other modules call `EnvironmentUtil.getEnvironmentVariable(KEY)` — never call `System.getenv()` or `System.getProperty()` directly elsewhere

---

## Component Declaration

All Spring-managed DAO classes use `@Component` + `@Scope(value = BeanDefinition.SCOPE_SINGLETON)` — **never `@Service` or `@Repository`**:

```java
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UserMongoDao implements UserIAMService {
```

---

## Initialization: `@PostConstruct`

Use `@PostConstruct` to read environment variables and initialize connection objects. Use `EnvironmentUtil.getEnvironmentVariable(CONSTANT_KEY)` — never call `System.getenv()` directly:

```java
@PostConstruct
public void setupMongoConnectionString() {
    this.dbConnectionString = EnvironmentUtil.getEnvironmentVariable(MONGODB_CONNECTION_STRING);
    this.dbName = EnvironmentUtil.getEnvironmentVariable(MONGODB_DATABASE_NAME);
    logger.debug("Value of dbConnectionString::{}", this.dbConnectionString);
    logger.debug("Value of dbName::{}", this.dbName);
    initMongoDbConnection();
}
```

Use `MongoClientSettings.builder()` for building the MongoDB client:

```java
MongoClientSettings mongoClientSettings =
        MongoClientSettings
                .builder()
                .writeConcern(WriteConcern.MAJORITY)
                .applyConnectionString(new ConnectionString(this.dbConnectionString))
                .build();

this.mongoClient = MongoClients.create(mongoClientSettings);
```

---

## Constants Placement

- Constants used externally (env var key names, collection names referenced in Javadoc) → `public static final` on the class
- Constants used only internally → `private static final` on the class
- Do **not** move these to a separate constants class; keep them on the class that owns them

```java
// MongoDBConnection.java — consumed externally
public static final String MONGODB_CONNECTION_STRING = "MONGODB_CONNECTION_STRING";
public static final String MONGODB_DATABASE_NAME = "MONGODB_DATABASE_NAME";

// UserMongoDao.java — public because referenced in @{value} Javadoc
public static final String USER_COLLECTION_NAME = "users";

// TokenMongoDao.java — public because referenced in @{value} Javadoc
public static final String TOKEN_COLLECTION_NAME = "tokens";
```

---

## MongoDB Operations Pattern

Follow the same patterns as `breakdown-mongo-adapter/CLAUDE.md` (accessing collections via `MongoDBOperations.getCollection()`, insert, fetch-with-filter, delete-then-insert for updates). IAM-specific differences:
- Use `IAMFieldNameConstants` for all field name strings (not `FieldNameConstants`)
- Use `IAMException` (not `SystemException`) when wrapping infrastructure errors
- `UserQueryBuilder` is the static filter factory (mirrors `TransactionQueryBuilder`); use `filter.put(...)` for first field, `filter.append(...)` for additional fields

---

## Ory Kratos IAM Implementation

`KratosUserIAMService` is the second implementation of `UserIAMService`. It delegates user identity operations to an **Ory Kratos** instance via its REST API instead of MongoDB.

### Environment Variables

- `KRATOS_ADMIN_URL` — base URL of the Kratos Admin API (e.g. `http://localhost:4434`). Used for create, update, delete operations.
- `KRATOS_PUBLIC_URL` — base URL of the Kratos Public API (e.g. `http://localhost:4433`). Used for login flow operations.

Read both via `EnvironmentUtil.getEnvironmentVariable(KEY)` in `@PostConstruct` — never call `System.getenv()` directly.

### Class Declaration

```java
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class KratosUserIAMService implements UserIAMService {

    public static final String KRATOS_ADMIN_URL = "KRATOS_ADMIN_URL";
    public static final String KRATOS_PUBLIC_URL = "KRATOS_PUBLIC_URL";

    private String kratosAdminUrl;
    private String kratosPublicUrl;

    @PostConstruct
    public void init() {
        this.kratosAdminUrl = EnvironmentUtil.getEnvironmentVariable(KRATOS_ADMIN_URL);
        this.kratosPublicUrl = EnvironmentUtil.getEnvironmentVariable(KRATOS_PUBLIC_URL);
        logger.info("Kratos Admin URL::{}", this.kratosAdminUrl);
        logger.info("Kratos Public URL::{}", this.kratosPublicUrl);
    }
```

### HTTP Client

Use the standard Java `HttpClient` (Java 11+) for all Kratos REST calls — no Spring `RestTemplate` or `WebClient`:

```java
private final HttpClient httpClient = HttpClient.newHttpClient();
```

### Error Handling

Wrap all HTTP and IO exceptions as `IAMException` before they leave the class — same pattern as MongoDB DAOs:

```java
} catch (IOException | InterruptedException e) {
    logger.error("Kratos API call failed");
    logger.error(ExceptionUtils.getStackTrace(e));
    throw new IAMException("Kratos API call failed: " + e.getMessage());
}
```

### Token and Role Storage

`KratosUserIAMService` handles **user identity only**. Token storage (`TokenMongoDao`) and role storage always use MongoDB — `PREFERRED_IAM=kratos` has no effect on `getPreferredTokenIAM()`.

---

## Exception Handling

### `throwIAMException()` Helper

Every class that calls MongoDB operations must have a private `throwIAMException(MongoException e)` method:

```java
private void throwIAMException(MongoException e) {
    logger.error("Mongo Exception Occurred");
    logger.error(ExceptionUtils.getStackTrace(e));
    throw new IAMException("Mongo Exception Occurred");
}
```

Call it from every `catch (MongoException e)` block:

```java
} catch (MongoException e) {
    throwIAMException(e);
    return false;
}
```

---

## Logging

Log `"Entering methodName"` / `"Exiting methodName"` at entry/exit of all public methods. Logger declaration and Log4j2 rules: see root `CLAUDE.md`.

---

## JavaDoc

Full prose Javadoc on all classes and public/package-private methods (including private helpers):

- **Class-level**: Describes purpose, collaborators via `{@link}`, and lifecycle responsibilities
- **Methods**: Full `@param`, `@return`, `@throws`. Use `{@inheritDoc}` on `@Override` implementations, plus a supplementary `<p>` block noting the collection name via `{@value #CONSTANT_NAME}`

  ```java
  /**
   * {@inheritDoc}
   * <p>Serialises the user to a {@link org.bson.Document} and inserts it
   * into the {@value #USER_COLLECTION_NAME} collection.</p>
   */
  @Override
  public boolean createUser(User user) throws IAMException { ... }
  ```

- Use `{@value #CONSTANT_NAME}` to inline the actual string value of a class-level constant into Javadoc
- Private helpers also get Javadoc

---

## Tests

### Test Setup

Use `@ExtendWith(MockitoExtension.class)` with `@Mock` and `@InjectMocks`. Add `reset(mock1, mock2)` in `@BeforeEach` for DAO tests:

```java
@ExtendWith(MockitoExtension.class)
public class UserMongoDaoTest {

    @Mock
    private MongoDBOperations mongoDBOperations;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @InjectMocks
    private UserMongoDao userMongoDao;

    @BeforeEach
    void setUp() {
        reset(mongoDBOperations, mongoCollection);
    }
}
```

### Example test names
`createUser_Success`, `createUser_Failure_DuplicateKey`, `findByUsername_ValidUsername_ReturnsUser`, `findByUsername_NotFound_ReturnsNull`, `createUser_MongoException_ThrowsIAMException`, `revokeToken_ValidToken_Success`

Always test `MongoException` propagation: mock throw → assert `IAMException`. Test naming and AAA: see root `CLAUDE.md`.
