package com.inspiredandroid.red

// For desktop, a common way is to use a system property.
// This can be set, for example, in the JVM arguments when running in debug mode: -Dred.debug=true
actual val isDebugBuild: Boolean = System.getProperty("red.debug", "false").toBoolean()
