# codegen/ast

Bare-minimum AST for codegen output. There are endless ways to model an AST; this one picks what is simple and cheap and covers only what the generator needs today.

## Building blocks

`AstLiteral` and `AstStringLiteral` are the escape hatches and are always needed. Everything else can be expressed with them.

Open idea: distill every other node into these two, so the printer only has to know them.

## Names as expressions

A name that is later used as an expression (a param or variable referenced in a call or assignment) is typed `AstExpression`, even when it is really just an identifier. The generator declares it once as a node, e.g. `val item = AstLiteral("item")`, and reuses that node everywhere, with no `AstLiteral(...)` wrapping at use sites.

## Deliberate shortcuts

The model is intentionally imprecise. Fix these only when there is a real need:

- `AstAssignment` is really an operator. A fuller model would have `AstUnaryOperator`, `AstBinaryOperator`, etc.
- `VariableType` describes a declaration, not an assignment.
