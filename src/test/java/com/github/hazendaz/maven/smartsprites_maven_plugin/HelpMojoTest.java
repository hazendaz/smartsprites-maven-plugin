/*
 * Copyright (c) 2012-2026 Hazendaz.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of The Apache Software License,
 * Version 2.0 which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *     Hazendaz (Jeremy Landis).
 */
package com.github.hazendaz.maven.smartsprites_maven_plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class HelpMojoTest {

    @Test
    void testExecuteWithDefaultParameters() {
        HelpMojo mojo = new HelpMojo();
        assertDoesNotThrow(mojo::execute);
    }

    @Test
    void testExecuteWithDetailAndGoal() throws ReflectiveOperationException {
        HelpMojo mojo = new HelpMojo();
        setField(mojo, "detail", true);
        setField(mojo, "goal", "smartsprites");
        setField(mojo, "lineLength", 64);
        setField(mojo, "indentSize", 2);

        assertDoesNotThrow(mojo::execute);
    }

    @Test
    void testGetSingleChildThrowsForMissingAndMultiple() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);
        root.appendChild(document.createElement("a"));
        root.appendChild(document.createElement("a"));

        Method getSingleChild = method("getSingleChild", Node.class, String.class);
        assertThrows(MojoExecutionException.class, () -> invoke(getSingleChild, null, root, "missing"));
        assertThrows(MojoExecutionException.class, () -> invoke(getSingleChild, null, root, "a"));
    }

    @Test
    void testFindSingleChildReturnsNullWhenMissing() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);

        Method findSingleChild = method("findSingleChild", Node.class, String.class);
        Object child = invoke(findSingleChild, null, root, "absent");

        assertNull(child);
    }

    @Test
    void testGetPropertyFromExpressionVariants() throws Exception {
        Method method = method("getPropertyFromExpression", String.class);
        assertEquals("demo.property", invoke(method, null, "${demo.property}"));
        assertNull(invoke(method, null, "${a${b}}"));
        assertNull(invoke(method, null, "plain-value"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testToLinesWrapsAndNormalizesWhitespace() throws Exception {
        Method toLines = method("toLines", String.class, int.class, int.class, int.class);
        List<String> lines = (List<String>) invoke(toLines, null, "\tword\u00A0word longlongtoken", 1, 2, 12);

        assertTrue(lines.size() > 1);
        assertTrue(lines.stream().noneMatch(line -> line.contains("\u00A0")));
    }

    private static Method method(String name, Class<?>... parameterTypes) throws ReflectiveOperationException {
        Method method = HelpMojo.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    private static Object invoke(Method method, Object target, Object... args) throws Exception {
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
