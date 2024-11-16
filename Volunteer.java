import java.util.Date;

public class Volunteer {
    private String username;
    private String password;
    private String gender;
    private Date dateOfBirth;

    public Volunteer(String username, String password, String gender, Date dateOfBirth) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
    }

    // Getter methods
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getGender() {
        return gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }
}
