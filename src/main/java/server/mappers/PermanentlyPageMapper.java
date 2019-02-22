package server.mappers;

import java.util.LinkedHashMap;
import java.util.Map;

public class PermanentlyPageMapper {
    private Map<String, String> pageMapper;

    public PermanentlyPageMapper() {
        pageMapper = new LinkedHashMap<>();
        addUrl("/change-team2.html", "change-team.html");
    }

    public void addUrl(String oldUrl, String newUrl) {
        pageMapper.put(oldUrl, newUrl);
    }

    public String getMovedUrl(String oldUrl){
        return pageMapper.get(oldUrl);
    }
}
