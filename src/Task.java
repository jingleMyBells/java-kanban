public class Task {
    private final int id;
    private String title;
    private String description;
    private Status status;

    public Task(int id, String title, String description) {
        this.id = id + 1;
        this.title = title;
        this.description = description;
        this.status = Status.NEW;
    }


    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
