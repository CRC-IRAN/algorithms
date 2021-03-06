/*
 * Copyright (C) 2013 Information Management Services, Inc.
 */
package com.imsweb.algorithms.napiia;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

public class NapiiaUtilsTest {

    @Test
    public void assertInfo() {
        Assert.assertNotNull(NapiiaUtils.ALG_VERSION);
        Assert.assertNotNull(NapiiaUtils.ALG_NAME);
        Assert.assertNotNull(NapiiaUtils.ALG_INFO);
    }

    @Test
    public void testComputeNapiia() {

        //test different flavor of methods for special cases
        List<Map<String, String>> patient1 = new ArrayList<>();
        Assert.assertEquals("", NapiiaUtils.computeNapiia(patient1).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(patient1).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_2_2_3, NapiiaUtils.computeNapiia(patient1).getReasonForReview());

        NapiiaInputPatientDto patient2 = new NapiiaInputPatientDto();
        Assert.assertEquals("", NapiiaUtils.computeNapiia(patient2).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(patient2).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_2_2_3, NapiiaUtils.computeNapiia(patient2).getReasonForReview());

        NapiiaInputRecordDto rec = new NapiiaInputRecordDto();
        Assert.assertEquals("", NapiiaUtils.computeNapiia(rec).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(rec).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_2_2_3, NapiiaUtils.computeNapiia(rec).getReasonForReview());

        Map<String, String> record = new HashMap<>();
        //special record value
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_2_2_3, NapiiaUtils.computeNapiia(record).getReasonForReview());

        //SINGLE RACE: race 96 or race 97 at race1 the remaining empty and Hispanic origin(1-6,8).
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "1");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE1, "97");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //race 96 0r 97 at different place other than race 1.
        record.clear();
        record.put(NapiiaUtils.PROP_RACE4, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "1");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_7, NapiiaUtils.computeNapiia(record).getReasonForReview());

        //SINGLE RACE: race 96 or race 97 at race1 the remaining empty and not-Hispanic origin(0,7,9).
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Asian birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "IND");
        Assert.assertEquals("16", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "0");
        Assert.assertEquals("16", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "x");
        Assert.assertEquals("16", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //Pacific Islander birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ASM");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Excluded Asian and Pacific Islander birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "MDV");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Excluded Hispanic birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PAN");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //change to 97
        record.put(NapiiaUtils.PROP_RACE1, "97");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ASM");
        Assert.assertEquals("27", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //SINGLE RACE: race 96 or race 97 at race3 the remaining empty and not-Hispanic origin(0,7,9).
        record.clear();
        record.put(NapiiaUtils.PROP_RACE3, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "0");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_7, NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Asian birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "IND");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_7, NapiiaUtils.computeNapiia(record).getReasonForReview());

        //SINGLE RACE: race 96 or race 97 at race 1, the remaining empty, non-hispanic origin (0,7,9)
        //test applying names for 96
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "7");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "Amrit");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "8");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "tuat");
        Assert.assertEquals("10", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "tezuka");
        Assert.assertEquals("05", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "tovez");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "2");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "tezuka");
        Assert.assertEquals("05", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "ffffff");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "NGU");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "ut");
        Assert.assertEquals("10", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "fong");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "NANTHAVONG");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //SINGLE RACE: race 96 or race 97 at race 1, the remaining empty, non-hispanic origin (0,7,9)
        //test applying names for 97
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "97");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "9");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "keahi");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "2");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "keahi");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "x");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "keahi");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "2");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "quinene");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "LAULU");
        Assert.assertEquals("27", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "2");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "zhuo");
        Assert.assertEquals("27", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //SINGLE RACE: single race other than 96 0r 97 at race1
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "13");
        Assert.assertEquals("13", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE1, "45");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE1, "99");
        Assert.assertEquals("99", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //SINGLE RACE: single race other than 96 0r 97 at race3
        record.clear();
        record.put(NapiiaUtils.PROP_RACE3, "13");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE3, "45");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //Multiple races not involving 96 or 97
        record.clear();
        record.put(NapiiaUtils.PROP_RACE5, "15");
        record.put(NapiiaUtils.PROP_RACE1, "01");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE3, "24");
        Assert.assertEquals("01", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_2_2_3, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE4, "07");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE4, "07");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE1, "01");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //Multiple races involving 96 or 97
        record.clear();
        record.put(NapiiaUtils.PROP_RACE5, "15");
        record.put(NapiiaUtils.PROP_RACE1, "96");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE3, "24");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_3, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE5, "07");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "15");
        record.put(NapiiaUtils.PROP_RACE2, "18");
        record.put(NapiiaUtils.PROP_RACE4, "96");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_3, NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "03");
        record.put(NapiiaUtils.PROP_RACE4, "96");
        Assert.assertEquals("03", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_5, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE2, "02");
        Assert.assertEquals("03", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_5, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE1, "02");
        Assert.assertEquals("02", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_5, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE3, "06");
        Assert.assertEquals("02", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_7, NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "97");
        record.put(NapiiaUtils.PROP_RACE5, "96");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_6, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE5, "30");
        Assert.assertEquals("30", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE3, "97");
        record.put(NapiiaUtils.PROP_RACE5, "96");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_6, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE1, "03");
        Assert.assertEquals("03", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_6, NapiiaUtils.computeNapiia(record).getReasonForReview());

        //multiple races applying step 3 and 4
        record.clear();
        record.put(NapiiaUtils.PROP_RACE5, "97");
        record.put(NapiiaUtils.PROP_RACE1, "01");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Asian birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "IND");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Pacific Islander birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ASM");
        Assert.assertEquals("27", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Excluded Asian and Pacific Islander birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "MDV");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        //Excluded Hispanic birthplaces
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PAN");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE5, "97");
        record.put(NapiiaUtils.PROP_RACE2, "01");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ASM");
        Assert.assertEquals("27", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "IND");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //test applying names for multiple races
        //Pacific Islander
        record.clear();
        record.put(NapiiaUtils.PROP_RACE5, "97");
        record.put(NapiiaUtils.PROP_RACE1, "01");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "ATOIGUE");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "ATOIGUE");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "ATOIGUE");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "Jhanji");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "2");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "3");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "RAJARAMAN");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "ADUSUMILLI");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "DEPANTE");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "SUMAIT");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "CHAMPACO");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "Taijeron");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "aguon");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "ATOIGUE");
        Assert.assertEquals("22", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "KUEHU");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "xiz");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "mafi");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "yandaLL");
        Assert.assertEquals("27", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PAK");
        Assert.assertEquals("27", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "MMR");
        Assert.assertEquals("97", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PLW");
        Assert.assertEquals("20", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE3, "07");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //test applying names for multiple races
        //Asian
        record.clear();
        record.put(NapiiaUtils.PROP_RACE2, "96");
        record.put(NapiiaUtils.PROP_RACE4, "01");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "RAJARAMAN");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "edita");
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "8");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "lalita");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "felIpe");
        Assert.assertEquals("06", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "8");
        Assert.assertEquals("06", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        record.put(NapiiaUtils.PROP_NAME_LAST, "bbbbbb");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "Moyo");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "Moyo");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "2");
        Assert.assertEquals("05", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "VALLABHANENI");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "KUEHU");
        Assert.assertEquals("05", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "dddddd");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_LAST, "leeWong");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "dddddd");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "we");
        Assert.assertEquals("08", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "kurachi");
        Assert.assertEquals("05", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "GGGG");
        Assert.assertEquals("08", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "Manyvong");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "chitre");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_MAIDEN, "Maluia");
        Assert.assertEquals("08", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_NAME_FIRST, "jagir");
        Assert.assertEquals("15", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_SEX, "1");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //test all other multiple races
        record.clear();
        record.put(NapiiaUtils.PROP_RACE2, "10");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE4, "01");
        Assert.assertEquals("10", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE5, "16");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_2_2_3, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE1, "04");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_2_2_3, NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_RACE3, "07");
        Assert.assertEquals("07", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //the following tests added for changes related to squish issue 207
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "9");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "CHN");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "9");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PAK");
        Assert.assertEquals("17", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "9");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "UMI");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "9");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "BLZ");
        record.put(NapiiaUtils.PROP_SEX, "1");
        record.put(NapiiaUtils.PROP_NAME_LAST, "AGUYEN");
        Assert.assertEquals("10", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ZZU");
        Assert.assertEquals("10", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PAN");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ZZS");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //testing special name LOW and HIGH (the sas xpt file had something to do with this names)
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_RACE2, "01");
        record.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, "0");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "USA");
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertFalse(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertNull(NapiiaUtils.computeNapiia(record).getReasonForReview());

        //testing cases with 1.3.4 and other related to #215
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "01");
        record.put(NapiiaUtils.PROP_RACE2, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "MDV");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "01");
        record.put(NapiiaUtils.PROP_RACE2, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "GTM");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "01");
        record.put(NapiiaUtils.PROP_RACE2, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PHL");
        Assert.assertEquals("06", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("06", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "01");
        record.put(NapiiaUtils.PROP_RACE2, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "XMC");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "01");
        record.put(NapiiaUtils.PROP_RACE2, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ZZU");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "01");
        record.put(NapiiaUtils.PROP_RACE2, "96");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        //test r1 = 96
        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "MDV");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "GTM");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PHL");
        Assert.assertEquals("06", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("06", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "XMC");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "ZZU");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "96");
        Assert.assertEquals("96", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("04", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE2, "96");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, "PHL");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        record.put(NapiiaUtils.PROP_SEX, "2");
        record.put(NapiiaUtils.PROP_NAME_FIRST, "VALERIE");
        record.put(NapiiaUtils.PROP_NAME_LAST, "LOW");
        Assert.assertEquals("", NapiiaUtils.computeNapiia(record).getNapiiaValue());

        record.clear();
        record.put(NapiiaUtils.PROP_RACE1, "33");
        record.put(NapiiaUtils.PROP_RACE2, "96");
        Assert.assertEquals("33", NapiiaUtils.computeNapiia(record).getNapiiaValue());
        Assert.assertTrue(NapiiaUtils.computeNapiia(record).getNeedsHumanReview());
        Assert.assertEquals(NapiiaUtils.REASON_1_3_7, NapiiaUtils.computeNapiia(record).getReasonForReview());
    }

    @Test
    public void testCsvFile() throws IOException {
        int line = 0;
        for (String[] row : new CSVReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/testNAPIIA.csv"), "US-ASCII"), ',', '\"', 1).readAll()) {
            line++;

            //if (line != 10)
            //    continue;

            Map<String, String> rec = new HashMap<>();
            rec.put(NapiiaUtils.PROP_RACE1, row[0].trim().isEmpty() ? null : row[0]);
            rec.put(NapiiaUtils.PROP_RACE2, row[1].trim().isEmpty() ? null : row[1]);
            rec.put(NapiiaUtils.PROP_RACE3, row[2].trim().isEmpty() ? null : row[2]);
            rec.put(NapiiaUtils.PROP_RACE4, row[3].trim().isEmpty() ? null : row[3]);
            rec.put(NapiiaUtils.PROP_RACE5, row[4].trim().isEmpty() ? null : row[4]);
            rec.put(NapiiaUtils.PROP_SPANISH_HISPANIC_ORIGIN, row[5].trim().isEmpty() ? null : row[5]);
            rec.put(NapiiaUtils.PROP_BIRTH_PLACE_COUNTRY, row[6].trim().isEmpty() ? null : row[6]);
            rec.put(NapiiaUtils.PROP_SEX, row[7].trim().isEmpty() ? null : row[7]);
            rec.put(NapiiaUtils.PROP_NAME_LAST, row[8].trim().isEmpty() ? null : row[8]);
            rec.put(NapiiaUtils.PROP_NAME_MAIDEN, row[9].trim().isEmpty() ? null : row[9]);
            rec.put(NapiiaUtils.PROP_NAME_FIRST, row[10].trim().isEmpty() ? null : row[10]);

            String napiia = row[11];
            Boolean review = Boolean.valueOf(row[12]);
            String reason = row[13].trim().isEmpty() ? null : row[13];
            NapiiaResultsDto results = NapiiaUtils.computeNapiia(rec);

            if (!napiia.equals(results.getNapiiaValue()))
                Assert.fail("Unexpected napiia result in CSV data file for row #" + line + " " + Arrays.asList(row) + "  " + results.getNapiiaValue());
            if (!review.equals(results.getNeedsHumanReview()))
                Assert.fail("Unexpected needs manual review result in CSV data file for row #" + line + " " + Arrays.asList(row) + "  " + results.getNeedsHumanReview());
            if (results.getReasonForReview() == null ? reason != null : !results.getReasonForReview().equals(reason))
                Assert.fail("Unexpected reason for review result in CSV data file for row #" + line + " " + Arrays.asList(row) + "  " + results.getReasonForReview());
        }
        //System.out.println(line + " cases tested!");
    }
}