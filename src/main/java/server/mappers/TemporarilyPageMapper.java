package server.mappers;

import java.util.LinkedHashMap;
import java.util.Map;

public class TemporarilyPageMapper {
    private Map<String, String> pageMapper;

    public TemporarilyPageMapper() {
        pageMapper = new LinkedHashMap<>();
        addUrl("/index2.html", "index.html");
    }

    public void addUrl(String oldUrl, String newUrl) {
        pageMapper.put(oldUrl, newUrl);
    }

    public String getFoundUrl(String oldUrl){
        return pageMapper.get(oldUrl);
    }
}
