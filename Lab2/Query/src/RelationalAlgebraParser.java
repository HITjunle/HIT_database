class RelationalAlgebraParser {
    public RelationalAlgebraOperator parse(String expression) {
        expression = expression.trim();//去除空格
		//*递归地分解 */
        if (expression.startsWith("SELECT")) {
            int start = expression.indexOf("[") + 1;
            int end = expression.indexOf("]");
            String condition = expression.substring(start, end).trim();
            String subExpr = expression.substring(end + 1).trim().replace("(", "").replace(")", "");
            return new RelationalAlgebraOperator(OperatorType.SELECT, condition, parse(subExpr), null);
        } else if (expression.startsWith("PROJECTION")) {
            int start = expression.indexOf("[") + 1;
            int end = expression.indexOf("]");
            String condition = expression.substring(start, end).trim();
            String subExpr = expression.substring(end + 1).trim().replace("(", "").replace(")", "");
            return new RelationalAlgebraOperator(OperatorType.PROJECTION, condition, parse(subExpr), null);
        } else if (expression.contains("JOIN")) {
            int joinIndex = expression.indexOf("JOIN");
            String leftExpr = expression.substring(0, joinIndex).trim();
            String rightExpr = expression.substring(joinIndex + 4).trim();
            return new RelationalAlgebraOperator(OperatorType.JOIN, null, parse(leftExpr), parse(rightExpr));
        }
        return new RelationalAlgebraOperator(OperatorType.NODE, expression, null, null);
    }
}
