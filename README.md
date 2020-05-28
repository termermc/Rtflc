# Rtflc
Robust Rtfl runtime and compiler

## Why?
Since the creation of Rtfl, its speed has been a major drag on usefulness and usability. Due to this fact, I decided to write a new Rtfl runtime, fulfilling the following requirements:
 - Speed
 - Robustness
 - Improved debugging

## How does Rtflc fulfill these requirements
To understand how Rtflc is an improvement on the original Rtfl interpreter, we must first understand how the original Rtfl interpreter worked. The original interpreter did not split up its execution and parsing mechanisms, instead executing each line as it parsed it. The effect of this is that execution was throttled to the speed of parsing, which is [not very good](https://github.com/termermc/rtfl/blob/c6d785a39353af4f1779d86064d307027d5bd078/src/net/termer/rtfl/expressions/Expressions.java#L50).

Rtflc, on the other hand, splits up parsing and execution into different components. The parsing component takes in source code and produces intermediate instructions which it then feeds to the executor. Since the parser can parse multiple lines into instructions and runs faster than the original interpreter, it allow the executor to work much faster. Loops are also faster, since instead of re-parsing the loop body, the entire body is translated into instructions that the executor can re-use. 

## Rtfl bytecode
Like many previously interpreted languages have done, Rtfl now utilizes bytecode, which is effectively a high level implementation of basic instructions. Interpreting source code is far more expensive than executing bytecode, and as such, Rtflc can compile Rtfl source code into bytecode binaries that can be executed by Rtflc.

## Getting it
You can either download Rtflc from the [releases](https://github.com/termermc/rtflc/releases) tab, or you can compile it.
To compile, you need to run either `gradlew.bat shadowJar` (on Windows) or `./gradlew shadowJar` (on Mac, Linux, Unix) in the source code root, and a file named `Rtflc-X.X-all.jar` will be created in the `build/libs/` directory.

## Embedding Rtfl
To embed Rtfl, you will need to include the jar, either via Maven/Gradle, or by manually adding it to your project.
To use with Maven:
```xml
<dependency>
    <groupId>net.termer.rtflc</groupId>
    <artifactId>Rtflc</artifactId>
    <version>1.2</version>
</dependency>
```
To use with Gradle:
```groovy
dependencies {
    implementation 'net.termer.rtflc:Rtflc:1.2'
}
```

Using Rtflc in your project is as simple as adding the following code to your project:
```java
RtflRuntime runtime new RtflRuntime();
```
From there, you can import that standard library with
```java
runtime.importStandard();
```
import Java interop functions with
```java
runtime.importJavaInterop();
```
To expose a Java method to Rtfl, it's as simple as the following:
```java
// Some object
String str = "Hello world";

runtime.exposeMethod(/* The object that owns the method */ str, /* The method to expose */ "substring", /* The method's arguments */ new Class<?>[] {int.class});
```
After that, it would be possible to execute `println(substring(2))` and it would output `llo world`!

## Usage
To see usage instructions, execute the Rtflc jar with the `-h` or `--help` options.

## Language documentation
TODO, there's been some large updates. The last version of Rtfl was 1.3, this runtime supports 1.3 and onward. To see which versions Rtflc supports, execute the jar with the `-v` or `--version` options.
