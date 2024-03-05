package entity;

import operation.DBOperations;

public class Department {
    private int departmentID;
    private String departmentName;

    public String getDepartmentName() {
        return departmentName;
    }

    public Department(int departmentID, String departmentName) {
        this.departmentID = departmentID;
        this.departmentName = departmentName;
    }
    public int getDepartmentID() {
        return departmentID;
    }



}
