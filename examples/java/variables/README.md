# Demo VariablesExamples

This demo application showcases a workflow that involves several types of variables, including:

- INT (64-bit Integer)
- DOUBLE (Floating-point number)
- BOOL (Boolean)
- JSON_OBJ (JSON Object)

## Workflow Steps

The workflow performs the following steps:

### 1. Input Text

- Declare a variable called "input-text" of type String. This variable contains the text to be analyzed.

### 2. Sentiment Analysis

- Pass the "input-text" variable as input to the "sentiment-analysis" task.
- For demonstration purposes, the "sentiment-analysis" task will generate a random double between 0.0 and 100.0, simulating the sentiment score.

### 3. Process Text

- Take the result of the "sentiment-analysis" task (sentiment score) as an argument.
- Create a JSON object containing the user-id, text length, and sentiment score.

### 4. Send Result

- Pass the resulting JSON object as an argument to the "send" task.
- For the demo, the "send" task will print the JSON string representation in the console.

This workflow demonstrates the flow of data between tasks and the handling of different variable types.

## Workflow Execution

### Start Workers

```
./gradlew example-variables:run
```

### Run workflow

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-variables input-text 'this is a very long text' add-length false user-id 1234
```

### Search Variables

#### By input-text

```
lhctl search variable --name input-text --value 'this is a very long text' --varType STR --wfSpecName example-variables --wfSpecMajorVersion 0 --wfSpecRevision 0
```

#### By user-id

```
lhctl search variable --name user-id --value 1234 --varType INT --wfSpecName example-variables --wfSpecMajorVersion 0 --wfSpecRevision 0
```

#### By sentiment-score

```
lhctl search variable --name sentiment-score --value <sentiment-score> --varType DOUBLE --wfSpecName example-variables --wfSpecMajorVersion 0 --wfSpecRevision 0
```

> Notice the variable <sentiment-score>

#### By add-length

```
lhctl search variable --name add-length --value false --varType BOOL --wfSpecName example-variables --wfSpecMajorVersion 0 --wfSpecRevision 0
```
