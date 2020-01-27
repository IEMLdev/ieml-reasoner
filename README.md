# IEML reasoner in Java

This project aims at building a reasoner for the [IEML language](https://pierrelevyblog.com/tag/ieml/).
IEML is a computable meaning-representation system based on semantics primitives.
It aims at combining the expressivity of natural languages with the computability of unambiguous formalisms.

This project of reasoner has two main purposes: help extending IEML by exploiting its regularities to compute new structures, and ensure coherence of IEML with itself by exhaustively confront existing elements.

## Dependencies

### Code

The projects depends on the [IEML main system project](https://github.com/IEMLdev/ieml) (in a git submodule), an [analogical reasoner](https://github.com/vletard/analogy-java) (in a git submodule), and a [Java JSON parser](https://github.com/stleary/JSON-java) (as external library).

Git submodule dependencies are initialized with `git submodule update --init`

External libraries are automatically retrieved if necessary at compilation time.

[Apache Ant](https://ant.apache.org/) is also helpful for simple compilation and running of the system, but not mandatory.

### Resources

The reasoner also requires a JSON extraction of the IEML database.
This needs to be done once using `python3 usl_extract.py` (after git submodule initialization).


## Prototype

The prototype can be run simply using `ant run`

