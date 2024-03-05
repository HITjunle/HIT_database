package entity;

public class Major {
    private String majorName;
    //所在专业的人数
    private int StudentNum;

    public Major(String majorName, int StudentNum) {
        this.majorName = majorName;
        this.StudentNum = StudentNum;
    }

    public int getStudentNum() {
        return StudentNum;
    }

    public String getMajorName() {
        return majorName;
    }
}
