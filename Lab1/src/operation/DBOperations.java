package operation;

import java.sql.*;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBOperations {
    private Connection connection;
    private String databaseName;

    public DBOperations() {
        this.connection = DBConnector.getConnection();
    }
    // 创建数据库
    // 创建数据库
    public void createDatabase(String databaseName) {
        Statement statement = null;
        this.databaseName = databaseName;
        try {
            // 创建Statement对象
            statement = connection.createStatement();

            // 检查数据库是否存在
            if (!databaseExists(databaseName)) {
                // 创建新数据库
                String createDatabaseQuery = "CREATE DATABASE " + databaseName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                statement.executeUpdate(createDatabaseQuery);

                System.out.println("Database '" + databaseName + "' created successfully.");

                // 使用新数据库
                connection.setCatalog(databaseName);

                // 创建表和初始化数据
                createTeacherTable();
                insertTeacherData();

                createDepartmentTable();
                insertDepartmentData();

                createMajorTable();
                insertMajorData();

                createClassroomTable();
                insertClassroomData();

                createCourseTable();
                insertCourseData();

                createClassTable();
                insertClassData();

                createStudentTable();
                insertStudentData();

                createStudentIDCardTable();
                insertStudentIDCardData();

            } else {
                System.out.println("Database '" + databaseName + "' already exists.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // 插入老师数据
    private void insertTeacherData() {
        String[] columns = {"TeacherID", "Name"};
        String[] values1 = {"1", "John Smith"};
        String[] values2 = {"2", "Jane Doe"};
        String[] values3 = {"3", "Anna Smith"};
        String[] values4 = {"4", "Peter Jones"};
        insertData("Teacher", columns, values1);
        insertData("Teacher", columns, values2);
        insertData("Teacher", columns, values3);
        insertData("Teacher", columns, values4);
    }

    // 插入系别数据
    private void insertDepartmentData() {
        String[] columns = {"DepartmentID", "Name"};
        String[] values1 = {"1", "Computer Science"};
        String[] values2 = {"2", "Mathematics"};
        String[] values3 = {"3", "Economics"};
        String[] values4 = {"4", "Physics"};
        insertData("Department", columns, values1);
        insertData("Department", columns, values2);
        insertData("Department", columns, values3);
        insertData("Department", columns, values4);
    }

    // 插入专业数据
    private void insertMajorData() {
        String[] columns = {"MajorName", "StudentNum"};
        String[] values1 = {"Computer Science", "100"};
        String[] values2 = {"Mathematics", "80"};
        String[] values3 = {"AI", "60"};
        String[] values4 = {"Data Science", "80"};
        String[] values5 = {"Chemistry", "100"};
        insertData("Major", columns, values1);
        insertData("Major", columns, values2);
        insertData("Major", columns, values3);
        insertData("Major", columns, values4);
        insertData("Major", columns, values5);
    }

    // 插入教室数据
    private void insertClassroomData() {
        String[] columns = {"ClassroomID", "capicity"};
        String[] values1 = {"101", "30"};
        String[] values2 = {"102", "25"};
        String[] values3 = {"103", "35"};
        String[] values4 = {"104", "45"};
        insertData("Classroom", columns, values1);
        insertData("Classroom", columns, values2);
        insertData("Classroom", columns, values3);
        insertData("Classroom", columns, values4);
    }

    // 插入课程数据
    public void insertCourseData(String[] values) {
        String[] columns = {"CourseID", "CourseName", "TeacherID", "ClassroomID"};
        insertData("Course", columns, values);
    }
    private void insertCourseData() {
        String[] columns = {"CourseID", "CourseName", "TeacherID", "ClassroomID"};
        String[] values1 = {"1", "Programming", "1", "101"};
        String[] values2 = {"2", "Calculus I", "2", "102"};
        String[] values3 = {"3", "Calculus II", "3", "103"};
        String[] values4 = {"4", "Linear Algebra", "4", "104"};
        insertData("Course", columns, values1);
        insertData("Course", columns, values2);
        insertData("Course", columns, values3);
        insertData("Course", columns, values4);
    }

    // 插入班级数据
    private void insertClassData() {
        String[] columns = {"ClassID", "HeadTeacherID"};
        String[] values1 = {"2136005", "1"};
        String[] values2 = {"2136006", "2"};
        insertData("Class", columns, values1);
        insertData("Class", columns, values2);
    }

    // 插入学生数据
    public void insertStudentData(String[] values) throws SQLException {
        String[] columns = {"StudentID", "Name", "ClassID", "DepartmentID", "CourseID", "MajorName"};
        if (values == null) {
            System.out.println("Error: Null value is not allowed. Please check your data.");
            return;
        }
        insertData("Student", columns, values);

        //keypoint:更新专业人数
        String MajorName = values[5];
        String updateMajorQuery = "UPDATE Major SET StudentNum = StudentNum + 1 WHERE MajorName = '" + MajorName + "'";

        String selectDatabaseQuery = "USE " + databaseName;
        Statement statement = connection.createStatement();
        statement.executeUpdate(selectDatabaseQuery);
        statement.executeUpdate(updateMajorQuery);
    }

    private void insertStudentData() {
        String[] columns = {"StudentID", "Name", "ClassID", "DepartmentID", "CourseID", "MajorName"};

        String[] values1 = {"1", "Alice Johnson", "2136005", "1", "1", "Computer Science"};
        String[] values2 = {"2", "Bob Williams", "2136005", "2", "2", "Mathematics"};
        String[] values3 = {"3", "Charlie Smith", "2136005", "2", "2", "AI"};
        String[] values4 = {"4", "David Jones", "2136006", "4", "4", "Chemistry"};
        String[] values5 = {"5", "Emma Brown", "2136006", "3", "3", "AI"};
        String[] values6 = {"6", "Grace Davis", "2136006", "3", "3", "AI"};
        String[] values7 = {"7", "Helen Miller", "2136006", "3", "3", "Computer Science"};
        String[] values8 = {"8", "Isabella Wilson", "2136006", "3", "3", "Data Science"};
        String[] values9 = {"9", "Jack Taylor", "2136006", "3", "3", "Data Science"};
        insertData("Student", columns, values1);
        insertData("Student", columns, values2);
        insertData("Student", columns, values3);
        insertData("Student", columns, values4);
        insertData("Student", columns, values5);
        insertData("Student", columns, values6);
        insertData("Student", columns, values7);
        insertData("Student", columns, values8);
        insertData("Student", columns, values9);
    }

    // 插入学生证数据
    private void insertStudentIDCardData() {
        String[] columns = {"StudentID", "IssueDate", "ExpirationDate"};
        String[] values1 = {"1", "2023-01-01", "2024-01-01"};
        String[] values2 = {"2", "2023-02-01", "2024-02-01"};
        insertData("StudentIDCard", columns, values1);
        insertData("StudentIDCard", columns, values2);
    }


    // 检查数据库是否存在
    private boolean databaseExists(String databaseName) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet resultSet = meta.getCatalogs();

            while (resultSet.next()) {
                String existingDatabaseName = resultSet.getString(1);
                if (existingDatabaseName.equalsIgnoreCase(databaseName)) {
                    return true;
                }
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }



    // 插入数据，考虑空值和重复值的情况
    public void insertData(String tableName, String[] columns, String[] values) {
        try {

            String selectDatabaseQuery = "USE "+ databaseName;
            Statement statement = connection.createStatement();
            statement.executeUpdate(selectDatabaseQuery);
            // 构建插入语句
            String columnsStr = String.join(", ", columns);
            String valuesStr = String.join(", ", Collections.nCopies(values.length, "?")); // 用?替代值

            String query = "INSERT INTO " + tableName + " (" + columnsStr + ") VALUES (" + valuesStr + ")";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // 设置参数值
            for (int i = 0; i < values.length; i++) {
                preparedStatement.setString(i + 1, values[i]);
            }

            preparedStatement.executeUpdate();
            System.out.println("Data inserted successfully.");



        } catch (SQLException e) {
            // 处理插入异常，例如空值或重复值
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("Error: Duplicate entry. Please check your data.");
            } else if (e.getMessage().contains("cannot be null")) {
                System.out.println("Error: Null value is not allowed. Please check your data.");
            } else {
                e.printStackTrace();
            }
        }
    }


    // 删除数据，考虑空值和不存在信息的情况
    public void deleteData(String tableName, String conditionColumn, String conditionValue) {
        try {
            String selectDatabaseQuery = "USE "+ databaseName;
            Statement statement = connection.createStatement();
            statement.executeUpdate(selectDatabaseQuery);

            // 构建删除语句
            String query = "DELETE FROM " + tableName + " WHERE " + conditionColumn + " = ?";

            // 执行删除
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, conditionValue);
            int rowsAffected = preparedStatement.executeUpdate();

            // 处理删除结果
            if (rowsAffected > 0) {
                System.out.println("Data deleted successfully.");
            } else {
                System.out.println("Error: Data not found for deletion.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //连接查询
    public void performDynamicJoinQuery(String firstTable, String secondTable, String newTableName,int mode) {
        try {
            String query = null;
            // 自然连接
            if (mode == 0)
                query = "CREATE TABLE "+ newTableName +" AS SELECT * FROM " + firstTable + " NATURAL JOIN " + secondTable;
            // 左外连接
            if (mode == 1)
                query = "CREATE TABLE "+ newTableName +" AS SELECT Student.StudentID, Student.Name, Student.ClassID, " +
                        "Student.DepartmentID, Student.CourseID, Student.MajorName, Course.CourseName, Course.TeacherID, " +
                        "Course.ClassroomID FROM " + firstTable +
                        " LEFT JOIN " + secondTable + " ON " + firstTable + ".CourseID = " + secondTable + ".CourseID";
            //右外连接
            if (mode == 2)
                query = "CREATE TABLE "+ newTableName +" AS SELECT Student.StudentID, Student.Name, Student.ClassID, " +
                        "Student.DepartmentID, Student.CourseID, Student.MajorName, Course.CourseName, Course.TeacherID, " +
                        "Course.ClassroomID FROM " + firstTable +
                        " RIGHT JOIN " + secondTable + " ON " + firstTable + ".CourseID = " + secondTable + ".CourseID";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // 嵌套查询
    public void performNestedQuery() {
        // 构建嵌套查询语句
        String query = "SELECT * FROM Student WHERE CourseID IN (SELECT CourseID FROM Course WHERE CourseName = 'Calculus I')";

        // 执行查询并打印结果
        printTableColumnWithCondition("SC", "*", "CourseID IN (SELECT CourseID FROM" +
                " Course WHERE CourseName = 'Calculus I')");
    }


    // 分组查询（带有HAVING语句）
    public void performGroupByQueryWithHaving() throws SQLException {

        // 构建带有HAVING子句的分组查询语句
        String query = "SELECT CourseID, COUNT(*) FROM SC GROUP BY CourseID HAVING COUNT(*) > 1";
        String selectDatabaseQuery = "USE " + databaseName;
        Statement selectStatement = connection.createStatement();
        selectStatement.executeUpdate(selectDatabaseQuery);

        Statement statement = connection.createStatement();
        System.out.println(query);
        ResultSet resultSet = statement.executeQuery(query);

        // 获取元数据
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 打印表头
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("| %-15s", metaData.getColumnName(i));
        }
        System.out.println("|");

        // 打印分隔线
        for (int i = 1; i <= columnCount; i++) {
            System.out.print("+---------------");
        }
        System.out.println("+");

        // 打印数据
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("| %-15s", resultSet.getString(i));
            }
            System.out.println("|");
        }

    }
    //创建视图
    public void createView(String viewName, String query) {
        try {
            Statement statement = connection.createStatement();
            String createViewQuery = "CREATE VIEW " + viewName + " AS " + query;
            statement.executeUpdate(createViewQuery);
            Pattern pattern = Pattern.compile("WHERE\\s+(.*)");
            Matcher matcher = pattern.matcher(createViewQuery);
            String condition = null;
            if (matcher.find()) {
                condition = matcher.group(1);
            }
            System.out.println("View " + viewName + " created successfully.");
            System.out.println(createViewQuery);
            printTableColumnWithCondition(viewName, "*", condition);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    // 创建学生表
    public void createStudentTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createStudentTableQuery = "CREATE TABLE IF NOT EXISTS Student (" +
                    "StudentID INT PRIMARY KEY," +
                    "Name VARCHAR(255) NOT NULL," +
                    "ClassID INT," +
                    "DepartmentID INT," +
                    "CourseID INT," +
                    "MajorName VARCHAR(255)," +
                    "FOREIGN KEY (ClassID) REFERENCES Class(ClassID)," +
                    "FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID)," +
                    "FOREIGN KEY (MajorName) REFERENCES Major(MajorName)," +
                    "FOREIGN KEY (CourseID) REFERENCES Course(CourseID)" +
                    ")";
            statement.executeUpdate(createStudentTableQuery);

            System.out.println("Student table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建课程表
    public void createCourseTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createCourseTableQuery = "CREATE TABLE IF NOT EXISTS Course (" +
                    "CourseID INT PRIMARY KEY," +
                    "CourseName VARCHAR(255) NOT NULL," +
                    "TeacherID INT," +
                    "ClassroomID INT," +
                    "FOREIGN KEY (TeacherID) REFERENCES Teacher(TeacherID)," +
                    "FOREIGN KEY (ClassroomID) REFERENCES Classroom(ClassroomID)" +
                    ")";
            statement.executeUpdate(createCourseTableQuery);

            System.out.println("Course table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建老师表
    public void createTeacherTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createTeacherTableQuery = "CREATE TABLE IF NOT EXISTS Teacher (" +
                    "TeacherID INT PRIMARY KEY," +
                    "Name VARCHAR(255) NOT NULL" +
                    ")";
            statement.executeUpdate(createTeacherTableQuery);

            System.out.println("Teacher table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建学生证表
    public void createStudentIDCardTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createStudentIDCardTableQuery = "CREATE TABLE IF NOT EXISTS StudentIDCard (" +
                    "StudentID INT PRIMARY KEY," +
                    "IssueDate DATE," +
                    "ExpirationDate DATE," +
                    "FOREIGN KEY (StudentID) REFERENCES Student(StudentID)" +
                    ")";
            statement.executeUpdate(createStudentIDCardTableQuery);

            System.out.println("StudentIDCard table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建班级表
    public void createClassTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createClassTableQuery = "CREATE TABLE IF NOT EXISTS Class (" +
                    "ClassID INT PRIMARY KEY," +
                    "HeadTeacherID INT," +
                    "FOREIGN KEY (HeadTeacherID) REFERENCES Teacher(TeacherID)" +
                    ")";
            statement.executeUpdate(createClassTableQuery);

            System.out.println("Class table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建系别表
    public void createDepartmentTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createDepartmentTableQuery = "CREATE TABLE IF NOT EXISTS Department (" +
                    "DepartmentID INT PRIMARY KEY," +
                    "Name VARCHAR(255) NOT NULL" +  // 添加Name属性
                    ")";

            statement.executeUpdate(createDepartmentTableQuery);

            System.out.println("Department table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建专业表
    public void createMajorTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createMajorTableQuery = "CREATE TABLE IF NOT EXISTS Major (" +
                    "MajorName VARCHAR(255) PRIMARY KEY," +
                    "StudentNum INT" +
                    ")";
            statement.executeUpdate(createMajorTableQuery);

            System.out.println("Major table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建教室表
    public void createClassroomTable() {
        Statement statement = null;
        try {
            statement = connection.createStatement();

            String createClassroomTableQuery = "CREATE TABLE IF NOT EXISTS Classroom (" +
                    "ClassroomID INT PRIMARY KEY," +
                    "capicity INT " +
                    ")";
            statement.executeUpdate(createClassroomTableQuery);

            System.out.println("Classroom table created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void dropDatabase(String databaseName) {
        Statement statement = null;

        try {
            // 关闭连接到数据库的连接


            // 创建Statement对象
            statement = connection.createStatement();

            // 删除数据库
            String dropDatabaseQuery = "DROP DATABASE IF EXISTS " + databaseName;
            statement.executeUpdate(dropDatabaseQuery);

            System.out.println("Database '" + databaseName + "' dropped successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 其他资源清理操作
        }

    }
    // 打印学生表
    public void printTable(String tableName) {
        try {
            // 选择正确的数据库
            String selectDatabaseQuery = "USE " + databaseName;
            Statement selectStatement = connection.createStatement();
            selectStatement.executeUpdate(selectDatabaseQuery);

            Statement statement = connection.createStatement();
            String query = "SELECT * FROM "+tableName;
            ResultSet resultSet = statement.executeQuery(query);

            // 打印表头
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 打印表头，并在列之间添加竖线
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("| %-15s", metaData.getColumnName(i));
            }
            System.out.println("|");

            // 打印分隔线
            for (int i = 1; i <= columnCount; i++) {
                System.out.print("+---------------");
            }
            System.out.println("+");

            // 打印数据，并在列之间添加竖线
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.printf("| %-15s", resultSet.getString(i));
                }
                System.out.println("|");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   //查询并打印
   public void printTableColumnWithCondition(String tableName, String columnName, String condition) {
       try {
           System.out.println("start query "+condition+" in "+tableName);
           // 选择正确的数据库
           String selectDatabaseQuery = "USE " + databaseName;
           Statement selectStatement = connection.createStatement();
           selectStatement.executeUpdate(selectDatabaseQuery);

           Statement statement = connection.createStatement();
           String query;
           if (columnName.equals("*")) {
               query = "SELECT * FROM " + tableName + " WHERE " + condition;
           } else {
               query = "SELECT " + columnName + " FROM " + tableName + " WHERE " + condition;
           }
           ResultSet resultSet = statement.executeQuery(query);

           // 获取元数据
           ResultSetMetaData metaData = resultSet.getMetaData();
           int columnCount = metaData.getColumnCount();

           // 打印表头
           for (int i = 1; i <= columnCount; i++) {
               System.out.printf("| %-15s", metaData.getColumnName(i));
           }
           System.out.println("|");

           // 打印分隔线
           for (int i = 1; i <= columnCount; i++) {
               System.out.print("+---------------");
           }
           System.out.println("+");

           // 打印数据
           while (resultSet.next()) {
               for (int i = 1; i <= columnCount; i++) {
                   System.out.printf("| %-15s", resultSet.getString(i));
               }
               System.out.println("|");
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
   }

    // 使用事务插入数据
    /**
     * 插入数据并使用事务管理。
     *
     * @param tableName 表名
     * @param columns   要插入的列名数组
     * @param values    要插入的值数组
     */
    public void insertDataWithTransaction(String tableName, String[] columns, String[] values) {
        try {
            connection.setAutoCommit(false); // 禁用自动提交，开始事务

            // 插入数据
            String selectDatabaseQuery = "USE "+ databaseName;
            Statement statement = connection.createStatement();
            statement.executeUpdate(selectDatabaseQuery);
            // 构建插入语句
            String columnsStr = String.join(", ", columns);
            String valuesStr = String.join(", ", Collections.nCopies(values.length, "?")); // 用?替代值

            String query = "INSERT INTO " + tableName + " (" + columnsStr + ") VALUES (" + valuesStr + ")";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // 设置参数值
            for (int i = 0; i < values.length; i++) {
                preparedStatement.setString(i + 1, values[i]);
            }

            preparedStatement.executeUpdate();
            System.out.println("Data inserted successfully.");

            // 如果没有异常，提交事务
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback(); // 回滚事务
                System.out.println("Transaction rolled back.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // 处理异常
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("Error: Duplicate entry. Please check your data.");
            } else if (e.getMessage().contains("cannot be null")) {
                System.out.println("Error: Null value is not allowed. Please check your data.");
            } else {
                e.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true); // 恢复自动提交
                if (!connection.getAutoCommit()) {
                    connection.commit(); // 确保自动提交已经开启，以免影响后续操作
                }
                System.out.println("Transaction committed successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }






}
