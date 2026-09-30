# codegen/ast

Bare-minimum AST for codegen output. There are endless ways to model an AST; this one picks what is simple and cheap and covers only what the generator needs today.

## Building blocks

`AstLiteral` and `AstStringLiteral` are the escape hatches and are always needed. Everything else can be expressed with them.

Open idea: distill every other node into these two, so the printer only has to know them.

## Deliberate shortcuts

The model is intentionally imprecise. Fix these only when there is a real need:

- `AstAssignment` is really an operator. A fuller model would have `AstUnaryOperator`, `AstBinaryOperator`, etc.
- `VariableType` describes a declaration, not an assignment.
