import java.util.HashMap;

public class IDandPassword {
    private HashMap<String, String> loginInfo = new HashMap<>();

    public IDandPassword() {
        loginInfo.put("admin", "admin");
        loginInfo.put("volunteer1", "password1");
    }

    public HashMap<String, String> getLoginInfo() {
        return loginInfo;
    }
}


