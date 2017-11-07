/*
 * Copyright 2017 Nafundi
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

package org.odk.collectTester.utilities;

public class ListElement {
    private int id;

    private String text1;
    private String text2;

    public ListElement(int id, String text1, String text2) {
        this.id = id;
        this.text1 = text1;
        this.text2 = text2;
    }

    public int getId() {
        return id;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }
}
