/*
 * Copyright (c) 2020 vitasystems GmbH and Hannover Medical School.
 *
 * This file is part of project EHRbase
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ehrbase.aql.sql.queryimpl.translator.testcase.pg10.pgsql;

import static org.assertj.core.api.Assertions.assertThat;

import org.ehrbase.aql.sql.queryimpl.translator.testcase.UC22;
import org.junit.Test;

public class UC22Test extends UC22 {

    public UC22Test() {
        super();
        this.expectedSqlExpression =
                "select count(\"_FCT_ARG_0\",\"_FCT_ARG_1\") as \"count\" from (select (ARRAY.COLUMN#>>'{/description[at0001],/items[at0002],0,/value,value}')::TEXT as \"_FCT_ARG_1\", (ARRAY.COLUMN#>>'{/description[at0001],/items[at0004],0,/value,magnitude}')::bigint as \"_FCT_ARG_0\" from \"ehr\".\"entry\" join lateral (\n"
                        + "  select (ehr.xjsonb_array_elements((\"ehr\".\"entry\".\"entry\"#>>'{/composition[openEHR-EHR-COMPOSITION.health_summary.v1],/content[openEHR-EHR-ACTION.immunisation_procedure.v1]}')::jsonb)) \n"
                        + " AS COLUMN) as \"ARRAY\" on true where \"ehr\".\"entry\".\"template_id\" = ?) as \"\"";
    }

    @Test
    public void testIt() {
        assertThat(testAqlSelectQuery()).isTrue();
    }
}
