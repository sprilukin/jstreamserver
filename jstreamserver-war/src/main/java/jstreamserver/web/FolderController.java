package jstreamserver.web;

import jstreamserver.dto.BreadCrumb;
import jstreamserver.dto.FileListEntry;
import jstreamserver.services.FolderService;
import jstreamserver.utils.*;
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

    @Autowired
    private FolderService folderService;

    @Autowired
    private ConfigReader configReader;

    @Autowired
    private MimeProperties mimeProperties;

    @Autowired
    private ControllerUtils controllerUtils;

    @RequestMapping("/")
    public String home() {
        return "redirect:/index";
    }

    @RequestMapping("/index")
    public String listFolder(@RequestParam(value = "path", required = false) String path, Model model) throws Exception {

        List<FileListEntry> files = folderService.getFolderContent(controllerUtils.getFile(path), path);
        List<BreadCrumb> breadCrumbs = folderService.getBreadCrumbs(path);

        model.addAttribute("folder", files);
        model.addAttribute("breadCrumbs", breadCrumbs);

        return "directory";
    }

    @RequestMapping("/download")
    public void downloadResource(
            @RequestParam(value = "path", required = false) String path,
            @RequestHeader(value = "Range", required = false) String range,
             HttpServletRequest request, HttpServletResponse response) throws Exception {

        File file = controllerUtils.getFile(path);
        if (file != null && file.exists() && file.isFile()) {
            InputStream is = controllerUtils.getFileAsStream(file, range, response);

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
}
