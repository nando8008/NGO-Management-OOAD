public class Event {
    private String name;
    private String date;
    private String type;

    public Event(String name, String date, String type) {
        this.name = name;
        this.date = date;
        this.type = type;
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

    @Override
    public String toString() {
        return name + " on " + date + " (" + type + ")";
    }
}