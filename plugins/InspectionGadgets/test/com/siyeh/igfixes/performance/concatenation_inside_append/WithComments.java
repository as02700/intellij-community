/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package com.siyeh.igfixes.performance.concatenation_inside_append;

class WithComments {

    void foo(Object o) {
        StringBuilder sb = new StringBuilder();
        sb.append<caret>(/* before */ "asdf" + " " /* space is important */
                         + //bla should be added here
                         o.bla() + /* add one more asdf */ "asdf" /* after */);
        final String s = sb.toString();
    }
}