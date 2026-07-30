package fi.gtrxac.bluewap;

public class WmlTemplates {
    public static final String BEGIN =
        "<?xml version=\"1.0\" encoding='utf-8'?>" +
        "<!DOCTYPE wml PUBLIC \"-//WAPFORUM//DTD WML 1.1//EN\" \"http://www.wapforum.org/DTD/wml_1.1.xml\">" +
        "<wml>" +
        "<head>" +
        "</head>";

    public static final String END = 
        "</card>" +
        "</wml>";

    public static final String LOADING =
        BEGIN +
        "<card title=\"Loading\">" +
        "<p>Loading...</p>" +
        END;

    public static final String ERROR_BEGIN =
        BEGIN +
        "<card title=\"Error\">" +
        "<p>An error occurred:</p>" +
        "<p>";

    public static final String ERROR_END =
        "</p>" +
        END;
}