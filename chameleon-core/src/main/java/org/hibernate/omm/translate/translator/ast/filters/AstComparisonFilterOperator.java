

package org.hibernate.omm.translate.translator.ast.filters;

public enum AstComparisonFilterOperator {
    EQ("$eq"),
    GT("$gt"),
    GTE("$gte"),
    LT("$lt"),
    LTE("$lte"),
    NE("$ne");

    public String operatorName() {
        return operatorName;
    }

    private final String operatorName;

    AstComparisonFilterOperator(String operatorName) {
       this.operatorName = operatorName;
    }
}