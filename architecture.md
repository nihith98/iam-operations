# iam-operations — Module Architecture

> For coding style rules, see `CLAUDE.md` in this directory.

---

## Module Role

This is the **IAM infrastructure and shared library** — the foundation of the authentication repo. It owns two distinct responsibilities:

**Shared library (used by all modules):**
- Domain model POJOs (`model/`) — `User`, `Token`, `Role`
- Response wrapper types (`response/`)
- IAM service **interfaces** (`interfaces/`) — implementations live in `dao/`
- Shared constants (`constants/`)
- Utility classes (`util/`)
- Custom exception (`exception/IAMException`)

**Infrastructure (implementations):**
- `MongoDBConnection` — manages `MongoClient` lifecycle
- `MongoDBOperations` — thin wrapper around native MongoDB Java driver CRUD operations
- DAO classes (`dao/`) — two implementations of `UserIAMService`, one of `TokenIAMService`
- `UserQueryBuilder` (`query/`) — static factory for `BasicDBObject` query filters

This module uses the **native synchronous MongoDB Java driver** directly for token and role storage. There are **no Spring Data MongoDB repositories**. No `@Document` or `@Field` annotations anywhere.

For user identity, `KratosUserIAMService` delegates to **Ory Kratos** via its REST API. Token and role storage always uses MongoDB regardless of which `UserIAMService` implementation is active.

---

## Package Structure

```
com.nihith.iam
├── constants/
│   ├── AuthMessageConstants.java       ← human-readable message strings
│   ├── IAMFieldNameConstants.java      ← MongoDB field name strings
│   └── AuthOperation.java             ← enum for CRUD operation names
├── connection/
│   ├── MongoDBConnection.java          ← @PostConstruct init, @PreDestroy close
│   └── MongoDBOperations.java          ← CRUD primitives (insert, fetch, delete)
├── dao/
│   ├── UserMongoDao.java               ← implements UserIAMService (MongoDB backend)
│   ├── KratosUserIAMService.java       ← implements UserIAMService (Ory Kratos backend)
│   └── TokenMongoDao.java              ← implements TokenIAMService (always MongoDB)
├── exception/
│   └── IAMException.java
├── interfaces/
│   ├── UserIAMService.java             ← interface only
│   └── TokenIAMService.java            ← interface only
├── model/
│   ├── User.java
│   ├── Token.java
│   └── Role.java
├── query/
│   └── UserQueryBuilder.java           ← static filter factory, no Spring
├── response/
│   ├── AuthResponseStructure.java
│   ├── AuthResponseMessages.java
│   ├── AuthResponseStatus.java         ← enum
│   └── AuthMessageType.java            ← enum
└── util/
    ├── EnvironmentUtil.java
    ├── ObjectMapperUtil.java
    └── AuthResponseStructureUtil.java
```
