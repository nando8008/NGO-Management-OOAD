import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Sample login data
        HashMap<String, String> loginInfo = new HashMap<>();
        loginInfo.put("admin", "admin");
        loginInfo.put("volunteer1", "password1");
        
        // Sample events
        List<Event> events = new ArrayList<>();
        events.add(new Event("Food Drive", "2024-11-10", "Charity"));
        events.add(new Event("Clothes Donation", "2024-12-01", "Fundraiser"));
        
        // Sample volunteers
        List<String> volunteers = new ArrayList<>(Arrays.asList("volunteer1", "volunteer2"));
        
        // Launch login page
        new LoginPage(loginInfo, events, volunteers).setVisible(true);
    }
}
