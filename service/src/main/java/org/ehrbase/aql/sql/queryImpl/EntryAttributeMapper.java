/*
 * Modifications copyright (C) 2019 Christian Chevalley, Vitasystems GmbH and Hannover Medical School.

 * This file is part of Project EHRbase

 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehrbase.aql.sql.queryImpl;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Map a datavalue UML expression from an ARCHETYPED structure into its RM/JSON representation
 * see http://www.openehr.org/releases/trunk/UML/ for field identification
 * Created by christian on 5/11/2016.
 */
public class EntryAttributeMapper {

    public static final String ISM_TRANSITION = "ism_transition";
    public static final String NAME = "name";
    public static final String TIME = "time";
    public static final String ORIGIN = "origin";
    public static final String OTHER_PARTICIPATIONS = "other_participations";
    public static final String SLASH_VALUE = "/value";
    public static final String VALUE = "value";
    public static final String DEFINING_CODE = "defining_code";
    public static final String SLASH = "/";
    public static final String COMMA = ",";
    public static final String LOWER = "lower";
    public static final String UPPER = "upper";
    public static final String INTERVAL = "interval";
    public static final String MAPPINGS = "mappings";

    //from Locatable
    private static String toCamelCase(String underscoreSeparated) {
        if (!underscoreSeparated.contains("_")) {
            return underscoreSeparated;
        }
        StringTokenizer tokens = new StringTokenizer(underscoreSeparated, "_");
        StringBuilder buf = new StringBuilder();
        while (tokens.hasMoreTokens()) {
            String word = tokens.nextToken();
            if (buf.length() == 0) {
                buf.append(word);
            } else {
                buf.append(word.substring(0, 1).toUpperCase());
                buf.append(word.substring(1));
            }
        }
        return buf.toString();
    }

    private static Integer firstOccurence(int offset, List<String> list, String match) {
        for (int i = offset; i < list.size(); i++) {
            if (list.get(i).equals(match))
                return i;
        }
        return null;
    }

    /**
     * do a simple toCamelCase translation... and prefix all with /value :-)
     *
     * @param attribute
     * @return
     */
    public static String map(String attribute) {
        List<String> fields = new ArrayList<>();

        fields.addAll(Arrays.asList(attribute.split(SLASH)));
        fields.remove(0);

        int floor = 1;

        if (fields.size() < 1)
            return null; //this happens when a non specified value is queried f.e. the whole json body

        //deals with the tricky ones first...
        if (fields.get(0).equals(ISM_TRANSITION)) {
            //get the next key and concatenate...
            String subfield = fields.get(1);
            fields.remove(0);
            fields.set(0, ISM_TRANSITION + SLASH + subfield);
            if (!fields.get(1).equals(NAME)) {
                fields.add(1, SLASH_VALUE);
            }
            floor = 2;
        } else if (fields.get(0).equals(OTHER_PARTICIPATIONS)) { //insert a tag value
            fields.set(0, OTHER_PARTICIPATIONS);
            fields.add(1, "0");
            fields.add(2, SLASH_VALUE);
            floor = 3;
        } else if (fields.get(0).equals(NAME)) {
            fields.add(1, "0"); //name is now formatted as /name -> array of values! Required to deal with cluster items
        } else if (fields.size() >= 2 && fields.get(1).equals(MAPPINGS)) {
            fields.add(2, "0"); //mappings is now formatted as /mappings -> array of values!
        }else if (fields.get(0).equals(TIME) || fields.get(0).equals(ORIGIN)) {
            if (fields.size() > 1 && fields.get(1).equals(VALUE)) {
                fields.add(VALUE); //time is formatted with 2 values: string value and epoch_offset
                fields.set(1, SLASH_VALUE);
            }
        } else { //this deals with the "/value,value"
            Integer match = firstOccurence(0, fields, VALUE);

            if (match != null) { //deals with "/value/value"
                Integer ndxInterval;

                //TODO traverse to find an interval spec and insert "interval"
                if ((ndxInterval = intervalValueIndex(fields)) > 0) { //interval
                    fields.add(ndxInterval, INTERVAL);
                } else if (match != 0) {
                    //deals with name/value (name value is contained into a list conventionally)
                    if (match > 1 && fields.get(match - 1).equals("name"))
                        fields.set(match, VALUE);
                    else
                        //usual /value
                        fields.set(match, SLASH_VALUE);

                } else if (match + 1 < fields.size() - 1) {
                    Integer first = firstOccurence(match + 1, fields, VALUE);
                    if (first != null && first == match + 1)
                        fields.set(match + 1, SLASH_VALUE);
                }
            }
        }

        //prefix the first element
        fields.set(0, SLASH + fields.get(0));

        //deals with the remainder of the array
        for (int i = floor; i < fields.size(); i++) {

            if (fields.get(i).toUpperCase().equals("NAME")){
                //whenever the canonical json for name is queried
                fields.set(i, "/name,0");
            }
            else
                fields.set(i, toCamelCase(fields.get(i)));

        }

        //elegant...
        String encoded = StringUtils.join(fields, COMMA);

        return encoded;
    }

    private static Integer intervalValueIndex(List<String> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).matches("^lower|^upper")) {
                return i;
            }
        }
        return -1;
    }
}
