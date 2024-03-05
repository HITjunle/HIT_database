enum OperatorType {
    SELECT, PROJECTION, JOIN, NODE
}

class RelationalAlgebraOperator {
    OperatorType type;
    String condition;
    RelationalAlgebraOperator left;
    RelationalAlgebraOperator right;

    // 构造函数
    RelationalAlgebraOperator(OperatorType type, String condition, RelationalAlgebraOperator left, RelationalAlgebraOperator right) {
        this.type = type;
        this.condition = condition;
        this.left = left;
        this.right = right;
    }

    // 打印方法
    String print() {
        StringBuilder sb = new StringBuilder();
        switch (this.type) {
            case SELECT:
                sb.append("SELECT [ ").append(condition).append(" ]\n");
                sb.append("             |\n");
                sb.append(left.print());
                break;
            case PROJECTION:
                sb.append("PROJECTION [ ").append(condition).append(" ]\n");
                sb.append("             |\n");
                sb.append(left.print());
                break;
            case JOIN:
                String leftOutput = left.print();
                String rightOutput = right.print();
                //TODO:保证左右输出在同一行
                //分割输出为行：
                String[] leftLines = leftOutput.split("\n");
                String[] rightLines = rightOutput.split("\n");

                sb.append("            JOIN\n");
                sb.append("            /      \\\n");

                int maxLines = Math.max(leftLines.length, rightLines.length);
                for (int i = 0; i < maxLines; i++) {
                    String leftLine = i < leftLines.length ? leftLines[i] : "";
                    String rightLine = i < rightLines.length ? rightLines[i] : "";
                    sb.append(String.format("%15s   %s\n", leftLine, rightLine));
                }
                break;
            case NODE:
                sb.append("   ");
                sb.append(condition);
                //root时候
                if (this.left != null)
                    sb.append(left.print());
                break;
        }
        return sb.toString();
    }
}
