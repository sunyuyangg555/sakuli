/*
 * Sakuli - Testing and Monitoring-Tool for Websites and common UIs.
 *
 * Copyright 2013 - 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.consol.sakuli.services.receiver.gearman.model.builder;

import de.consol.sakuli.builder.TestCaseExampleBuilder;
import de.consol.sakuli.builder.TestCaseStepExampleBuilder;
import de.consol.sakuli.builder.TestSuiteExampleBuilder;
import de.consol.sakuli.datamodel.TestCase;
import de.consol.sakuli.datamodel.TestSuite;
import de.consol.sakuli.datamodel.state.TestCaseState;
import de.consol.sakuli.datamodel.state.TestCaseStepState;
import de.consol.sakuli.datamodel.state.TestSuiteState;
import de.consol.sakuli.exceptions.SakuliException;
import de.consol.sakuli.services.receiver.gearman.GearmanProperties;
import de.consol.sakuli.services.receiver.gearman.GearmanPropertiesTestHelper;
import de.consol.sakuli.services.receiver.gearman.model.NagiosOutput;
import de.consol.sakuli.services.receiver.gearman.model.PlaceholderMap;
import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import static de.consol.sakuli.services.receiver.gearman.TextPlaceholder.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class OutputBuilderTest {


    private OutputBuilder testling;

    @DataProvider(name = "placeholder")
    public static Object[][] placeholder() {
        return new Object[][]{
                {null, null},
                {"empty {{}} test", "empty {{}} test"},
                {"test message " + STATE.getPattern() + " test", "test message OK test"},
                {STATE.getPattern() + " test", "OK test"},
                {STATE.getPattern() + " test-" + STATE.getPattern(), "OK test-OK"},
                {"test message " + STATE.getPattern(), "test message OK"},
                {"test message " + STATE.getPattern() + " with id \"" + ID.getPattern() + "\"", "test message OK with id \"sakuli-123\""},
        };
    }

    @DataProvider(name = "performanceDataRows")
    public static Object[][] performanceDataRows() {
        return new Object[][]{
                {null, "name with space", null, null, null, "name_with_space=;;;;"},
                {"name_with_space=;;;;", " second_name ", "0", "", "", "name_with_space=;;;; second_name=0;;;;"},
                {" ", " warnIng ", " 15s ", " 10.5s ", " 11.0s ", "warnIng=15s;10.5s;11.0s;;"},
                {"nam1=;;;; nam2=;;;;", "nam3", "0", "", "", "nam1=;;;; nam2=;;;; nam3=0;;;;"},
        };

    }

    @DataProvider(name = "performanceDataRowsNonOverview")
    public static Object[][] performanceDataRowsNonOverview() {
        return new Object[][]{
                {null, "name with space", 0f, 0, 0, "name_with_space=0.00s;;;;"},
                {"name_with_space=;;;;", " second_name ", 12.5f, 10, 14, "name_with_space=;;;; second_name=12.50s;10;14;;"},
        };

    }

    @BeforeMethod
    public void setUp() throws Exception {
        testling = new OutputBuilder();
    }

    @Test(dataProvider = "placeholder")
    public void testReplacePlaceHolder(String testMessage, String expectedMesaage) throws Exception {
        PlaceholderMap map = new PlaceholderMap();
        map.put(STATE, "OK");
        map.put(ID, "sakuli-123");
        assertEquals(OutputBuilder.replacePlaceHolder(testMessage, map), expectedMesaage);

        //test recusiv
        map.put(STATE, STATE_DESC.getPattern());
        map.put(STATE_DESC, "OK");
        assertEquals(OutputBuilder.replacePlaceHolder(testMessage, map), expectedMesaage);
    }

    @Test(dataProvider = "performanceDataRows")
    public void testAddPerformanceDataRow(String currentPerformanceData, String name, String value, String warning, String critical, String expectedData) throws Exception {
        assertEquals(OutputBuilder.addPerformanceDataRow(currentPerformanceData, name, value, warning, critical), expectedData);
    }

    @Test(dataProvider = "performanceDataRowsNonOverview")
    public void testAddPerformanceDataRow(String currentPerformanceData, String name, float duration, int warning, int critical, String expectedData) throws Exception {
        assertEquals(OutputBuilder.addPerformanceDataRow(currentPerformanceData, name, duration, warning, critical), expectedData);
    }

    @Test
    public void testBuild() throws Exception {
        testling = spy(new OutputBuilder());
        TestSuite testSuite = mock(TestSuite.class);
        GearmanProperties gearmanProperties = mock(GearmanProperties.class);
        doReturn("STATUS").when(testling).getStatusSummary(testSuite, gearmanProperties);
        doReturn("PERFORMANCE").when(testling).getPerformanceData(testSuite);

        NagiosOutput result = testling.withTestSuite(testSuite, gearmanProperties).build();
        assertEquals(result.getStatusSummary(), "STATUS");
        assertEquals(result.getPerformanceData(), "PERFORMANCE");
    }

    @Test
    public void testGetPerformanceData() throws Exception {
        Date startDate = new GregorianCalendar(2014, 14, 7, 13, 0).getTime();
        TestSuite testSuiteExample = new TestSuiteExampleBuilder()
                .withId("sakuli-123")
                .withStartDate(startDate)
                .withStopDate(DateUtils.addSeconds(startDate, 120))
                .withWarningTime(100)
                .withCriticalTime(150)
                .withTestCases(Arrays.asList(
                        new TestCaseExampleBuilder().withState(TestCaseState.WARNING_IN_STEP)
                                .withId("case-warning")
                                .withStartDate(startDate)
                                .withStopDate(DateUtils.addSeconds(startDate, 20))
                                .withWarningTime(19)
                                .withCriticalTime(25)
                                .withTestCaseSteps(Arrays.asList(new TestCaseStepExampleBuilder()
                                                .withName("step1")
                                                .withState(TestCaseStepState.WARNING)
                                                .withStartDate(startDate)
                                                .withStopDate(DateUtils.addSeconds(startDate, 10))
                                                .withWarningTime(9)
                                                .buildExample(),
                                        new TestCaseStepExampleBuilder()
                                                .withName("step2")
                                                .withState(TestCaseStepState.OK)
                                                .withStartDate(DateUtils.addSeconds(startDate, 11))
                                                .withStopDate(DateUtils.addSeconds(startDate, 19))
                                                .withWarningTime(10)
                                                .buildExample()))
                                .buildExample(),
                        new TestCaseExampleBuilder().withState(TestCaseState.OK)
                                .withId("case with no steps")
                                .withStartDate(DateUtils.addSeconds(startDate, 25))
                                .withStopDate(DateUtils.addSeconds(startDate, 40))
                                .withWarningTime(0)
                                .withCriticalTime(0)
                                .withTestCaseSteps(null)
                                .buildExample()))
                .buildExample();
        testSuiteExample.refreshState();

        assertEquals(testling.getPerformanceData(testSuiteExample), "suite__state=1;;;; " +
                "suite_sakuli-123=120.00s;100;150;; " +
                "c_1__state_case-warning=1;;;; " +
                "c_1_case-warning=20.00s;19;25;; " +
                "s_1_1_step1=10.00s;9;;; " +
                "s_1_2_step2=8.00s;10;;; " +
                "c_2__state_case_with_no_steps=0;;;; " +
                "c_2_case_with_no_steps=15.00s;;;;");
    }

    @Test
    public void testFormatTestSuiteStateMessageUnknown() throws Exception {
        TestSuite testSuiteExample = new TestSuiteExampleBuilder()
                .withId("sakuli-123")
                .withState(TestSuiteState.RUNNING)
                .withStopDate(null)
                .buildExample();
        GearmanProperties propertiesExample = new GearmanPropertiesExampleBuilder().buildExample();

        String result = testling.formatTestSuiteSummaryStateMessage(testSuiteExample, propertiesExample);
        assertEquals(result, "UNKNOWN - [UNKN] Sakuli suite \"sakuli-123\" ran in -1.00 seconds - suite still running. (Last suite run: xx)");
    }

    @Test
    public void testFormatTestSuiteStateMessageOK() throws Exception {
        Date startDate = new Date();
        TestSuite testSuiteExample = new TestSuiteExampleBuilder()
                .withId("sakuli-123")
                .withState(TestSuiteState.OK)
                .withStartDate(startDate)
                .withStopDate(DateUtils.addSeconds(startDate, 120))
                .buildExample();
        GearmanProperties propertiesExample = new GearmanPropertiesExampleBuilder().buildExample();

        String result = testling.formatTestSuiteSummaryStateMessage(testSuiteExample, propertiesExample);
        String lastRun = OutputBuilder.dateFormat.format(testSuiteExample.getStopDate());
        assertEquals(result, "OK - [OK] Sakuli suite \"sakuli-123\" ran in 120.00 seconds - ok. (Last suite run: " + lastRun + ")");
    }

    @Test
    public void testFormatTestSuiteStateMessageException() throws Exception {
        Date startDate = new Date();
        TestSuite testSuiteExample = new TestSuiteExampleBuilder()
                .withId("sakuli-123")
                .withState(TestSuiteState.ERRORS)
                .withStartDate(startDate)
                .withException(new SakuliException("TEST-ERROR"))
                .withStopDate(DateUtils.addSeconds(startDate, 120))
                .buildExample();
        GearmanProperties propertiesExample = new GearmanPropertiesExampleBuilder().buildExample();

        String result = testling.formatTestSuiteSummaryStateMessage(testSuiteExample, propertiesExample);
        String lastRun = OutputBuilder.dateFormat.format(testSuiteExample.getStopDate());
        assertEquals(result, "CRITICAL - [CRIT] Sakuli suite \"sakuli-123\" ran in 120.00 seconds - EXCEPTION: \"TEST-ERROR\". (Last suite run: " + lastRun + ")");
    }

    @Test
    public void testFormatTestSuiteTableException() throws Exception {
        Date startDate = new Date();
        TestSuite testSuiteExample = new TestSuiteExampleBuilder()
                .withId("sakuli-123")
                .withState(TestSuiteState.ERRORS)
                .withStartDate(startDate)
                .withException(new SakuliException("TEST-ERROR"))
                .withStopDate(DateUtils.addSeconds(startDate, 120))
                .buildExample();
        GearmanProperties propertiesExample = new GearmanPropertiesExampleBuilder().buildExample();

        String result = testling.formatTestSuiteTableStateMessage(testSuiteExample, propertiesExample);
        String lastRun = OutputBuilder.dateFormat.format(testSuiteExample.getStopDate());
        assertEquals(result, "<tr valign=\"top\"><td class=\"serviceCRITICAL\">[CRIT] Sakuli suite \"sakuli-123\" ran" +
                " in 120.00 seconds - EXCEPTION: \"TEST-ERROR\". (Last suite run: " + lastRun + ")</td></tr>");
    }

    @Test
    public void testFormatTestCaseTableStateMessage() throws Exception {
        String htmlTemplate = "<tr valign=\"top\"><td class=\"%s\">%s</td></tr>";
        GearmanProperties properties = GearmanPropertiesTestHelper.initMock(mock(GearmanProperties.class));

        TestCase testCase = new TestCaseExampleBuilder()
                .withState(TestCaseState.OK)
                .withId("case-ok")
                .buildExample();
        assertEquals(testling.formatTestCaseTableStateMessage(testCase, properties),
                String.format(htmlTemplate, "serviceOK", "[OK] case \"case-ok\" ran in 3.00s - ok"));

        Date startDate = new Date();
        testCase = new TestCaseExampleBuilder()
                .withId("case-warning")
                .withState(TestCaseState.WARNING)
                .withStartDate(startDate)
                .withStopDate(DateUtils.addMilliseconds(startDate, 5500))
                .withWarningTime(5)
                .buildExample();
        assertEquals(testling.formatTestCaseTableStateMessage(testCase, properties),
                String.format(htmlTemplate, "serviceWARNING", "[WARN] case \"case-warning\" over runtime (5.50s /warning at 5s) "));

        testCase = new TestCaseExampleBuilder().withState(TestCaseState.WARNING_IN_STEP)
                .withId("case-warning")
                .withTestCaseSteps(Arrays.asList(new TestCaseStepExampleBuilder()
                                .withName("step-name")
                                .withState(TestCaseStepState.WARNING)
                                .withStartDate(startDate)
                                .withStopDate(DateUtils.addMilliseconds(startDate, 3154))
                                .withWarningTime(3)
                                .buildExample(),
                        new TestCaseStepExampleBuilder()
                                .withName("step-name2")
                                .withState(TestCaseStepState.WARNING)
                                .withStartDate(startDate)
                                .withStopDate(DateUtils.addMilliseconds(startDate, 154))
                                .withWarningTime(1)
                                .buildExample()))
                .buildExample();
        assertEquals(testling.formatTestCaseTableStateMessage(testCase, properties),
                String.format(htmlTemplate, "serviceWARNING", "[WARN] case \"case-warning\" over runtime (3.00s /warning in step at 4s) step \"step-name\" (3.15s /warn at 3s), step \"step-name2\" (0.15s /warn at 1s)"));

        testCase = new TestCaseExampleBuilder().withState(TestCaseState.CRITICAL)
                .withId("case-critical")
                .withStartDate(startDate)
                .withStopDate(DateUtils.addMilliseconds(startDate, 8888))
                .withCriticalTime(7)
                .buildExample();
        assertEquals(testling.formatTestCaseTableStateMessage(testCase, properties),
                String.format(htmlTemplate, "serviceCRITICAL", "[CRIT] case \"case-critical\" over runtime (8.89s /critical at 7s) "));

        testCase = new TestCaseExampleBuilder().withState(TestCaseState.ERRORS)
                .withId("case-error")
                .withException(new SakuliException("EXCEPTION-MESSAGE"))
                .buildExample();
        assertEquals(testling.formatTestCaseTableStateMessage(testCase, properties),
                String.format(htmlTemplate, "serviceCRITICAL", "[CRIT] case \"case-error\" EXCEPTION: EXCEPTION-MESSAGE"));
    }

    @Test
    public void testGetStatusSummary() throws Exception {
        TestSuite testSuite = new TestSuiteExampleBuilder()
                .withState(TestSuiteState.OK)
                .withId("TEST-SUITE-ID")
                .withTestCases(Arrays.asList(new TestCaseExampleBuilder()
                        .withId("TEST-CASE-ID")
                        .buildExample()))
                .buildExample();
        GearmanProperties gearmanProperties = new GearmanPropertiesExampleBuilder().buildExample();

        String lastRun = OutputBuilder.dateFormat.format(testSuite.getStopDate());
        String expectedHTML =
                "OK - OK - [OK] Sakuli suite \"TEST-SUITE-ID\" ran in 120.00 seconds - ok. (Last suite run: " + lastRun + ")\\\\n" +
                        "<table style=\"border-collapse: collapse;\">" +
                        "<tr valign=\"top\">" +
                        "<td class=\"serviceOK\">[OK] Sakuli suite \"TEST-SUITE-ID\" ran in 120.00 seconds - ok. (Last suite run: " + lastRun + ")" +
                        "</td>" +
                        "</tr>" +
                        "<tr valign=\"top\">" +
                        "<td class=\"serviceOK\">[OK] case \"TEST-CASE-ID\" ran in 3.00s - ok</td>" +
                        "</tr>" +
                        "</table>";


        String statusSummary = testling.getStatusSummary(testSuite, gearmanProperties);
        assertEquals(statusSummary, expectedHTML);

        NagiosOutput output = testling.withTestSuite(testSuite, gearmanProperties).build();
        String substringStatusSummary = output.getOutputString().substring(0, output.getOutputString().indexOf("|"));
        assertEquals(substringStatusSummary, expectedHTML);
    }
}