# Parsing xml for fun and profit!

This repo contains some code in different languages and styles to solve a simple problem: Parsing a (vastly simplified) form of XML. It's more about fun and exploration than actual usability. Contained are at the moment:

- The first approach that comes to mind but doesn't really scale (Sketched in Java)
- A simple, ad-hoc parser based on explicitly consuming the stream, without dependencies (Go)
- A combinator based parser in Haskell, based on [https://charlieharvey.org.uk/page/naive_xml_parser_with_haskell_parsec_and_perl_regexen_part_one_haskell](https://charlieharvey.org.uk/page/naive_xml_parser_with_haskell_parsec_and_perl_regexen_part_one_haskell) (Haskell)
- A EBNF Grammer based parser writte with [https://github.com/Engelberg/instaparse](instaparse) (Clojure)
- A "chemical computing" parser, where fragments form "molecules" and react with each other (Clojure, incomplete)
- A deep net which learns to transform xml to hiccups syntax, based on the [https://blog.keras.io/a-ten-minute-introduction-to-sequence-to-sequence-learning-in-keras.html](Keras blog) (Python)
- A spec based generator for the hiccups/html training data (Clojure)

Have fun, and if you have any contributions, PRs welcome!
