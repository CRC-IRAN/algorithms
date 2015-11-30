/*
 * Copyright (C) 2013 Information Management Services, Inc.
 */
package com.imsweb.algorithms.napiia;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class is used to calculate the NAPIIA variable. More information can be found here:
 * <a href="http://www.naaccr.org/Research/DataAnalysisTools.aspx">http://www.naaccr.org/Research/DataAnalysisTools.aspx</a>
 * <br/><br/>
 * This Java implementation is based ONLY on the SAS implementation of the algorithm; the PDF documentation was not accurate when
 * this algorithm was implemented and therefore was not taken into account.
 */

public final class NapiiaUtils {

    public static final String ALG_NAME = "NAACCR Asian/Pacific Islander Identification Algorithm";
    public static final String ALG_VERSION = "15";
    public static final String ALG_INFO = "NHAPIIA v15 released in September 2015";

    public static final String PROP_RACE1 = "race1";
    public static final String PROP_RACE2 = "race2";
    public static final String PROP_RACE3 = "race3";
    public static final String PROP_RACE4 = "race4";
    public static final String PROP_RACE5 = "race5";
    public static final String PROP_SPANISH_HISPANIC_ORIGIN = "spanishHispanicOrigin";
    public static final String PROP_BIRTH_PLACE_COUNTRY = "birthplaceCountry";
    public static final String PROP_SEX = "sex";
    public static final String PROP_NAME_LAST = "nameLast";
    public static final String PROP_NAME_MAIDEN = "nameMaiden";
    public static final String PROP_NAME_FIRST = "nameFirst";

    public static final String REASON_1_3_3 = "More than one race 04-32 and one race 96 or 97 (step 1.3.3).";
    public static final String REASON_1_3_5 = "One or more races 02-03 and one race 96 (step 1.3.5).";
    public static final String REASON_1_3_6 = "One race 96 and one race 97 (step 1.3.6).";
    public static final String REASON_1_3_7 = "Multiple combination of races involving race 96 (step 1.3.7).";
    public static final String REASON_2_2_3 = "Multiple combination of races (step 2.2.3).";

    private static final String _GENDER_MALE = "1";
    private static final String _GENDER_FEMALE = "2";

    // special birth place countries corresponding to a given race (indirect identification based on birth place) - Asian
    private static final String[][] _BPC_ASIAN = {{"XCH", "04"}, // Chinese
            {"CHN", "04"}, // Chinese
            {"HKG", "04"}, // Chinese
            {"TWN", "04"}, // Chinese
            {"MAC", "04"}, // Chinese            
            {"JPN", "05"}, // Japanese            
            {"PHL", "06"}, // Filipino
            {"KOR", "08"}, // Korean
            {"PRK", "08"}, // Korean
            {"VNM", "10"}, // Vietnamese
            {"KHM", "13"}, // Kampuchean
            {"THA", "14"}, // Thai
            {"IND", "16"}, // Asian Indian
            {"PAK", "17"} // Pakistan      
    };
    private static final Map<String, String> _BPC_MAP_ASIAN = new HashMap<>();

    static {
        for (String[] s : _BPC_ASIAN)
            _BPC_MAP_ASIAN.put(s[0], s[1]);
    }

    // special birth place countries corresponding to a given race (indirect identification based on birth place) - Asian
    private static final String[][] _BPC_PACIFIC_ISLANDER = {{"ASM", "27"}, // Samoan
            {"KIR", "20"}, // Micronesian, NOS
            {"FSM", "20"}, // Micronesian, NOS            
            {"MHL", "20"}, // Micronesian, NOS
            {"PLW", "20"}, // Micronesian, NOS
            {"XMC", "20"}, // Micronesian, NOS
            {"NRU", "20"}, // Micronesian, NOS
            {"COK", "25"}, // Polynesian, NOS
            {"TUV", "25"}, // Polynesian, NOS
            {"TKL", "25"}, // Polynesian, NOS
            {"XPL", "25"}, // Polynesian, NOS
            {"PYF", "25"}, // Polynesian, NOS
            {"NFK", "25"}, // Polynesian, NOS
            {"PCN", "25"}, // Polynesian, NOS
            {"WSM", "25"}, // Polynesian, NOS
            {"TON", "25"} // Polynesian, NOS
    };
    private static final Map<String, String> _BPC_MAP_PACIFIC_ISLANDER = new HashMap<>();

    static {
        for (String[] s : _BPC_PACIFIC_ISLANDER)
            _BPC_MAP_PACIFIC_ISLANDER.put(s[0], s[1]);
    }

    //Asian birth place countries excluded from indirect identification based on names
    private static final List<String> _BPC_EXCLUDE_ASIAN = Arrays.asList("MDV", "NPL", "BTN", "BGD", "LKA", "MMR", "XMS", "MYS", "SGP", "BND", "IDN", "TLS", "MNG");
    //Hispanic birth place countries excluded from identification based on names
    private static final List<String> _BPC_EXCLUDE_HISP = Arrays.asList("PRI", "MEX", "CUB", "DOM", "ZZC", "GTM", "HND", "SLV", "NIC", "CRI", "PAN", "ZZS", "COL", "VEN", "ECU", "PER", "BOL", "CHL",
            "ARG", "PRY", "URY", "ESP", "AND");
    private static final List<String> _SPANISH_ORIGIN_IS_SPANISH = Arrays.asList("1", "2", "3", "4", "5", "6", "8");

    //lookups
    private static Map<String, Short> _LKUP_NAPIIA_SURNAME_CENSUS_ASIAN = new HashMap<>();
    private static Map<String, Short> _LKUP_NAPIIA_SURNAME_CENSUS_PI = new HashMap<>();
    private static Map<String, Short> _LKUP_NAPIIA_SURNAME_LAUD = new HashMap<>();
    private static Map<String, Short> _LKUP_NAPIIA_GIVEN_LAUD_MALE = new HashMap<>();
    private static Map<String, Short> _LKUP_NAPIIA_GIVEN_LAUD_FEMALE = new HashMap<>();
    private static Map<String, Short> _LKUP_NAPIIA_SURNAME_NAACCR = new HashMap<>();
    private static Map<String, Short> _LKUP_NAPIIA_GIVEN_NAACCR = new HashMap<>();

    /**
     * Calculates the NAPIIA value for the provided record.
     * <br/><br/>
     * The provided record doesn't need to contain all the input variables, but the algorithm wil use the following ones:
     * <ul>
     * <li>race1 (#160)</li>
     * <li>race2 (#160)</li>
     * <li>race3 (#160)</li>
     * <li>race4 (#160)</li>
     * <li>race5 (#160)</li>
     * <li>spanishHispanicOrigin (#190)</li>
     * <li>birthplaceCountry (#254)</li>
     * <li>sex (#220)</li>
     * <li>nameLast (#2230)</li>
     * <li>nameMaiden (#2390)</li>
     * <li>nameFirst (#2240)</li>
     * </ul>
     * All those properties are defined as constants in this class.
     * <br/><br/>
     * Note that some of those properties are part of the full NAACCR Abstract; providing only Indicence information is not enough
     * for this algorithm.
     * <br/><br/>
     * This algorithm returns a result containing the calculated value, a boolean indication whether a human review is required, in which case
     * a reason is also provided.
     * @param record a map of properties representing a NAACCR line
     * @return the computed NAPIIA Results Dto, which has a calculated napiia value, a boolean which indicates whether a human review is needed or not and a reason if human review is required
     */
    public static NapiiaResultsDto computeNapiia(Map<String, String> record) {
        NapiiaInputRecordDto input = new NapiiaInputRecordDto();
        input.setRace1(record.get(PROP_RACE1));
        input.setRace2(record.get(PROP_RACE2));
        input.setRace3(record.get(PROP_RACE3));
        input.setRace4(record.get(PROP_RACE4));
        input.setRace5(record.get(PROP_RACE5));
        input.setSpanishHispanicOrigin(record.get(PROP_SPANISH_HISPANIC_ORIGIN));
        input.setBirthplaceCountry(record.get(PROP_BIRTH_PLACE_COUNTRY));
        input.setSex(record.get(PROP_SEX));
        input.setNameLast(record.get(PROP_NAME_LAST));
        input.setNameMaiden(record.get(PROP_NAME_MAIDEN));
        input.setNameFirst(record.get(PROP_NAME_FIRST));
        return computeNapiia(input);
    }

    /**
     * Calculates the NAPIIA value for the provided Patient.
     * <br/><br/>
     * The provided patient doesn't need to contain all the input variables, but the algorithm wil use the following ones:
     * <ul>
     * <li>race1 (#160)</li>
     * <li>race2 (#160)</li>
     * <li>race3 (#160)</li>
     * <li>race4 (#160)</li>
     * <li>race5 (#160)</li>
     * <li>spanishHispanicOrigin (#190)</li>
     * <li>birthplaceCountry (#254)</li>
     * <li>sex (#220)</li>
     * <li>nameLast (#2230)</li>
     * <li>nameMaiden (#2390)</li>
     * <li>nameFirst (#2240)</li>
     * </ul>
     * All those properties are defined as constants in this class.
     * <br/><br/>
     * Note that some of those properties are part of the full NAACCR Abstract; providing only Indicence information is not enough
     * for this algorithm.
     * <br/><br/>
     * This algorithm returns a result containing the calculated value, a boolean indication whether a human review is required, in which case
     * a reason is also provided.
     * @param patient a list of map of properties representing a NAACCR line
     * @return the computed NAPIIA Results Dto, which has a calculated napiia value, a boolean which indicates whether a human review is needed or not and a reason if human review is required
     */
    public static NapiiaResultsDto computeNapiia(List<Map<String, String>> patient) {
        NapiiaInputRecordDto input = new NapiiaInputRecordDto();
        //Since the following properties are the same for all records lets use one of them and build a record input dto
        if (patient != null && !patient.isEmpty()) {
            input.setRace1(patient.get(0).get(PROP_RACE1));
            input.setRace2(patient.get(0).get(PROP_RACE2));
            input.setRace3(patient.get(0).get(PROP_RACE3));
            input.setRace4(patient.get(0).get(PROP_RACE4));
            input.setRace5(patient.get(0).get(PROP_RACE5));
            input.setSpanishHispanicOrigin(patient.get(0).get(PROP_SPANISH_HISPANIC_ORIGIN));
            input.setBirthplaceCountry(patient.get(0).get(PROP_BIRTH_PLACE_COUNTRY));
            input.setSex(patient.get(0).get(PROP_SEX));
            input.setNameLast(patient.get(0).get(PROP_NAME_LAST));
            input.setNameMaiden(patient.get(0).get(PROP_NAME_MAIDEN));
            input.setNameFirst(patient.get(0).get(PROP_NAME_FIRST));
        }
        return computeNapiia(input);
    }

    /**
     * Calculates the NAPIIA value for the provided Patient DTO.
     * <br/><br/>
     * The provided patient dto has a list of record input dto. The record Dto has the following parameters.
     * <ul>
     * <li>_race1</li>
     * <li>_race2</li>
     * <li>_race3</li>
     * <li>_race4</li>
     * <li>_race5</li>
     * <li>_spanishHispanicOrigin</li>
     * <li>_birthplaceCountry</li>
     * <li>_sex</li>
     * <li>_nameLast</li>
     * <li>_nameMaiden</li>
     * <li>_nameFirst</li>
     * </ul>
     * <br/><br/>
     * This algorithm returns a result containing the calculated value, a boolean indication whether a human review is required, in which case
     * a reason is also provided.
     * @param patient Dto
     * @return the computed NAPIIA Results Dto, which has a calculated napiia value, a boolean which indicates whether a human review is needed or not and a reason if human review is required
     */
    public static NapiiaResultsDto computeNapiia(NapiiaInputPatientDto patient) {
        NapiiaInputRecordDto input = new NapiiaInputRecordDto();
        //Since the properties used to calculate napiia are the same for all record of a patient lets use one of them and build a record input dto
        if (patient != null && patient.getNapiiaInputPatientDtoList() != null && !patient.getNapiiaInputPatientDtoList().isEmpty())
            input = patient.getNapiiaInputPatientDtoList().get(0);
        return computeNapiia(input);
    }

    /**
     * Calculates the NAPIIA value for the provided Record DTO.
     * <br/><br/>
     * The provided record Dto has the following parameters.
     * <ul>
     * <li>_race1</li>
     * <li>_race2</li>
     * <li>_race3</li>
     * <li>_race4</li>
     * <li>_race5</li>
     * <li>_spanishHispanicOrigin</li>
     * <li>_birthplaceCountry</li>
     * <li>_sex</li>
     * <li>_nameLast</li>
     * <li>_nameMaiden</li>
     * <li>_nameFirst</li>
     * </ul>
     * <br/><br/>
     * This algorithm returns a result containing the calculated value, a boolean indication whether a human review is required, in which case
     * a reason is also provided.
     * @param input the <code>NapiiaInputRecordDto</code> input DTO object
     * @return the computed NAPIIA Results Dto, which has a calculated napiia value, a boolean which indicates whether a human review is needed or not and a reason if human review is required
     */
    public static NapiiaResultsDto computeNapiia(NapiiaInputRecordDto input) {
        NapiiaResultsDto napiiaResults = new NapiiaResultsDto();

        //NPE
        if (input == null)
            return napiiaResults;

        String napiia = null, reason = null;
        boolean runBirthPlaceCountries = false;
        boolean runNames = false;

        // get spanish/hispanic origin and race 1
        String spanishOrigin = input.getSpanishHispanicOrigin();
        String race1 = input.getRace1();

        // gather a few stats
        List<String> races = Arrays.asList(input.getRace1(), input.getRace2(), input.getRace3(), input.getRace4(), input.getRace5());
        int r01 = 0, r0203 = 0, r0432 = 0, r07 = 0, r88 = 0, r96 = 0, r97 = 0, r98 = 0, r99 = 0;
        String r0203val = null, r0432val = null, r07val = null;
        for (String r : races) {
            if (r == null || r.equals("88") || r.trim().isEmpty())
                r88++;
            else if (r.equals("99"))
                r99++;
            else if (r.equals("98"))
                r98++;
            else if (r.equals("97"))
                r97++;
            else if (r.equals("96"))
                r96++;
            else if (r.compareTo("04") >= 0 && r.compareTo("32") <= 0) {
                r0432++;
                if (r0432val == null)
                    r0432val = r;

                if (r.equals("07")) {
                    r07++;
                    if (r07val == null)
                        r07val = r;
                }
            }
            else if (r.equals("02") || r.equals("03")) {
                r0203++;
                if (r0203val == null)
                    r0203val = r;
            }
            else if (r.equals("01"))
                r01++;
        }

        // step 1 - Identify cases containing race code 96 or 97
        if (r96 > 0 || r97 > 0) {
            // step 1.1 - Single race code of 96
            if (r96 == 1 && r88 == 4 && "96".equals(race1)) {
                napiia = "96";
                if (!_SPANISH_ORIGIN_IS_SPANISH.contains(spanishOrigin))
                    runBirthPlaceCountries = true;
            }
            // step 1.2 - Single race code of 97
            else if (r97 == 1 && r88 == 4 && "97".equals(race1)) {
                napiia = "97";
                if (!_SPANISH_ORIGIN_IS_SPANISH.contains(spanishOrigin))
                    runBirthPlaceCountries = true;
            }
            // step 1.3 - Race code of 96 or 97 in combination with one or more other race codes
            else {
                // step 1.3.1 - One race code is 04-32, one race code is 96 or 97; others are blank or 88
                if (r0432 == 1 && (r96 == 1 || r97 == 1) && r88 == 3)
                    napiia = r0432val;
                    // step 1.3.2 - One Race code is 07; one race code is 96 or 97;
                else if (r07 >= 1 && (r96 == 1 || r97 == 1))
                    napiia = r07val;
                    // step 1.3.3 - More than one race code is 04-32; one race code is 96 or 97; others are blank or 88.
                else if (r0432 > 1 && (r96 == 1 || r97 == 1) && (r0432 + r88 == 4) && r07 == 0)
                    reason = REASON_1_3_3;
                    // step 1.3.4 - One race code is 01; one race code is 96 or 97; others are blank or 88
                else if (r01 == 1 && (r96 == 1 || r97 == 1) && r88 == 3) {
                    napiia = r96 == 1 ? "96" : "97";  //(#215)
                    runBirthPlaceCountries = true;
                }
                // step 1.3.5 - One or more race codes is 02-03; one race code is 96 or 97; others are blank or 88
                else if (r0203 >= 1 && (r96 == 1 || r97 == 1) && (r0203 + r88 == 4))
                    reason = REASON_1_3_5;
                    // step 1.3.6 - One race code is 96; one race code is 97; others are any value
                else if (r96 == 1 && r97 == 1)
                    reason = REASON_1_3_6;
                    // step 1.3.7 - Any multiple race combination involving code 96 or 97 not listed above
                else
                    reason = REASON_1_3_7;
            }

        }
        // step 2 - Directly code cases not containing code 96 or 97
        else {
            // step 2.1 - Direct Code Single Race Cases
            if (((r01 == 1 || r0203 == 1 || r0432 == 1 || r98 == 1 || r99 == 1) && r88 == 4) || r99 == 5)
                napiia = race1;
                // step 2.2 - Multiple Race Cases
            else {
                // step 2.2.1 - One race code is 01, one race code is 02-32; others are blank or 88
                if (r01 == 1 && (r0203 == 1 || r0432 == 1) && r88 == 3)
                    napiia = r0203 == 1 ? r0203val : r0432val;
                    // step 2.2.2 - One race code is 07, and at least one other race code in race2-race5
                else if (r07 == 1 && r88 < 4)
                    napiia = r07val;
                    // step 2.2.3 - All other multiple race combinations
                else if (r88 < 4 || r88 == 5)
                    // RACE1 will take precedence.
                    reason = REASON_2_2_3;
            }
        }

        // step 3 - Indirect Identification Based on Birthplace
        String birthplaceCountry = input.getBirthplaceCountry();
        if (runBirthPlaceCountries) {
            // step 3.1 - Included Asian and Pacific Island Birthplaces
            if (r96 > 0 && _BPC_MAP_ASIAN.containsKey(birthplaceCountry))
                napiia = _BPC_MAP_ASIAN.get(birthplaceCountry);
            else if (r97 > 0 && _BPC_MAP_PACIFIC_ISLANDER.containsKey(birthplaceCountry))
                napiia = _BPC_MAP_PACIFIC_ISLANDER.get(birthplaceCountry);
                // step 3.2 - Excluded Asian and Pacific Island Birthplaces
                // step 3.3 - Excluded Hispanic Birthplaces
            else if (_BPC_EXCLUDE_ASIAN.contains(birthplaceCountry) || _BPC_EXCLUDE_HISP.contains(birthplaceCountry)) {
                if (napiia == null) //(#215)
                    napiia = race1;
            }
            else
                runNames = true;
        }

        // step 4 - Indirect Identification Based on Name
        String sex = input.getSex();
        String nameResult = null;
        if (runNames) {
            if (_GENDER_MALE.equals(sex))
                nameResult = applyMaleNameIdentification(input, r96 > 0, r97 > 0);
            else if (_GENDER_FEMALE.equals(sex))
                nameResult = applyFemaleNameIdentification(input, r96 > 0, r97 > 0);
            else
                nameResult = applyNotMaleNotFemaleNameIdentification(input, r96 > 0, r97 > 0);
        }
        //Assign the run names result to the napiia value if it is not null
        if (nameResult != null)
            napiia = nameResult;

        // NAPIIA wasn't set and there were an error reason, should default to race1
        if (reason != null && napiia == null)
            napiia = race1;

        // If result is still null or missing at this point, default to unknown
        if (napiia == null)
            napiia = "";

        napiiaResults.setNapiiaValue(napiia);
        if (reason != null) {
            napiiaResults.setNeedsHumanReview(true);
            napiiaResults.setReasonForReview(reason);
        }

        return napiiaResults;
    }

    /**
     * Executes the male indirect identification based on name.
     * @return new NAPIIA code, possible the not-calculated code
     */
    private static String applyMaleNameIdentification(NapiiaInputRecordDto input, boolean asianNOS, boolean pacificIslanderNOS) {
        Short result = null;
        String lastname = input.getNameLast();
        String firstname = input.getNameFirst();

        if (lastname != null && !lastname.trim().isEmpty()) {
            // M1 - Surname in Census surname
            if (asianNOS)
                result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_ASIAN);
            if (pacificIslanderNOS && result == null)
                result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_PI);

            // M2 - Surname in Lauderdale surname
            if (asianNOS && result == null)
                result = codeName(truncateName(lastname), _LKUP_NAPIIA_SURNAME_LAUD);

            // M3 - Surname in NAACCR surname
            if (asianNOS && result == null)
                result = codeName(lastname, _LKUP_NAPIIA_SURNAME_NAACCR);
        }

        if (firstname != null && !firstname.trim().isEmpty()) {
            // M4 - Given name in Lauderdale given name
            if (asianNOS && result == null)
                result = codeName(truncateName(firstname), _LKUP_NAPIIA_GIVEN_LAUD_MALE);

            // M5 - Given name in NAACCR given name
            if (asianNOS && result == null)
                result = codeName(firstname, _LKUP_NAPIIA_GIVEN_NAACCR);
        }

        return result == null ? null : String.format("%02d", result);
    }

    /**
     * Executes the female indirect identification based on name.
     * @return new NAPIIA code, possible the not-calculated code
     */
    private static String applyFemaleNameIdentification(NapiiaInputRecordDto input, boolean asianNOS, boolean pacificIslanderNOS) {
        Short result = null;
        String lastname = input.getNameLast();
        String firstname = input.getNameFirst();
        String maidenname = input.getNameMaiden();

        if (maidenname == null || maidenname.trim().isEmpty()) {
            if (lastname != null && !lastname.trim().isEmpty()) {
                // F2a - Surname in Census surname
                if (asianNOS)
                    result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_ASIAN);
                if (pacificIslanderNOS && result == null)
                    result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_PI);

                // F3a - Surname in Lauderdale surname
                if (asianNOS && result == null)
                    result = codeName(truncateName(lastname), _LKUP_NAPIIA_SURNAME_LAUD);

                // F4a - Surname in NAACCR surname
                if (asianNOS && result == null)
                    result = codeName(lastname, _LKUP_NAPIIA_SURNAME_NAACCR);
            }

            if (firstname != null && !firstname.trim().isEmpty()) {
                // F5a - Given name in Lauderdale given name
                if (asianNOS && result == null)
                    result = codeName(truncateName(firstname), _LKUP_NAPIIA_GIVEN_LAUD_FEMALE);

                // F6a - Given name in NAACCR given name
                if (asianNOS && result == null)
                    result = codeName(firstname, _LKUP_NAPIIA_GIVEN_NAACCR);
            }
        }
        else {
            // F2b - Maiden in Census surname
            if (asianNOS)
                result = codeName(maidenname, _LKUP_NAPIIA_SURNAME_CENSUS_ASIAN);
            if (pacificIslanderNOS && result == null)
                result = codeName(maidenname, _LKUP_NAPIIA_SURNAME_CENSUS_PI);

            // F3b - Maiden name in Lauderdale surname
            if (asianNOS && result == null)
                result = codeName(truncateName(maidenname), _LKUP_NAPIIA_SURNAME_LAUD);

            // F4b - Maiden name in NAACCR surname
            if (asianNOS && result == null)
                result = codeName(maidenname, _LKUP_NAPIIA_SURNAME_NAACCR);

            if (firstname != null && !firstname.trim().isEmpty()) {
                // F5b - Given name in Lauderdale given name
                if (asianNOS && result == null)
                    result = codeName(truncateName(firstname), _LKUP_NAPIIA_GIVEN_LAUD_FEMALE);

                // F6b - Given name in NAACCR given name
                if (asianNOS && result == null)
                    result = codeName(firstname, _LKUP_NAPIIA_GIVEN_NAACCR);
            }

            if (lastname != null && !lastname.trim().isEmpty()) {
                // F7b - Surname in Census surname
                if (asianNOS && result == null)
                    result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_ASIAN);
                if (pacificIslanderNOS && result == null)
                    result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_PI);

                // F8b - Surname in Lauderdale surname
                if (asianNOS && result == null)
                    result = codeName(truncateName(lastname), _LKUP_NAPIIA_SURNAME_LAUD);

                // F9b - Surname in NAACCR surname
                if (asianNOS && result == null)
                    result = codeName(lastname, _LKUP_NAPIIA_SURNAME_NAACCR);
            }
        }

        return result == null ? null : String.format("%02d", result);
    }

    /**
     * Executes the non-male-and-non-female indirect identification based on name.
     * @return new NAPIIA code, possible the not-calculated code
     */
    private static String applyNotMaleNotFemaleNameIdentification(NapiiaInputRecordDto input, boolean asianNOS, boolean pacificIslanderNOS) {
        Short result = null;
        String lastname = input.getNameLast();

        if (lastname != null && !lastname.trim().isEmpty()) {
            // NMF1 - Surname in Census surname
            if (asianNOS)
                result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_ASIAN);
            if (pacificIslanderNOS && result == null)
                result = codeName(lastname, _LKUP_NAPIIA_SURNAME_CENSUS_PI);

            // NMF2 - Surname in Lauderdale surname
            if (asianNOS && result == null)
                result = codeName(truncateName(lastname), _LKUP_NAPIIA_SURNAME_LAUD);

            // NMF3 - Surname in NAACCR surname
            if (asianNOS && result == null)
                result = codeName(lastname, _LKUP_NAPIIA_SURNAME_NAACCR);
        }

        return result == null ? null : String.format("%02d", result);
    }

    //Returns the name truncated to 12 characters.    
    protected static String truncateName(String name) {
        if (name == null || name.length() <= 12)
            return name;
        else
            return name.substring(0, 12);
    }

    protected static synchronized Short codeName(String name, Map<String, Short> lkup) {
        if (name == null)
            return null;

        if (lkup.isEmpty()) {
            try {
                Reader reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/napiia-census-asian.csv"), "US-ASCII");
                List<String[]> rows = new CSVReader(reader).readAll();
                for (String[] row : rows)
                    _LKUP_NAPIIA_SURNAME_CENSUS_ASIAN.put(row[0].toUpperCase(), Short.valueOf(row[1]));
                reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/napiia-census-pi.csv"), "US-ASCII");
                rows = new CSVReader(reader).readAll();
                for (String[] row : rows)
                    _LKUP_NAPIIA_SURNAME_CENSUS_PI.put(row[0].toUpperCase(), Short.valueOf(row[1]));
                reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/napiia-laud-surname.csv"), "US-ASCII");
                rows = new CSVReader(reader).readAll();
                for (String[] row : rows)
                    _LKUP_NAPIIA_SURNAME_LAUD.put(row[0].toUpperCase(), Short.valueOf(row[1]));
                reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/napiia-laud-given-male.csv"), "US-ASCII");
                rows = new CSVReader(reader).readAll();
                for (String[] row : rows)
                    _LKUP_NAPIIA_GIVEN_LAUD_MALE.put(row[0].toUpperCase(), Short.valueOf(row[1]));
                reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/napiia-laud-given-female.csv"), "US-ASCII");
                rows = new CSVReader(reader).readAll();
                for (String[] row : rows)
                    _LKUP_NAPIIA_GIVEN_LAUD_FEMALE.put(row[0].toUpperCase(), Short.valueOf(row[1]));
                reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/napiia-naaccr-surname.csv"), "US-ASCII");
                rows = new CSVReader(reader).readAll();
                for (String[] row : rows)
                    _LKUP_NAPIIA_SURNAME_NAACCR.put(row[0].toUpperCase(), Short.valueOf(row[1]));
                reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("napiia/napiia-naaccr-given.csv"), "US-ASCII");
                rows = new CSVReader(reader).readAll();
                for (String[] row : rows)
                    _LKUP_NAPIIA_GIVEN_NAACCR.put(row[0].toUpperCase(), Short.valueOf(row[1]));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return lkup.get(name.toUpperCase());
    }
}
