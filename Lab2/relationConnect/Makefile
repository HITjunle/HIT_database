# 定义编译器
CC = gcc
JAVAC = javac
JAVA = java

# 定义编译选项
CFLAGS = -g
JFLAGS = -g

# 目标文件
TARGET_C = connect
TARGET_JAVA = Main

# 默认规则
all: $(TARGET_C) $(TARGET_JAVA)

# 构建规则
$(TARGET_C): connect.c extmem.c extmem.h
	$(CC) $(CFLAGS) -o $(TARGET_C) connect.c extmem.c

$(TARGET_JAVA): ../Query/src/Main.java ../Query/src/QueryOptimizer.java ../Query/src/RelationalAlgebraOperator.java ../Query/src/RelationalAlgebraParser.java
	$(JAVAC) $(JFLAGS) -d ./bin ../Query/src/Main.java ../Query/src/QueryOptimizer.java ../Query/src/RelationalAlgebraOperator.java ../Query/src/RelationalAlgebraParser.java

# 运行Java程序
query: $(TARGET_JAVA)
	$(JAVA) -cp ./bin $(TARGET_JAVA)

# 运行C程序
run_connect: $(TARGET_C)
	./$(TARGET_C)

# 清理规则
clean:
	rm -f *.o $(TARGET_C)
	rm -rf ./bin/*.class
