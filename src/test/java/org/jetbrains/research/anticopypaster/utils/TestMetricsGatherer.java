package org.jetbrains.research.anticopypaster.utils;

import com.intellij.testFramework.fixtures.*;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.junit.jupiter.api.*;

/**
 * Test class for the MetricsGatherer. Extends the LightJavaCodeInsightFixtureTestCase,
 * which is a class that was written for the express purpose of testing IntelliJ
 * plugins with the full Psi/Project functionality, while reusing the same project in
 * between tests.
 */
public class TestMetricsGatherer extends LightJavaCodeInsightFixtureTestCase {
    // Boolean to ensure the testdata is only added once across the multiple tests
    private boolean addedTestClass = false;

    /**
     * Gets the path for the testdata.
     * @return A string of the testdata path
     */
    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testdata";
    }

    /**
     * Overridden from LightJavaCodeInsightFixtureTestCase. Setup now also ensures
     * the project is initialized fully and adds the testdata to the project.
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        while (!getProject().isInitialized());
        if(!addedTestClass) {
            myFixture.copyDirectoryToProject("", "");
            addedTestClass = true;
        }
    }

    /**
     * Tests that the list size is what we expect. There are 6 methods in
     * Calculator.java and 2 in CalculatorTest.java. Only the 6 from
     * Calculator.java should be gotten.
     */
    public void testMetricsListSize() {
        MetricsGatherer metricsGatherer = new MetricsGatherer();
        System.out.println("Message = Metrics List Has 6 Methods");
        Assertions.assertEquals(6 , metricsGatherer.getMethodsMetrics().size());
    }

    /**
     * Test to ensure that the arraylist for the MethodMetrics is instantiated
     * properly and that the MetricsGatherer doesn't crash.
     */
    public void testMetricsListNotNull() {
        MetricsGatherer metricsGatherer = new MetricsGatherer();
        System.out.println("Message = MetricsList Not Null");
        Assertions.assertNotNull(metricsGatherer.getMethodsMetrics());
    }

    /**
     * Test to ensure that no methods from CalculatorTest made it into the
     * MetricsGatherer. This is tested by seeing if any of the metrics gotten
     * have a 1 line method. The only 1 line methods exist in CalculatorTest.java
     */
    public void testMetricsInListNoTestMethods() {
        MetricsGatherer metricsGatherer = new MetricsGatherer();
        System.out.println("Message = No Single Line Methods Gotten");
        for(FeaturesVector fv: metricsGatherer.getMethodsMetrics()){
            float[] arr = fv.buildArray();
            Assertions.assertNotEquals(1, arr[0]);
        }
    }
}
