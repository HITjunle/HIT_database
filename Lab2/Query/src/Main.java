import java.io.UnsupportedEncodingException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        RelationalAlgebraParser parser = new RelationalAlgebraParser();
        Map<String,String[]> properties = Map.of(
                "EMPLOYEE", new String[]{"ENAME","BDATE"},
                "DEPARTMENT", new String[]{"DNAME"},
                "WORKS_ON", new String[]{"ESSN"},
                "PROJECT", new String[]{"PNAME"}
        );

        String[] expressions = {
                "SELECT [ ENAME = 'Mary' & DNAME = 'Research' ] ( EMPLOYEE JOIN DEPARTMENT )",
                "PROJECTION [ BDATE ] ( SELECT [ ENAME = 'John' & DNAME = 'Research' ] ( EMPLOYEE JOIN DEPARTMENT ) )",
                "SELECT [ ESSN = '01' ] ( PROJECTION [ ESSN , PNAME ] ( WORKS_ON JOIN PROJECT ) )"
        };
        //*打印查询执行树
        RelationalAlgebraOperator[] trees = new RelationalAlgebraOperator[expressions.length];
        int i = 0;
        for (String expression : expressions) {
            RelationalAlgebraOperator tree = parser.parse(expression);
            trees[i++] = tree;
            System.out.println(tree.print());

        }
		System.out.println("---------------------------------------------");
        //优化查询执行树
        System.out.println("print the optimized query execution tree:");
        System.out.println();
        QueryOptimizer optimizer = new QueryOptimizer(properties);
        for (int j = 0; j < trees.length; j++) {
            trees[j] = adjust(trees[j]);
            if (trees[j].left.type == OperatorType.JOIN){
                trees[j] = optimizer.optimize(trees[j]);
            }
            else{
                trees[j].left = optimizer.optimize(trees[j].left);
            }
            System.out.println(trees[j].print());

        }
    }


    //TODO:进行调整，将SELECT在PROJECTION下方
    static RelationalAlgebraOperator adjust(RelationalAlgebraOperator tree) {
        RelationalAlgebraOperator temp = tree.left;
        if (tree.type == OperatorType.SELECT && tree.left.type == OperatorType.PROJECTION) {
            tree.left = temp.left;
            temp.left = tree;
            return temp;
        }
        return tree;
    }




}
