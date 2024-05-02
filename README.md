# mutable-var
This library introduces mutable variables into the Clojure programming language, providing a way to declare and mutate variables within a scoped block.

## Foreword
Clojure is a language that strongly favors immutability as a core principle for building robust, concurrent, and predictable software. This library provides mutable variable capabilities as a specialized tool for scenarios where mutable state is necessary or significantly simplifies certain kinds of operations. **It should be used judiciously and with a thorough understanding of the implications**. Frequent use of mutable variables can lead to code that contradicts the foundational design principles of Clojure and may result in software that is harder to reason about and maintain. Ensure you understand the necessity and implications of introducing mutable state into your application before using this library.

## Installation
To include `mutable-var` in your project, add the dependency to your project configuration file depending on the build tool you are using:

### Leiningen
Add the following to your `project.clj` file:
```clj
[com.xadecimal/mutable-var "0.1.0"]
```

### Clojure CLI/deps.edn
Add the following to your `deps.edn`:
```clj
{:deps {com.xadecimal/mutable-var {:mvn/version "0.1.0"}}}
```

## Usage
You can define a variable scope using `var-scope`, and within this scope, you can declare and initialize mutable variables using `(var <name> <value>)`. The variable is accessible from the point of declaration to the end of the `var-scope`. To mutate the variable, use `(set! <name> <new-value>)`.

Example:
```clj
(ns example
  (:require [com.xadecimal.mutable-var :refer [var-scope]]))

(var-scope
  (var name (read-line))
  (println "Hello" name)
  (set! name "Bob")
  (println "Hello" name))
```

## Semantics
Mutable variables adhere to the following rules:
- Variables are scoped to their block, meaning they exist only within the `var-scope` where they are defined.
- Variables can shadow each other. An inner block can declare a variable with the same name as one in an outer block, which temporarily hides the outer variable.
- You cannot use a variable before it has been declared within the same scope.
- Redefining the same variable within the same scope is not allowed. Instead, you must mutate its value using `(set! <name> <new-value>)`.
- When a variable is inferred as primitive, its type cannot be changed through mutation. If a variable is declared as a `long`, only `long` values can be assigned to it.
- Inner block scopes have access to all variables declared in outer blocks and can modify their values.

## Features
- **Scoped Variable Declarations:** Encapsulate variables within a `var-scope`, enhancing control over variable lifecycles and state management.
- **Mutation Operations:** Use `set!` for explicit mutations, maintaining clarity in state changes within the block.
- **Type Safety:** Enforces type consistency in variables, preventing runtime type errors and ensuring more predictable behavior.
- **Error Handling:** Attempts to violate the rules (like redefining a variable or using a variable before declaration) will throw clear and informative exceptions, aiding in debugging and development.

## Performance
This library is designed with performance in mind, by utilizing plain arrays of size 1 as the containers to manage mutable variables. This allows quick access and update operations, closely mimicking the behavior of mutable fields in other programming contexts. I can't promise it's as fast as mutable variables in Java, but it should get close.

### Implementation Details
- **Array-backed Variables**: Each mutable variable is implemented using a single-element array, providing a straightforward mechanism for mutable state management.
- **Primitive Arrays for Primitive Types**: When a variable is declared with a primitive type, either through type inference or explicit type hints, the library uses corresponding primitive arrays (e.g., `long[]`, `double[]`). This use of primitive arrays eliminates the need for boxing and unboxing operations, thereby optimizing performance.

## Best Practices
- **Use Scoped Variables Thoughtfully:** While mutable variables provide flexibility, use them judiciously to maintain clean and manageable codebases.
- **Prevent Shadowing Confusion:** Be cautious with variable shadowing as it can lead to confusion. Clear naming conventions can help manage this.
- **Leverage Type Safety:** Always initialize mutable variables with the intended type and adhere to that type throughout the scope.

Here's a restructured section focusing on the support for primitive types within `mutable-var`, explaining when type inference might be insufficient, and how to apply explicit type hints:

## Primitive Types
The `mutable-var` library supports primitive types, enhancing performance by allowing direct operations on these types without the overhead of boxing and unboxing. Clojure's type inference is robust and often suffices for determining the appropriate primitive type. However, there are scenarios where manual intervention via explicit type hints is necessary to ensure optimal performance and correctness.

### Automatic Type Inference
In most cases, `mutable-var` can automatically infer the primitive type based on the value assigned during variable initialization:

```clj
(var-scope
  (var i 42)  ; Automatically inferred as a long
  (var f 3.14)  ; Automatically inferred as a double
  (set! i (+ i 1)))
```

### When to Use Explicit Type Hints
Explicit type hints are necessary when:

- **Ambiguous type origin**: When a variable's initial value comes from a function whose return type is not clearly defined through type hints, leading to potential ambiguity.
- **Specific type requirements**: When the intended use of a variable requires a type different from what would be inferred, such as needing an Object type to allow for nil assignments, despite the initial value suggesting a primitive type.

### Applying Explicit Type Hints
Explicit type hints can be provided either by annotating the initializing expression or by using casting to enforce the variable to be treated as a desired type or by wrapping in an identity when you want Object type:

```clj
(var-scope
  (var i ^long (#(+ 1 2)))  ; Type hinting the function return
  (var j (int (#(+ 1 2))))  ; Casting to int, even though function returns a long
  (var k (identity 42))  ; Ensuring variable can hold nil values by typing it to Object
  (set! k nil))
```

### Caveats
- **Avoid hinting the variable name directly in the `var` declaration**. Always hint the value or use a casting function.
- **Immutable type post-declaration**: Once a variable's type is declared or inferred, attempting to `set!` a value of a different type will result in a runtime error.

### Understanding Type Errors
If a variable of a primitive type is attempted to be mutated with a different type, it will lead to a runtime error, which will often appear as `No matching method aset found taking 3 args`. This might mean you have type mismatches.
