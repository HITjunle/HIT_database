import operation.DBOperations;

import java.sql.SQLException;

public class Main {
    public static final int naturalJoin = 0;
    public static final int leftJoin = 1;
    public static final int rightJoin = 2;
    public static void main(String[] args) throws SQLException {
        DBOperations dbOperations = new DBOperations();
        dbOperations.dropDatabase("School");
        // 创建数据库
        //keypoint:创建数据库的时候会初始化，插入一些数据
        dbOperations.createDatabase("School");
        //keypoint:打印学生表
        dbOperations.printTable("Student");
        dbOperations.printTable("Major");
        System.out.println();
        //TODO:尝试插入一条数据

        dbOperations.insertStudentData(new String[]{"10", "Jack Hug", "2136005",
                "3", "3", "Data Science"});
        //TODO:触发器，专业人数会自动更新
        dbOperations.printTable("Major");
        //keypoint:插入重复值，会打印 Error: Duplicate entry. Please check your data.
        dbOperations.insertStudentData(new String[]{"10", "Jack Hug",
                "2136005", "3", "3", "Data Science"});
        //keypoint:插入空值，会打印 Error: Null value is not allowed. Please check your data.
        dbOperations.insertStudentData(null);
        String[] columns = {"StudentID", "Name", "ClassID", "DepartmentID", "CourseID", "MajorName"};
        String[] values1 = {"1", "John Doe", "2136005", "1", "1", "Computer Science"};
        String[] values2 = {"1", "Jane Smith", "2136006", "2", "2", "Mathematics"};
        String[] values3 = {"2", "Alice Johnson", null, "1", "1", "Computer Science"};
        //keypoint:插入数据并使用事务管理。
        //keypoint:触发唯一键冲突异常
        dbOperations.insertDataWithTransaction("Student", columns, values1);
        dbOperations.insertDataWithTransaction("Student", columns, values2);
        //keypoint:触发空值异常
        dbOperations.insertDataWithTransaction("Student", columns, values3);
        //TODO:打印学生表看看变化
        dbOperations.printTable("Student");
        //TODO:尝试删除一条数据
        // 选择要删除的学生ID
        int studentIdToDelete = 10;
        //keypoint:不存在值删除：删除学生卡ID
        //keypoint：首先看看学生卡表
        dbOperations.printTable("StudentIDCard");
        dbOperations.deleteData("studentidcard", "StudentID", String.valueOf(studentIdToDelete));

        // 删除学生数据
        dbOperations.deleteData("Student", "StudentID", String.valueOf(studentIdToDelete));
        System.out.println("delete student with id " + studentIdToDelete);
        // 打印删除后的学生表
        dbOperations.printTable("Student");
        //keypoint:打印课程表
        dbOperations.printTable("Course");
        System.out.println();


        //TODO:自然连接,形成新的表SC表
        dbOperations.performDynamicJoinQuery("Student", "Course",
                "SC",naturalJoin);
        //keypoint:打印SC表
        dbOperations.printTable("SC");
        dbOperations.printTableColumnWithCondition("SC","*","CourseID=2");
        //TODO:嵌套查询
        //TODO:例子：从SC表中选择所有学生的信息，但是只选择那些选修了"Calculus I"课程的学生
        dbOperations.performNestedQuery();
        //TODO:分组查询
        //TODO:例子：来统计每门课程的学生人数，并且只返回学生人数大于等于1的课程
        dbOperations.performGroupByQueryWithHaving();
        //TODO:1
        dbOperations.createView("StudentView","SELECT * FROM Student WHERE StudentID > 1");
        //TODO:创建课程视图
        dbOperations.createView("CourseView","SELECT * FROM Course WHERE CourseID > 1");






    }


}