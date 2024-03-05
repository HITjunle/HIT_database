import java.util.Map;


class QueryOptimizer {
    private final Map<String, String[]> properties;

    public QueryOptimizer(Map<String, String[]> properties) {
        this.properties = properties;
    }

    public RelationalAlgebraOperator optimize(RelationalAlgebraOperator node) {
        if (node == null) {
            return null;
        }

        // 根据操作类型递归优化
        switch (node.type) {
            case SELECT:
                return optimizeSelection(node);
            default:
                return node;
        }
    }



    //TODO:投影优化，进行投影下推,在查询语句例子中，不了解外键，无法投影下推
    private RelationalAlgebraOperator optimizeProjection(RelationalAlgebraOperator node) {
        //* 优化子节点
        node.left = optimize(node.left);

        //*  如果子节点是 JOIN，将选择条件下推到 JOIN 的左右子节点
        if (node.left != null && node.left.type == OperatorType.JOIN) {
            // 分解 PROJECTION 条件
            String[] conditions = node.condition.split(",");
            for (String condition : conditions) {
                condition = condition.trim();
                // 分别下推到 JOIN 的左右子节点
                node.left.left = pushDownProjection(node.left.left, condition);
                node.left.right = pushDownProjection(node.left.right, condition);
            }
            //*pushDownProjection会添加新的节点，故移除原来的 PROJECTION 节点
            return node.left;
        }

        return node;
    }




    private RelationalAlgebraOperator pushDownProjection(RelationalAlgebraOperator node, String field) {
        if (node == null) {
            return null;
        }

        //*当找到 NODE 类型的节点时，检查并应用 PROJECTION
        if (node.type == OperatorType.NODE && isInProperties(field, node.condition)) {
            return new RelationalAlgebraOperator(OperatorType.PROJECTION, field, node, null);
        }

        return node;
    }

    //TODO:选择优化，进行选择下推
    private RelationalAlgebraOperator optimizeSelection(RelationalAlgebraOperator node) {
        // 查询优化子节点
        node.left = optimize(node.left);

        // 如果子节点是 JOIN，将选择条件下推到 JOIN 的左右子节点
        if (node.left != null && node.left.type == OperatorType.JOIN) {
            // 分解 SELECT 条件
            String[] conditions = node.condition.split("&");
            for (String condition : conditions) {
                condition = condition.trim();
                // 分别下推到 JOIN 的左右子节点
                node.left.left = pushDownSELECT(node.left.left, condition);
                node.left.right = pushDownSELECT(node.left.right, condition);
            }
            //keypoint:pushDownSELECT会添加新的节点，故移除原来的 SELECT 节点
            return node.left;
        }
        else if (node.left != null && node.left.type == OperatorType.PROJECTION) {
            // 如果 SELECT 节点的子节点是 PROJECTION，将 SELECT 下推到 PROJECTION 的子节点
            node.left.left = new RelationalAlgebraOperator(OperatorType.SELECT, node.condition, node.left.left, null);
            return node.left;
        }

        return node;
    }

    private RelationalAlgebraOperator pushDownSELECT(RelationalAlgebraOperator node, String condition) {
        //TODO:提取属性名字，也就是等于号左边的字符串
        String field = extractNameFromCondition(condition);
        if (node == null) {
            return null;
        }
        // 递归地处理 PROJECTION 节点
        if (node.type == OperatorType.PROJECTION) {
            node.left = pushDownSELECT(node.left, condition);
            return node;
        }
        // 检查节点关联的关系是否包含该字段
        if (node.type == OperatorType.NODE && isInProperties(field, node.condition)) {
            // 为合适的分支创建新的 SELECT 节点
            return new RelationalAlgebraOperator(OperatorType.SELECT, condition, node, null);
        }

        return node;
    }
    //TODO:提取属性名字，也就是等于号左边的字符串

    private String extractNameFromCondition(String condition) {
        int eqIndex = condition.indexOf('=');
        if (eqIndex != -1) {
            return condition.substring(0, eqIndex).trim();
        }
        return ""; // 或者抛出异常
    }

    //TODO:判断查询的条件是否和实体相对应
    private boolean isInProperties(String field, String relation) {
        if (properties.containsKey(relation)) {
            for (String availableField : properties.get(relation)) {
                if (availableField.equals(field)) {
                    return true;
                }
            }
        }
        return false;
    }




}
