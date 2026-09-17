package com.mbio.base;

import com.mbio.listeners.TestListener;
import org.testng.annotations.Listeners;

/**
 * Parent class of API tests.
 * API tests do not open a browser, so there is no setUp / tearDown and no screenshot.
 */
@Listeners(TestListener.class)
public class BaseApiTest {
}
