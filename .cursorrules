# Android Development Cursor Rules

## General Guidelines

### Basic Principles

- **Language:** All code and documentation must be in English.
- **Type Declaration:** Always explicitly declare the type of each variable, function parameters, and return values. Avoid `Any`.
- **Conciseness:** Strive for concise code. Avoid unnecessary blank lines within a function.
- **Error Handling:** Use exceptions for unexpected errors. If an exception is caught, it must be to fix an expected problem or add context. Otherwise, use a global handler.
- **Immutability:** Prefer immutability for data. Use `val` for literals that don't change and `readonly` for data that doesn't change.
- **SOLID Principles:** Adhere strictly to SOLID principles for class design.
- **Composition over Inheritance:** Prefer composition over inheritance.
- **Interfaces:** Declare interfaces to define contracts.

### Naming Conventions

- **Classes:** Use `PascalCase` for classes and interfaces.
- **Variables, Functions, Methods:** Use `camelCase` for variables, functions, and methods.
- **Files and Directories:** Use `snake_case` for file and directory names (e.g., `user_profile_activity.xml`).
- **Constants:** Use `UPPER_SNAKE_CASE` for constants. Avoid "magic numbers" by defining them as constants.
- **Functions:** Start each function name with a verb.
  - For boolean functions: use `isX`, `hasX`, `canX`, etc. (e.g., `isLoading`, `hasError`, `canDelete`).
  - For functions that don't return anything: use `executeX`, `saveX`, etc.
- **Abbreviations:** Use complete words instead of abbreviations. Exceptions for standard abbreviations like API, URL, i, j (for loops), err (for errors), ctx (for contexts), req, res, next (for middleware function parameters).

### Functions

- **Single Purpose:** Write short functions (less than 20 instructions) with a single, clear purpose.
- **Naming:** Name functions with a verb and descriptive context.
- **Nesting:** Avoid nesting blocks by:
  - Performing early checks and returns.
  - Extracting logic into utility functions.
  - Utilizing higher-order functions (e.g., `map`, `filter`, `reduce`).
- **Lambda Functions:** Use arrow functions (lambdas) for simple functions (less than 3 instructions). Use named functions for more complex logic.
- **Default Parameters:** Use default parameter values instead of null checks.
- **Parameter Reduction (RO-RO):** Reduce function parameters by using objects to pass multiple parameters (e.g., a data class).
- **Return Objects:** Use an object (e.g., a data class or a sealed class for results) to return multiple results.
- **Abstraction:** Maintain a single level of abstraction within a function.

### Data

- **Data Classes:** Use `data class` for data models.
- **Encapsulation:** Don't abuse primitive types; encapsulate data in composite types.
- **Validation:** Prefer internal validation within classes rather than external data validations in functions.

### Classes

- **Size:** Write small classes with a single purpose (less than 200 instructions, less than 10 public methods, less than 10 properties).

## Android Specific Rules

### Project Structure (Clean Architecture / Modularization)

- **Architecture:** Implement clean architecture with distinct layers: `domain`, `data`, and `presentation` (or `ui`).
  - **`app` module:** The primary application module.
  - **`domain` module:** Contains business logic, use cases, and entities. Should be pure Kotlin and independent of Android frameworks.
    - `model/`: Data classes representing business entities.
    - `repository/`: Interfaces defining data operations.
    - `usecase/`: Business logic, orchestrating interactions between repositories and other domain components.
  - **`data` module:** Contains implementations of domain interfaces, data sources, and mappers.
    - `repository/impl/`: Implementations of domain repository interfaces.
    - `source/local/`: Local data sources (e.g., Room databases, SharedPreferences).
    - `source/remote/`: Remote data sources (e.g., Retrofit API clients).
    - `mapper/`: Classes to convert data between data layer models and domain models.
    - `dto/`: Data Transfer Objects for network communication.
  - **`presentation` (or `ui`) module:** Contains UI-related components (Activities, Fragments, ViewModels, Composable functions).
    - `ui/`: Subpackages for different screens or features (e.g., `ui.home`, `ui.login`).
      - Each feature package should contain its `Activity`/`Fragment`/`Composable`, `ViewModel`, and any related UI components.
    - `di/`: Dependency Injection modules (e.g., Hilt modules).
    - `util/`: UI-specific utility classes.

### Resource Naming

- `layout/`: `activity_name.xml`, `fragment_name.xml`, `item_name.xml`
- `values/`: `strings.xml`, `colors.xml`, `themes.xml`, `dimens.xml`, `styles.xml`
- `drawables/`: `ic_action_name.xml` (vector assets), `img_descriptive_name.png`

### Kotlin Specifics for Android

- **Null Safety:** Leverage Kotlin's null safety features (`?.`, `?:`, `!!`).
- **Coroutines and Flow:** Use Kotlin Coroutines for async operations and Flow for reactive data.
- **Extension Functions:** Use for adding functionality to existing Android classes.
- **Data Classes:** Use `data class` for models.
- **Higher-Order Functions:** Use for collection operations.
- **Dependency Injection:** Use Hilt.
- **`by viewModels()`:** Use delegate for ViewModels.
- **`StateFlow`/`SharedFlow`:** Prefer over LiveData for new development.
- **`ConstraintLayout`:** Use for optimized layouts.
- **`viewModelScope`:** Use for ViewModel coroutines.

### UI/UX

- **Material Design:** Follow Material Design 3 guidelines.
- **Responsiveness:** Design for various screen sizes.
- **Accessibility:** Implement proper accessibility.
- **Error Handling:** Clear UI error states.
- **Theming:** Use MaterialTheme.
- **Animations:** Follow proper animation patterns.

## Testing

### Testing Pyramid

- **Unit Tests:** ViewModels, UseCases, repositories, domain layer.
- **Integration Tests:** Component interactions.
- **End-to-End/UI Tests:** User flows and critical UI actions.

### Testing Practices

- **TDD:** Follow when applicable.
- **Test Data:** Use realistic test data.
- **Test Coverage:** Maintain good coverage.
- **CI Integration:** Automate test execution.

### Android Testing

- **Unit Tests:** Test ViewModels, UseCases, data layer, domain layer.
- **UI Tests:** Use Compose testing framework or Espresso.
- **Performance Testing:** Test across different conditions.
- **Security Testing:** Test for vulnerabilities.

## Required Libraries and Tools

### Core Libraries

- Kotlin
- Jetpack Compose
- Android Architecture Components
- Hilt
- Retrofit & OkHttp
- Moshi/Kotlinx Serialization
- Coil
- Firebase

### Testing Libraries

- JUnit 5
- MockK
- Espresso/Compose Testing
- Truth

### Development Tools

- KtLint
- Detekt
- Chucker (optional)

## Linting Configuration

### KtLint Configuration

```kotlin
ktlint {
    version.set("0.50.0")
    android.set(true)
    filter {
        exclude("**/generated/**")
    }
}
```

### Android Lint Configuration

```kotlin
android {
    lint {
        checkReleaseBuilds = true
        abortOnError = true
        lintConfig = file("${project.rootDir}/lint-baseline.xml")
        disable += "MissingTranslation"
        error += "LogNotTimber"
        warning += "NewApi"
    }
}
```
