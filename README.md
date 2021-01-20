# arbitup
Application that for a given exchange rates between a set of currencies finds the best arbitrage opportunity if it exists.

The source of the rates is: https://fx.priceonomics.com/v1/rates/.

It leverages Bellman-Ford algorithm for finding the shortest path from and to the same vertex (currency) and detecting an existence of a `negative cycle`. In order to use it some preprocessing of the edges weight (exchange rates) must be invloved.

All the above are explained here: https://www.thealgorists.com/Algo/ShortestPaths/Arbitrage.

## Running aplication

### Requirements

* UNIX-like operating system
* Working Internet connection
* Java JDK 14.0.0 or higher installed and available on search path
* curl installed and available on search path


On the first run it will download `sbt-launch.jar` file, publish an app as a jar to a local repo, download ammonite and run `Main.sc` script.

### Starting the application

Navigate to the project folder:

```bash
cd /path/to/project
```

Then simply execute a `run.sh` script from the main project directory.

```bash
./run.sh
```

Eventually, you should see something like this:

```bash
[...]
Compiling /path/to/project/arbitup/Main.sc
12:43:12,247 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback-test.xml]
12:43:12,247 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback.groovy]
12:43:12,247 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Found resource [logback.xml] at [jar:file:/Users/eltherion/.ivy2/local/pl.datart/arbitup_2.13/1.0.0/jars/arbitup_2.13.jar!/logback.xml]
12:43:12,248 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs multiple times on the classpath.
12:43:12,248 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [jar:file:/Users/eltherion/.ivy2/local/pl.datart/arbitup_2.13/1.0.0/jars/arbitup_2.13.jar!/logback.xml]
12:43:12,248 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [jar:file:/Users/eltherion/.ivy2/local/pl.datart/arbitup_2.13/1.0.0/srcs/arbitup_2.13-sources.jar!/logback.xml]
12:43:12,263 |-INFO in ch.qos.logback.core.joran.spi.ConfigurationWatchList@3d98729a - URL [jar:file:/Users/eltherion/.ivy2/local/pl.datart/arbitup_2.13/1.0.0/jars/arbitup_2.13.jar!/logback.xml] is not of type file
12:43:12,306 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - debug attribute not set
12:43:12,306 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.ConsoleAppender]
12:43:12,310 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [STDOUT]
12:43:12,316 |-INFO in ch.qos.logback.core.joran.action.NestedComplexPropertyIA - Assuming default type [ch.qos.logback.classic.encoder.PatternLayoutEncoder] for [encoder] property
12:43:12,571 |-INFO in ch.qos.logback.classic.joran.action.RootLoggerAction - Setting level of ROOT logger to INFO
12:43:12,574 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [STDOUT] to Logger[ROOT]
12:43:12,575 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [pl.datart.arbitup] to INFO
12:43:12,575 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - End of configuration.
12:43:12,575 |-INFO in ch.qos.logback.classic.joran.JoranConfigurator@2375a976 - Registering current configuration as safe fallback point

[2021-01-20 12:43:13,109] [INFO ] [main] [pl.datart.arbitup.flow.ArbitrageImpl] [] Fetching exchange rates...
[2021-01-20 12:43:19,013] [INFO ] [ForkJoinPool-1-worker-3] [pl.datart.arbitup.flow.ArbitrageImpl] [] Exchange rates fetched.
[2021-01-20 12:43:19,024] [INFO ] [ForkJoinPool-1-worker-3] [pl.datart.arbitup.flow.ArbitrageImpl] [] Searching for opportunities...
[2021-01-20 12:43:19,154] [INFO ] [scala-execution-context-global-17] [pl.datart.arbitup.flow.ArbitrageImpl] [] Best found opportunity:
Cycle: Currency(BTC) -> Currency(JPY) -> Currency(EUR) -> Currency(BTC),
rates: 12451.625 -> 0.0086497 -> 0.0111361,
multiplier: 1.1993895
```

### Starting tests

To run tests navigate to the project folder and execute (*after* executing `run.sh` so you have `sbt-launch.jar` downloaded):

```bash
cd /path/to/project
./sbt ";clean;test;it:test"
```

### Coverage report

To run tests and get coverage report navigate to the project folder and execute:

```bash
cd /path/to/project
./sbt ";clean;test;it:test;coverageReport"
```

### Algorithmic complexity

Most of the time complexity in the provided solution comes from the Bellman-Ford algorithm. A case, where any currency can be exchange to any another, can modelled as the complete graph. In the same time it is the worst case scenario.

Time complexity of Bellman-Ford algorithm is Θ(|V||E|) where |V| is number of vertices and |E| is number of edges. If the graph is complete, the value of |E| becomes |V|^2. For being sure to find the best available opportunity all the vertices are examined. So, unfortunately, overall time complexity becomes Θ(|V|^4).

What can be done to improve the calculation time is, for example, to change the architecture. The function of the `CycleFinder`:

```scala
def find(graph: Graph[Currency, Rate], v: Currency): F[Option[Opportunity]]
```

can be, in fact, a request sent to one of many independent workers and enqueueing the task to find the arbitrage.
Tasks can be performed in parallel, the same for providing results to the main agent. 
Then, the scheduler may be polled for the best arbitrage found so far (with optional infromation, like how many vertices have been already examined).