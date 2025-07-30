package io.littlehorse.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

/*
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class VariablesExample {

    private static final Logger log = LoggerFactory.getLogger(VariablesExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-variables",
            wf -> {
                WfRunVariable inputText = wf.addVariable("input-text", VariableType.STR).searchable().masked();

                WfRunVariable addLength = wf.addVariable(
                    "add-length",
                    VariableType.BOOL
                ).searchable();

                WfRunVariable userId = wf
                    .addVariable("user-id", VariableType.INT).searchable();

                WfRunVariable sentimentScore = wf
                    .addVariable("sentiment-score", VariableType.DOUBLE).searchable();

                WfRunVariable processedResult = wf
                    .addVariable("processed-result", VariableType.JSON_OBJ)
                    .searchableOn("$.sentimentScore", VariableType.DOUBLE)
                    .masked();

                NodeOutput sentimentAnalysisOutput = wf.execute(
                    "sentiment-analysis",
                    inputText
                );
                wf.mutate(
                    sentimentScore,
                    VariableMutationType.ASSIGN,
                    sentimentAnalysisOutput
                );
                NodeOutput processedTextOutput = wf.execute(
                    "process-text",
                    inputText,
                    sentimentScore,
                    addLength,
                    userId
                );
                wf.mutate(
                    processedResult,
                    VariableMutationType.ASSIGN,
                    processedTextOutput
                );
                    wf.execute("send", processedResult);
                
                    // EXTEND a String test
                    var myStr = wf.declareStr("my-str");
                    
                    wf.doIf(myStr.isNotEqualTo(null), then -> {
                        myStr.assign(myStr.extend("-suffix"));
                    });
            
                    // Add an int and composite expressions
                    var intToAdd = wf.declareInt("int-to-add");
                    var intToAddResult = wf.declareInt("int-to-add-result");
                    wf.doIf(intToAdd.isNotEqualTo(null), then -> {
                        // Tests compound expressions
                        intToAddResult.assign(wf.execute("expr-add-one", intToAdd.add(1)));
                    });
            
                    // Division By Zero test
                    var thingToDivideByZero = wf.declareInt("thing-to-divide-by-zero");
                    var divideByZeroResult = wf.declareInt("divide-by-zero-result");
                    wf.doIf(thingToDivideByZero.isNotEqualTo(null), then -> {
                        divideByZeroResult.assign(thingToDivideByZero.divide(0));
                    });
            
                    // Test precision of arithmetic. Make use of the fact that we don't have
                    // strong typing on json objects so that we can use a jsonpath to arbitrarily
                    // set input values.
                    var divisionTestJson = wf.declareJsonObj("dividend-tests");
                    var divisionResult = wf.declareDouble("division-result");
                    var divisionResultInt = wf.declareInt("division-result-int");
                    wf.doIf(divisionTestJson.isNotEqualTo(null), then -> {
                        LHExpression foobar = divisionTestJson.jsonPath("$.lhs").divide(divisionTestJson.jsonPath("$.rhs"));
                        divisionResult.assign(foobar);
                        divisionResultInt.assign(foobar);
                    });
            
                    // This test uses a complex expression where the things we are computing over
                    // have the double precision. We want to make sure that the computation is
                    // executed
                    // with double precision whether we assign the result to an int or a double.
                    var quantity = wf.declareInt("quantity");
                    var price = wf.declareDouble("price");
                    var discountPercentage = wf.declareInt("discount-percentage");
                    var totalPriceInt = wf.declareInt("total-price-int");
                    var totalPriceDouble = wf.declareDouble("total-price-double");
                    wf.doIf(quantity.isNotEqualTo(null), then -> {
                        // TotalPrice = Quantity * Price * (1 - DiscountPercentage / 100)
                        LHExpression pedro = quantity.multiply(price)
                                .multiply(wf.subtract(1.0, discountPercentage.divide(100.0)));
                        totalPriceInt.assign(pedro);
                        totalPriceDouble.assign(pedro);
                    });
            
                    // Test mutating sub-fields of a json object
                    var json = wf.declareJsonObj("json");
                    wf.doIf(json.isNotEqualTo(null), then -> {
                        json.jsonPath("$.foo").assign("bar");
                    });
            
                    // Test mutating doubly-nested fields of a Json Object
                    var nestedJson = wf.declareJsonObj("nested-json");
                    wf.doIf(nestedJson.isNotEqualTo(null), then -> {
                        nestedJson.jsonPath("$.foo.bar").assign("baz");
                    });
            }
        );
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
                System.getProperty("user.home"),
                ".config/littlehorse.config").toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static List<LHTaskWorker> getTaskWorker(LHConfig config) {
        MyWorker executable = new MyWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "sentiment-analysis", config),
                new LHTaskWorker(executable, "process-text", config),
                new LHTaskWorker(executable, "send", config),
                new LHTaskWorker(executable, "expr-add-one", config));
        // Gracefully shutdown
        Runtime
                .getRuntime()
                .addShutdownHook(
                        new Thread(() -> workers.forEach(worker -> {
                            log.debug("Closing {}", worker.getTaskDefName());
                            worker.close();
                        })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        List<LHTaskWorker> workers = getTaskWorker(config);

        // Register tasks
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register a workflow
        workflow.registerWfSpec(client);

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }

}
