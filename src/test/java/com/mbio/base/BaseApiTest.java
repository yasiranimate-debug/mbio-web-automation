package com.mbio.base;

import com.mbio.listeners.TestListener;
import org.testng.annotations.Listeners;

/**
 * Parent class of API tests.
 *
 * The class holds nothing but the listener, and that is the point: API tests need to appear in the report,
 * but they must not inherit BaseTest, which opens and closes a browser around every test. Inheriting it
 * would launch a browser for each RestAssured call and never use it.
 *
 * The listener is attached here rather than in the suite files so that running a single test from the IDE,
 * which does not read testng.xml, still produces a report entry.
 */
@Listeners(TestListener.class)
public class BaseApiTest {
}
