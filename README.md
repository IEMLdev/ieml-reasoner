# IEML reasoner in Java

This project aims at building a reasoner for the [IEML language](https://pierrelevyblog.com/tag/ieml/).
IEML is a computable meaning-representation system based on semantics primitives.
It aims at combining the expressivity of natural languages (NL) with the computability of unambiguous formalisms.

This project of reasoner has two main purposes: help extending IEML by exploiting its regularities to compute new structures, and ensure coherence of IEML with itself by exhaustively confront existing elements.

## Dependencies

### Code

The projects depends on the [IEML main system project](https://github.com/IEMLdev/ieml) (in a git submodule), an [analogical reasoner](https://github.com/vletard/analogy-java) (in a git submodule), and a [Java JSON parser](https://github.com/stleary/JSON-java) (as external library).

Git submodule dependencies are initialized with `git submodule update --init`

External libraries are automatically retrieved if necessary at compilation time.

[Apache Ant](https://ant.apache.org/) is also helpful for simple compilation and running of the system, but not mandatory.

### Resources

The reasoner also requires a JSON extraction of the IEML database.
The files are already packed in the `resources/` directory, they can be updated when necessary by using:
`python3 usl_extract.py` (after git submodule initialization).


## Prototype

Once dependencies have been retrieved and resources generated, the prototype can be run simply using `ant run`

The system loads the dictionary and the word structures from the database (previously gathered by `usl_extract.py`).
The available features are: finding IEML polysemy, searching the base for proportions and building equations from the base to attempt generating new words.

### Polysemy issues

The polysemy procedure consists in searching for expressions in a given natural language that are used to describe several IEML words.
As IEML aims to define a bijection between meanings and USLs, a polysemy may be a sign of inconsistency.
In many cases, it may also indicate a mere homonymy (homographs) in the considered natural language.
In the situation of NL homographs, the translation of the IEML words in this NL should be further specified (e.g. from "translation" to "translation (geometry)").

### Internal coherence checking

The internal coherence checking is done by constituting every valid (and relevant) analogical proportion over the syntactical structure of four words of the database.
The syntax and semantics of the IEML language are bijective, thus every analogical proportion over the syntactical structure of IEML words also holds as an analogical proportion over their meanings.
Finding non sound analogical proportion then helps in structure choices for making words or other IEML units.

### Assisted database extension

Making analogical equations out of combinations of three words of the database leads to the generation of solution words.
Those among them that do not already belong to the database are candidates to new additions following the same logical structuring as existing words.
Additionally, finding a meaningless solution to an equation can help for internal coherence checking (see above).
Note that for automatically generated new words, absence of meaning should not be confused with unused concept (no words in some NL to express it).
