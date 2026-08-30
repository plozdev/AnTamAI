package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SmsAnalysisStatusTest {

    @Test
    fun fromString_mapsExactStatusEnumValues() {
        assertEquals(SmsAnalysisStatus.DANGER, SmsAnalysisStatus.fromString("DANGER"))
        assertEquals(SmsAnalysisStatus.WARNING, SmsAnalysisStatus.fromString("WARNING"))
        assertEquals(SmsAnalysisStatus.SAFE, SmsAnalysisStatus.fromString("SAFE"))
        assertEquals(SmsAnalysisStatus.ANALYZING, SmsAnalysisStatus.fromString("ANALYZING"))
        assertEquals(SmsAnalysisStatus.RETRYING, SmsAnalysisStatus.fromString("RETRYING"))
    }

    @Test
    fun fromString_caseInsensitiveMapping() {
        assertEquals(SmsAnalysisStatus.DANGER, SmsAnalysisStatus.fromString("danger"))
        assertEquals(SmsAnalysisStatus.WARNING, SmsAnalysisStatus.fromString("warning"))
        assertEquals(SmsAnalysisStatus.SAFE, SmsAnalysisStatus.fromString("safe"))
        assertEquals(SmsAnalysisStatus.ANALYZING, SmsAnalysisStatus.fromString("analyzing"))
        assertEquals(SmsAnalysisStatus.RETRYING, SmsAnalysisStatus.fromString("retrying"))
    }

    @Test
    fun fromString_substringMatching() {
        assertEquals(SmsAnalysisStatus.DANGER, SmsAnalysisStatus.fromString("STATUS_DANGER_DETECTED"))
        assertEquals(SmsAnalysisStatus.WARNING, SmsAnalysisStatus.fromString("STATUS_WARNING_FLAG"))
        assertEquals(SmsAnalysisStatus.ANALYZING, SmsAnalysisStatus.fromString("IS_ANALYZING"))
        assertEquals(SmsAnalysisStatus.RETRYING, SmsAnalysisStatus.fromString("STATE_RETRYING_NOW"))
    }

    @Test
    fun fromString_nullAndUnknown_fallbacksToSafe() {
        assertEquals(SmsAnalysisStatus.SAFE, SmsAnalysisStatus.fromString(null))
        assertEquals(SmsAnalysisStatus.SAFE, SmsAnalysisStatus.fromString(""))
        assertEquals(SmsAnalysisStatus.SAFE, SmsAnalysisStatus.fromString("UNKNOWN"))
        assertEquals(SmsAnalysisStatus.SAFE, SmsAnalysisStatus.fromString("RANDOM_STRING_123"))
    }
}
