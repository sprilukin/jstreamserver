package jstreamserver.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Controller to show folders
 *
 * @author Sergey Prilukin
 */

@Controller
public class FolderController {

    private Log log = LogFactory.getLog(getClass());

    @RequestMapping("/")
    public String home() {
        return "redirect:/index";
    }

    @RequestMapping("/index")
    public String listFolder(Map<String, Object> map) {

        map.put("files", "{}");
        map.put("breadCrumbs", "{}");

        log.debug("HHHHHHHH");
        return "directory";
    }
}
