/*
 * Copyright (c) 2011 Sergey Prilukin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jstreamserver.utils.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for rendering velocity templates
 *
 * @author Sergey Prilukin
 */
public final class VelocityRenderer {

    public static final String VELOCITY_PROPERTIES_PATH = "velocity.properties";
    public static VelocityEngine VELOCITY_ENGINE;

    public static void renderTemplate(String pathToTemplate, Map<String, Object> model, Writer output) throws IOException {

        VelocityContext vc = new VelocityContext();

        if (model != null && model.size() > 0) {
            for (Map.Entry<String, Object> entry: model.entrySet()) {
                vc.put(entry.getKey(), entry.getValue());
            }
        }

        Template template = getVelocityEngine().getTemplate(pathToTemplate);
        template.merge(vc, output);
        output.flush();
        output.close();
    }

    public static InputStream renderTemplate(String pathToTemplate, Map<String, Object> model) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        renderTemplate(pathToTemplate, model, new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream)));

        return (new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    private static VelocityEngine getVelocityEngine() throws IOException {
        if (VELOCITY_ENGINE == null) {
            synchronized (VelocityRenderer.class) {
                if (VELOCITY_ENGINE == null) {
                    VELOCITY_ENGINE = new VelocityEngine();
                    Properties properties = new Properties();
                    properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(VELOCITY_PROPERTIES_PATH));
                    VELOCITY_ENGINE.init(properties);
                }
            }
        }

        return VELOCITY_ENGINE;
    }
}
