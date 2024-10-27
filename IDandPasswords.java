import java.util.HashMap;

public class IDandPasswords {
    HashMap<String, String> logininfo = new HashMap<>();

    IDandPasswords() {
        logininfo.put("Ayush", "1234");
        logininfo.put("bb", "bb");
        logininfo.put("Joah", "password");
    }

    protected HashMap<String, String> getLoginInfo() {
        return logininfo;
    }
}

