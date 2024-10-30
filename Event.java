public class Event {
    private String name;
    private String date;
    private String type;
    private int id;

    public Event(String name, String date, String type, int id) {
        this.name = name;
        this.date = date;
        this.type = type;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}