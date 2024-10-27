import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        IDandPasswords idandPasswords = new IDandPasswords();

        List<Event> events = new ArrayList<>();
        events.add(new Event("Fundraiser Gala", "2024-11-15", "Fundraiser"));
        events.add(new Event("Health Camp", "2024-12-10", "Medical"));
        events.add(new Event("Environmental Awareness", "2024-10-30", "Education"));

        new LoginPage(idandPasswords.getLoginInfo(), events);
    }
}


