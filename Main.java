import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Sample events
        List<Event> events = new ArrayList<>();
        events.add(new Event("Food Drive", "2024-11-10", "Charity"));
        events.add(new Event("Clothes Donation", "2024-12-01", "Fundraiser"));

        // Sample volunteers
        List<String> volunteers = new ArrayList<>();
        volunteers.add("volunteer1");
        volunteers.add("volunteer2");

        // Launch login page
        new LoginPage(events, volunteers).setVisible(true);
    }
}






