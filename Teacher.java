package teacherInfo;

import java.util.List;

public class Teacher {
    private String name;
    private String title;
    private List<String> directions;

    public Teacher(String name, String title, List<String> directions) {
        this.name = name;
        this.title = title;
        this.directions = directions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getDirections() {
        return directions;
    }

    public void setDirections(List<String> directions) {
        this.directions = directions;
    }


    public String toString() {
        return "name:" + name +
                " title:" + title +
                " directions:" + directions;
    }

}
