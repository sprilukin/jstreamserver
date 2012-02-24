package jstreamserver.web;

import jstreamserver.dto.BreadCrumb;
import jstreamserver.dto.FileListEntry;
import jstreamserver.services.FolderService;
import jstreamserver.utils.ConfigReader;
import jstreamserver.utils.HttpUtils;
import jstreamserver.utils.MimeProperties;
import jstreamserver.utils.RandomAccessFileInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Controller to show folders
 *
 * @author Sergey Prilukin
 */

@Controller
public class FolderController {

    private Log log = LogFactory.getLog(getClass());
    private ObjectMapper jsonMapper = new ObjectMapper();
    
    @Autowired
    private FolderService folderService;

    @Autowired
    private ConfigReader configReader;

    @Autowired
    private MimeProperties mimeProperties;

    @RequestMapping("/")
    public String home() {
        return "redirect:/index";
    }

    @RequestMapping("/index")
    public String listFolder(@RequestParam(value = "path", required = false) String path, Model model) throws Exception {

        List<FileListEntry> files = folderService.getFolderContent(getFile(path), path);
        List<BreadCrumb> breadCrumbs = folderService.getBreadCrumbs(path);

        model.addAttribute("folder", jsonMapper.writeValueAsString(files));
        model.addAttribute("breadCrumbs", jsonMapper.writeValueAsString(breadCrumbs));

        return "directory";
    }

    @RequestMapping("/download")
    public void downloadResource(
            @RequestParam(value = "path", required = false) String path,
            @RequestHeader(value = "Range", required = false) String range,
             HttpServletRequest request, HttpServletResponse response) throws Exception {

        File file = getFile(path);
        if (file != null && file.exists() && file.isFile()) {
            InputStream is = getFileAsStream(file, range, response);

            try {
                IOUtils.copyLarge(is, response.getOutputStream());
            } finally {
                is.close();
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        } else {
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    private InputStream getFileAsStream(File file, String range, HttpServletResponse response) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName());
        String mimeType = mimeProperties.getProperty(extension.toLowerCase());

        setResourceHeaders(mimeType, response);

        if (range != null) {
            long[] rangeArray = parseRange(range, file.length());
            String contentRange = String.format(HttpUtils.CONTENT_RANGE_FORMAT,
                    rangeArray[0], rangeArray[1], file.length());

            response.setHeader(HttpUtils.CONTENT_RANGE_HEADER, contentRange);

            //Range should be an integer
            int rangeLength = (int)(rangeArray[1] - rangeArray[0] + 1);

            response.setStatus(HttpURLConnection.HTTP_PARTIAL);

            return new BufferedInputStream(new RandomAccessFileInputStream(file, rangeArray[0], rangeLength));
        } else {
            response.setHeader(
                    HttpUtils.CONTENT_DISPOSITION_HEADER,
                    String.format(HttpUtils.CONTENT_DISPOSITION_FORMAT, file.getName()));

            return new BufferedInputStream(new FileInputStream(file));
        }
    }

    private void setResourceHeaders(String mimeType, HttpServletResponse response) {

        //Set response headers
        String contentType = mimeType != null ? mimeType : "application/octet-stream";
        if (contentType.startsWith("text")) {
            contentType = contentType + "; charset=" + configReader.getDefaultTextCharset();
        }

        response.setHeader(HttpUtils.CONTENT_TYPE_HEADER, contentType);
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store,private,no-cache");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Connection", "keep-alive");
    }

    private long[] parseRange(String range, long fileLength) {
        if (range == null) {
            return null;
        }

        String[] string = range.split("=")[1].split("-");
        long start = Math.min(Long.parseLong(string[0]), fileLength);
        long end = string.length == 1 ? fileLength - 1 : Math.min(Long.parseLong(string[1]), fileLength);

        long[] rangeArray = new long[2];
        rangeArray[0] = Math.min(start, end);
        rangeArray[1] = Math.max(start, end);

        return rangeArray;
    }

    public File getFile(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return null;
        }

        String rootDir = path.replaceFirst("\\/", "").replaceAll("\\/.*$", "");
        String fsPath = path.replaceFirst("\\/[^\\/]+", "");
        return new File(configReader.getRootDirs().get(rootDir) + fsPath);
    }
}
