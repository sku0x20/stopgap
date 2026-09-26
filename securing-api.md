## Summary

This proposal deals with making the apis secure by means of authentication. reference as AuthN hereafter.
Authorization is not of concern of this proposal.

Apis for AuthN can be secured at in a framework at multiple points via multiple ways.
this proposal establishes a opinionated way to achieve so, and thereby making the apis follow the established flow.

## Problem

Each api method/function can take the responsibility to validate the AuthN of the caller.
but doing so violates the DRY principle. Same code is repeated everywhere even if its a single call.
its penetrates everywhere.

For AuthN it shouldn't be like this. There should be a way control,
api surfaces at multiple levels.

- Method level
- Class level
- Global level

## Authorization

Authorization also called AuthZ, is much more finer than AuthN, endpoint need to know if caller/user have access on
particular resource. and rehouse can vary call to call.
Also, industry has convulteed the meaning of AuthZ and AuthN. Going Role Based or RBAC, would convolute this simple
framework also.

Also, RBAC scales very badly and it hinders the developers from thinking in a proper direction.
AuthZ can even vary service to service whereas same AuthN can server multiple services.
thats why we see growing number of gateway projects which offers this AuthN.

## Solution

Rather than providing a plethora of service we will do something like Json, idea is to do as little work as possible.
Having worked with Spring Security and Quarkus. The security api here have to way more straightforward and easy to use.
minimal overhead and easy to understand.

to do any reliable validation, each endpoint method would need a principle.
so developer can put the kind of authN it wants for that particular method.
AuthNUser, AuthNMachine, etc.

By default, all endpoints will be protected. To make endpoint public, developer can put AuthPublic.
Again all these are developer implementations.

One Question arises, how its works with http authorization header, can it support multiple types,
can it support different header? how will it support same header but different types of users?

so idea is similar to serde,

```kotlin
interface AuthenticationResolver {
  fun authenticate(request): Authentication
}

interface Authentication<T> {
  val principle: T
}

class BadAuthNException : HttpException(
  "Unauthorized",
  Status.Unauthorized_401
)

```

But unlike,
json serde catalog which can be per endpoint or per method level.
that is not supported here.
to support per method level, AuthenticationResolver have to run in endpoint like serde by doing so it cannot use helidon
filter, which restricts the impl.
And there is not reason for it to be.

Also, as said, it gives developer the total control how its wants to structure things.
Like AuthenticationResolver Impl can switch on headers, etc.
one can have internal impl which are like UserResolver, or if want http idiomatic can like BasicResolver, etc.
User have the full control.
like same Bearer token dev can do switch and return UserAuthN or MachineAuthN based on the type inside the jwt token.

Another question is same endpoint but different kind of users.
as general rule of advice this is bad pattern.
since they are 2 different resources, authz will be there and there had to be switch somewhere.
create another endpoint to support these kind of features.
its not like it cannot work, its easy, issue UserAuthN for both.
but we advise against it. since OCP violation.

Can we restrict by ip and username, both combined?
yes, developer own the `AuthenticationResolver` it can implement it in such a way it validates both.

Also, there are property by which developer can configure against which auth it should do the validation if the method
doesn't contain the Authentication in its parameters.
By default, it's a throw, ie. 401.
By configuring it can allow NoAuth, AnyAuth, any variation of it. All in control by developer.
