package server.mappers;

import java.util.LinkedHashMap;
import java.util.Map;

public class PageMapper {
    private Map<String, String> permanentlyPageMapper;
    private Map<String, String> temporarilyPageMapper;

    public PageMapper() {
        permanentlyPageMapper = new LinkedHashMap<>();
        temporarilyPageMapper = new LinkedHashMap<>();

        addPermanentUrl("/change-team-v2.html", "/change-team.html");
        addTemporaryUrl("/index-v2.html", "index.html");
    }

    public void addPermanentUrl(String oldUrl, String newUrl) {
        permanentlyPageMapper.put(oldUrl, newUrl);
    }

    public void addTemporaryUrl(String oldUrl, String newUrl) {
        temporarilyPageMapper.put(oldUrl, newUrl);
    }

    public String getMovedUrl(String oldUrl) {
        return permanentlyPageMapper.get(oldUrl);
    }

    public String getFoundUrl(String oldUrl) {
        return temporarilyPageMapper.get(oldUrl);
    }
}
