## User guide - Code generation

This part of the user guide provides information on code generation of beans.
See the [main user guide](userguide.html) for a higher level introduction.


## Code generator

It is possible to write a Joda-Bean by hand - they are just normal Java interfaces defining an API for beans.
There are some base classes that assist as well as some standard map-based implementations, notably
[FlexiBean](apidocs/org/joda/beans/impl/flexi/FlexiBean.html).
It is more common to use the code generator, however the generator is optional.

A code generated Joda-Bean minimises the amount of code that the developer has to write.
The developer simply creates an outline class and adds the properties (fields).
The code generator then produces all the getters and setters, plus additional helper methods.

Here is an example `UserAccount` class showing the parts that a developer has to write:

```
@BeanDefinition
public final class UserAccount implements Bean {

    /** The user identifier. */
    @PropertyDefinition(validate = "notEmpty")
    private String userId;

    /** The email address. */
    @PropertyDefinition(validate = "notEmpty")
    private String emailAddress;

    /** The number of logins. */
    @PropertyDefinition
    private int numberLogins;

    /** The last login instant. */
    @PropertyDefinition(validate = "notNull")
    private Date lastLogin;
}
```

The code generator will create getters, setters, equals, hashCode, toString, metaBean and properties.
To see the generated code for this class,
[click here](https://github.com/JodaOrg/joda-beans/blob/v1.4/src/test/java/org/joda/beans/gen/UserAccount.java#L34).


## Using the code generator

The code generator must be invoked before compilation as it generates Java source code.
It cannot operate at runtime, as the getters and setters that are generated must be available for developers
writing the main part of the application.

The code generator operates by creating a portion of the source file marked by "AUTOGENERATED START" and "AUTOGENERATED END" tags
(when first run, if the tags are not present, it will create the block at the end of the file).
The generator only ever changes code within the marked block, apart from imports which it inserts if it determines they are necessary.
Limiting the generation to a fixed part of the file means that developers can customise the rest of the Java file in any
way that is desired, such as adding constructors and methods.

The generator has the following requirements:

* The class must be annotated with [BeanDefinition](apidocs/org/joda/beans/BeanDefinition.html)
* The class must implement the [Bean](apidocs/org/joda/beans/Bean.html) interface, or extend a class that does.
At one time it was mandatory to subclass [DirectBean](apidocs/org/joda/beans/impl/direct/DirectBean.html) but that is no longer recommended
* The class must be a normal top-level class, nested/inner classes and multiple classes per file are not supported
* Each property field must be annotated with [PropertyDefinition](apidocs/org/joda/beans/PropertyDefinition.html)
* Each property field must be be private
* The Javadoc of each property field should normally start with the word "The".

The command line takes the following arguments:

```
  Usage java org.joda.beans.gen.BeanCodeGen [file]
  Options
    -R                process all files recursively, default false
    -indent=tab       use a tab for indenting, default 4 spaces
    -indent=[n]       use n spaces for indenting, default 4
    -prefix=[p]       field prefix of p should be removed, no default
    -verbose=[v]      output logging with verbosity from 0 to 3, default 1
    -nowrite          output messages rather than writing, default is to write
```

The prefix is useful for teams that have a coding standard where fields must be prefixed, such as by an underscore or a letter.
Specifying it to the generator allows that prefix to be stripped before generating the property name.

The code generator works by reading the source file and parsing it to memory.
The parser is deliberately simplistic and will reject complex files or unusual code formatting styles.
The generator processes the annotated properties and replaces the auto-generated section of the file in memory.
It then compares the newly generated file with the original and only if they are different is the new file written to the file system.
The generator will make use of property comments and will respect and propagate deprecated annotations.
It is reasonably intelligent - final fields will become read-only properties, most collections and maps are sensibly handled
and booleans generate "is" methods rather than "get" methods. Simple generics are also handled.

The generator must be invoked every time that the source file is changed.
Failure to do so (such as by using an IDE refactoring) could leave the auto-generated block in an invalid state.
Normally the generator will be invoked from the IDE or a tool like Apache Ant.

The code generator only parses and outputs based on a single source Java file.
It does not require the file to compile and never knows that "String" actually means "java.lang.String".
In other words, the type system in the generator is totally dumb and based solely on the short simple class name.

A [Maven plugin](https://github.com/JodaOrg/joda-beans-maven-plugin) is available to generate the beans.


## Customisation

The generator has a limited set of customisations to the core part of the generation.

The `PropertyDefinition` has attributes that allow the getter and setter generation to be controlled.
The main use case is to prevent generation of the getter by specifying "manual".
The developer can then write their own method with whatever special logic is required.

The `PropertyDefinition` also has an attribute to specify validation.
The standard values are "notNull" and "notEmpty", which are implemented in `JodaBeanUtils`.
Developers may specify any text in the attribute however, and it is simply inserted into the output.
Thus a validation attribute of "Validate.noNulls" could be used to connect up to Commons Lang.


## Derived properties

It is possible to declare a property without a matching field.
For example, an "age" property could be derived from a "dateOfBirth" field.

To do this, annotate the getter of the derived property with [DerivedProperty](apidocs/org/joda/beans/DerivedProperty.html).
Apart from the absence of a field, a derived property is very similar to a normal read-only property.


## Immutable beans

Code generated beans may be immutable.

All fields in an immutable bean must be final.
It is recommended that immutable beans are final,
do not extend any other bean class and directly implement `ImmutableBean`.

```
 @BeanDefinition
 public final class Foo implements ImmutableBean {
   // code generated immutable bean with public builder
 }
```


### Immutable bean customisation

Code generated immutable beans can be customized as follows.

An immutable bean can be configured to provide additional cross-property validation.
In most cases the per-property validation attribute is sufficient.
When cross-property validation is needed, this technique can be used.
Simply declare a private void method taking no arguments annotated with '@ImmutableValidator'.

```
 @ImmutableValidator
 private void validate() {
   // validate the instance variables of the bean
 }
```

An immutable bean can be configured to apply default property values.
In most cases this is not necessary, but if the bean has lots of non-null properties it may be
desirable to have some default values.
Simply declare a private static void method taking one 'Builder' argument annotated with '@ImmutableDefaults'.

```
 @ImmutableDefaults
 private static void applyDefaults(Builder builder) {
   // set default property values directly into the builder
 }
```

An immutable bean can be configured to cache the hash code.
In most cases this is not necessary, but if the bean is used as a hash key, then it may be helpful.
Simply set the boolean 'cacheHashCode' flag of '@BeanDefinition' to true.

```
 @BeanDefinition(cacheHashCode = true)
 public final class Foo implements ImmutableBean {
   // code generated immutable bean with cached hash code
 }
```


## Immutable bean hierarchies

It is possible for an immutable bean to be non-final.
Any subclass should logically also be immutable, but must set the
`BeanDefinition` annotation value `hierarchy` to "immutable":

```
 // superclass
 @BeanDefinition
 public abstract class SuperFoo implements ImmutableBean {
   // code generation will enable an immutable subclass
 }
 
 // subclass
 @BeanDefinition(hierarchy = "immutable")
 public final class Foo extends SuperFoo {
   // code generation will connect to superclass
   // note that there is NO 'implements ImmutableBean' on the subclass
 }
```

Immutable beans do not have set methods.
Instead, a public builder class will be generated with methods to setup an instance.
This supports code based manipulation, using the `toBuilder()` method.
The scope of the builder may be controlled in the bean definition:

```
 @BeanDefinition(builderScope = "private")
 public final class Bar implements ImmutableBean {
   // code generated immutable bean with private builder
 }
```

It is also possible for a bean to be partially final.
In this case, the bean will implement `Bean` rather than `ImmutableBean`.
To get the correct behaviour, the `BeanDefinition` annotation value
`builderScope` must be set to "public".
Partially final beans can extend one another, but must all declare the builder scope.

The rules as to what works and what does not are complex.
It is recommended to keep is simple, and only have normal mutable beans or final immutable beans.


## Links

Return to the [main user guide](userguide.html).