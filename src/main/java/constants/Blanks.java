package constants;

public class Blanks {
    public static final String CONTENT_DIRECTORY = "./src/main/pages";
    public static final String SCRIPTS_DIRECTORY = "./src/main/scripts/";
    public static final String NOT_IMPLEMENTED_PAGE = "/not_implemented.html";
    public static final String NOT_FOUND_PAGE = "/not_found.html";
    public static final String HTTP_VERSION_NOT_SUPPORTED = "/http_version_not_supported.html";
    public static final String FORBIDDEN_FILE = "/forbidden_file.html";
    public static final String[] PROTECTED_ROUTES = {
        "/favicon.ico",
        "/not_found.html",
        "/index.html",
        "/head-request.html",
        "/change-team.html",
        "/delete-team.html",
        "/bad-post.html",
        "/not_implemented.html",
        "/server-error-post.html",
        "/unauth-post.html",
        "/some-page.html",
        "/ruby_helper.rb/fake-post",
        "/ruby_helper.rb/post-change",
        "/not_existed_helper.rb/post"
    };

    public static final String HTTP_VERSION = "HTTP/1.1";
}
