# Sturdy

<img align="left" alt="Sturdy Logo" src="logo.jpg" width="25%">

Sturdy is a modular static analysis platform based on big-step abstract interpretation, implemented in Scala. While a concrete interpreter runs a program to produce its result, an abstract interpreter "simulates" the execution of the program to produce _all possible results_. Such abstract interpreters can be used to find bugs, validate security and other properties, or to enable compiler optimizations. In Sturdy, we use big-step abstract interpretation, which means interpreters are standard recursive functions. Sturdy is agnostic to the analyzed language, currently we provide analyses for the following languages:

- [WebAssembly](https://webassembly.org/), a binary instruction format for a stack-based virtual machine.
- [Tip](https://cs.au.dk/~amoeller/spa/), a tiny imperative language used to explain static analyses.
- The previous [Haskell implementation of Sturdy](https://gitlab.rlp.net/plmz/sturdy) also supported Scheme, Stratego, and LambdaJS.



### Modular static analysis platform

The key feature of Sturdy is the modular description of analyses, which are easy to write and refine. Sturdy achieves modular analysis descriptions through a number of design decisions:

- **Generic interpreter**: All static analyses share the same core behavior, which models the control-flow and data-flow of the analyzed language. For each language, we implement its core behavior once and for all in what we call a "generic interpreter". The generic interpreter is the superclass of all analyses of that language.
- **Value and effect interfaces**: The handling of values (e.g., integers) and effects (e.g., the heap) is analysis-specific and thus cannot be implemented by the generic interpreter. Instead, the generic interpreter is written against value and effect interfaces. While value interfaces provide pure operations on values (e.g., addition on integers), effect interfaces provide effectul operations on effects (e.g., read and write to the heap). Different implementations of these interfaces yield different static analyses.
- **Value and effect components**: To obtain a static analysis in Sturdy, we must implement the value and effect interfaces used by the generic interpreter. We call implementations of these interfaces value and effect components. Value and effect components are composable and their implementations are exchangeable. Composability means we can combine any number of values and effects and use them together. Exchangeability means we can replace the implementation of one component without changing other components. This way, Sturdy supports the gradual development and refinement of static analyses.
- **Configurable fixpoint algorithm**: Sturdy provides a language-independent fixpoint algorithm that is highly configurable. For example, analysis developers can select a context-sensitivity, activate loop unrolling, and choose between different iteration strategies (e.g., nested loop vs. outermost loop first). Furthermore, analysis developers can piggyback on the fixpoint algorithm to track analysis results of individual program fragments. For example, we can log a control-flow or data-flow graph, or we can collect expressions that yield constant results (and thus can be optimized).



### Soundness testing

A static analysis is sound if it predicts all possible results of a program. Only sound analyses provide safe results that, for example, can be used to trigger compiler optimizations. The design of Sturdy is based on a formal theory for compositional soundness. While only the [original implementation of Sturdy in Haskell](https://gitlab.rlp.net/plmz/sturdy) provides formal guarantees, the current implementation in Scala still benefits from that same design in multiple ways:

- **Concrete interpreter**: The soundness of an analysis is relative to the actual program behavior. Analysis developers can obtain a concrete interpreter for the analyzed language with little effort by instantiating the generic interpreter with value and effect components that implement the canonical concrete semantics. The concrete interpreter can also be tested against a reference semantics of the analyzed language to ensure the generic interpreter is correct (as we have done for WebAssembly).
- **Modular soundness propositions**: We must define what it means for an analysis to be sound. In Sturdy, these soundness propositions are defined separately for each value and effect component. Value components implement an abstraction function that lifts the canonical concrete value representation into the abstract domain. Effect components implement a soundness proposition that relates the internal state of the canonical effect implementation to their own internal state. An analysis then simply composes the soundness propositions of its components.
- **Soundness testing**: With these ingredients we can test the soundness of analyses against the concrete interpreter. Specifically, for each test program, we run the analysis and the concrete interpreter and compare them using the composed soundness proposition of the analysis. Importantly, this not only validates that the program's resulting value (e.g., the result of the `main` function) is soundly approximated, but also validates that the program's effects (e.g., values stored in the heap) are soundly approximated.



### Getting Started

Build the complete project using `sbt compile`, test the complete project with `sbt test`. Use `sturdy_wasm / test` to run tests of individual languages  (here `wasm`).



### Publications

**Combinator-Based Fixpoint Algorithms for Big-Step Abstract Interpreters**  
Sven Keidel, Sebastian Erdweg, Tobias Hombücher  
International Conference on Functional Programming (ICFP). ACM, 2023. [[pdf](https://svenkeidel.de/assets/papers/fixpoint_combinators.pdf)]

**Modular Abstract Definitional Interpreters for WebAssembly**  
Katharina Brandl, Sebastian Erdweg, Sven Keidel, Nils Hansen  
European Conference on Object-Oriented Programming (ECOOP). ACM, 2023. [[pdf](https://svenkeidel.de/assets/papers/sturdy_wasm.pdf)]

**A Systematic Approach to Abstract Interpretation of Program Transformations**  
Sven Keidel and Sebastian Erdweg.  
Verification, Model Checking, and Abstract Interpretation (VMCAI). Springer, 2020. [[pdf](https://svenkeidel.de/assets/papers/program-trans-analysis.pdf)]

**Sound and Reusable Components for Abstract Interpretation**  
Sven Keidel and Sebastian Erdweg.  
_Object-Oriented Programming, Systems, Languages, and Applications (OOPSLA)_. ACM, 2019 [[pdf](https://doi.org/10.1145/3360602)] [[Talk](https://youtu.be/uCM54R3ab-Q)]

**Compositional Soundness Proofs of Abstract Interpreters**  
Sven Keidel, Casper Bach Poulsen and Sebastian Erdweg.  
_International Conference on Functional Programming (ICFP)_. ACM, 2018 [[pdf](https://doi.org/10.1145/3236767)] [[Talk](https://www.youtube.com/watch?v=zOqSlHAMGt4)]

