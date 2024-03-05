package entity;

public class Classroom {
    private int classroomID;
    private int capicity;

    public Classroom(int classroomID, int capicity) {
        this.classroomID = classroomID;
        this.capicity = capicity;
    }

    public int getCapicity() {
        return capicity;
    }

    public int getClassroomID() {
        return classroomID;
    }

}

