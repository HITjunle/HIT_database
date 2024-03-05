package entity;

public class Class {
    private int classID;

    private int HeadTeacherID;

    public int getHeadTeacher() {
        return HeadTeacherID;
    }

    public Class(int classID, int HeadTeacherID) {
        this.classID = classID;
        this.HeadTeacherID = HeadTeacherID;
    }

    public int getClassID() {
        return classID;
    }
}
