package jstreamserver.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Utility used to load mime types
 *
 * @author Sergey Prilukin
 */
@Component
public final class MimeProperties extends Properties {

    @Value(value = "classpath:mime.properties")
    public void setMimeFile(Resource mimeFile) throws IOException {
        loadMimeProperties(mimeFile.getInputStream());
    }

    public void loadMimeProperties(InputStream is) throws IOException {
        try {
            Properties tmp = new Properties();
            tmp.load(is);
            is.close();

            for (Map.Entry entry: tmp.entrySet()) {
                String mimeType = (String)entry.getKey();
                String[] extensions = ((String)entry.getValue()).split("[\\s]+");
                for (String extension: extensions) {
                    this.put(extension, mimeType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
