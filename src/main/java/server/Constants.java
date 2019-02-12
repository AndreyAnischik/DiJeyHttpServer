package server;

public class Constants {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String HEAD = "HEAD";

    public static final String OK = "200";
    public static final String MOVED = "301";
    public static final String FOUND = "302";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String NOT_ACCEPTABLE = "406";
    public static final String REQUEST_TIMEOUT = "408";
    public static final String SERVER_ERROR = "500";
    public static final String NOT_IMPLEMENTED = "501";
    public static final String SERVICE_UNAVAILABLE = "503";
    public static final String HTTP_VERSION_NOT_SUPPORTED = "505";

    public static final String CONTENT_DIRECTORY = "./src/main/pages";
    public static final String SCRIPTS_DIRECTORY = "./src/main/scripts/";
    public static final String NOT_IMPLEMENTED_PAGE = "/not_implemented.html";
}
