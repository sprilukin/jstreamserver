package jstreamserver.web;

import jstreamserver.dto.BreadCrumb;
import jstreamserver.dto.FileListEntry;
import jstreamserver.services.FolderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

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

    @RequestMapping("/")
    public String home() {
        return "redirect:/index";
    }

    @RequestMapping("/index")
    public String listFolder(@RequestParam(value = "path") String path, Map<String, Object> map) throws Exception {

        List<FileListEntry> files = folderService.getFolderContent(path);
        List<BreadCrumb> breadCrumbs = folderService.getBreadCrumbs(path);

        map.put("folder", jsonMapper.writeValueAsString(files));
        map.put("breadCrumbs", jsonMapper.writeValueAsString(breadCrumbs));

        return "directory";
    }
}
