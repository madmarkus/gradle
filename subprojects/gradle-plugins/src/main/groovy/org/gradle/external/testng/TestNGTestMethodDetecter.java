/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.external.testng;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Tom Eyckmans
 */
class TestNGTestMethodDetecter extends EmptyVisitor {
    private final TestNGTestClassDetecter testClassDetecter;
    private final Set<String> testMethodAnnotations = new HashSet<String>();

    public TestNGTestMethodDetecter(TestNGTestClassDetecter testClassDetecter) {
        this.testClassDetecter = testClassDetecter;
        testMethodAnnotations.add("Lorg/testng/annotations/Test;");
        testMethodAnnotations.add("Lorg/testng/annotations/BeforeSuite;");
        testMethodAnnotations.add("Lorg/testng/annotations/AfterSuite;");
        testMethodAnnotations.add("Lorg/testng/annotations/BeforeTest;");
        testMethodAnnotations.add("Lorg/testng/annotations/AfterTest;");
        testMethodAnnotations.add("Lorg/testng/annotations/BeforeGroups;");
        testMethodAnnotations.add("Lorg/testng/annotations/AfterGroups;");
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (testMethodAnnotations.contains(desc)) {
            testClassDetecter.setTest(true);
        }
        return new EmptyVisitor();
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return new EmptyVisitor();
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        return new EmptyVisitor();
    }
}
