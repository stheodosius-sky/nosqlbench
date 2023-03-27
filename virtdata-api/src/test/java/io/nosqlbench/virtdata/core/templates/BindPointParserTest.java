/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BindPointParserTest {

    @Test
    public void testSpecialCharsInRefBindPoint() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("test {list<one>}", Map.of())).isEqualTo(
            new BindPointParser.Result(
                List.of("test ","list<one>",""),
                List.of(BindPoint.of("list<one>",null, BindPoint.Type.reference)))
        );
    }

    @Test
    public void testSingleRefTypeBindPoint() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("test {one}", Map.of())).isEqualTo(
            new BindPointParser.Result(
                List.of("test ","one",""),
                List.of(BindPoint.of("one",null, BindPoint.Type.reference)))
        );
//        assertThat(bpp.apply("test {one} {{two three}}",Map.of())).containsExactly("test ","one"," ","two three","");
    }

    @Test
    public void testSingleDefinitionTypeBindPoint() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("test {{this is a definition}} and {{another}}", Map.of())).isEqualTo(
            new BindPointParser.Result(
                List.of("test ","this is a definition"," and ","another",""),
                List.of(
                    BindPoint.of(BindPointParser.DEFINITION,"this is a definition", BindPoint.Type.definition),
                    BindPoint.of(BindPointParser.DEFINITION, "another",BindPoint.Type.definition)
                )
            )
        );
    }

    @Test
    public void testInline2TypeBindPoint() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("test {(this is a definition)} and {{another}}", Map.of())).isEqualTo(
            new BindPointParser.Result(
                List.of("test ","this is a definition"," and ","another",""),
                List.of(
                    BindPoint.of(BindPointParser.DEFINITION,"this is a definition", BindPoint.Type.definition),
                    BindPoint.of(BindPointParser.DEFINITION, "another",BindPoint.Type.definition)
                )
            )
        );
    }

    @Test
    public void testCurlyBracesWithinPattern() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("a{{Template(\"-{}-\",Combinations(\"a-z\"))}}b", Map.of())).isEqualTo(
            new BindPointParser.Result(
                List.of("a","Template(\"-{}-\",Combinations(\"a-z\"))","b"),
                List.of(
                    BindPoint.of(BindPointParser.DEFINITION,"Template(\"-{}-\",Combinations(\"a-z\"))", BindPoint.Type.definition)
                )
            )
        );

    }

}
