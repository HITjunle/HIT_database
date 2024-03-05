package entity;

import java.util.Date;

public class StudentIDCard {
    private int studentID;
    private Date IssueDate;
    private Date ExpirationDate;

    public StudentIDCard(int studentID, Date IssueDate, Date ExpirationDate) {
        this.studentID = studentID;
        this.IssueDate = IssueDate;
        this.ExpirationDate = ExpirationDate;
    }

    public Date getIssueDate() {
        return IssueDate;
    }

    public Date getExpirationDate() {
        return ExpirationDate;
    }

    public int getStudentID() {
        return studentID;
    }


}
