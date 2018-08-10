package com.feike.mrp.support.jpa.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Conditions {

    private Logger logger = LoggerFactory.getLogger(Conditions.class);

    private List<Condition> cdList = new ArrayList<>();

    private String andOr;
    private Conditions cds;

    private Conditions() {
    }

    public static Conditions empty() {
        return new Conditions();
    }

    public static Conditions where() {
        Conditions conditions = new Conditions();
        conditions.andOr = "";
        return conditions;
    }

    public static Conditions where(String field) {
        Conditions root = where();
        root.cdList.add(new Condition("", field));
        return root;
    }

    public Conditions and(String field) {
        cdList.add(new Condition(cdList.isEmpty() ? "" : "and", field));
        return this;
    }

    public Conditions or(String field) {
        cdList.add(new Condition(cdList.isEmpty() ? "" : "or", field));
        return this;
    }

    public Conditions and(Conditions conditions) {
        this.andOr = "and";
        this.cds = conditions;
        return this;
    }

    public Conditions or(Conditions conditions) {
        this.andOr = "or";
        this.cds = conditions;
        return this;
    }

    private Condition lastCondition() {
        return this.cdList.get(cdList.size() - 1);
    }

    public Conditions is(Object value) {
        lastCondition().express("=").value(value);
        return this;
    }

    public Conditions isNull() {
        lastCondition().express("is null");
        return this;
    }

    public Conditions notNull() {
        lastCondition().express("is not null");
        return this;
    }

    public Conditions notIn(Object value) {
        lastCondition().express("not in").value(value);
        return this;
    }

    public Conditions like(Object value) {
        lastCondition().express("like").value(value);
        return this;
    }

    public Conditions gt(Object value) {
        lastCondition().express(">").value(value);
        return this;
    }

    public Conditions lt(Object value) {
        lastCondition().express("<").value(value);
        return this;
    }

    public Conditions gte(Object value) {
        lastCondition().express(">=").value(value);
        return this;
    }

    public Conditions lte(Object value) {
        lastCondition().express("<=").value(value);
        return this;
    }

    public Conditions ne(Object value) {
        lastCondition().express("<>").value(value);
        return this;
    }

    public Conditions in(Object value) {
        lastCondition().express("in").value(value);
        return this;
    }

    @Override
    public String toString() {
        return toQL(new HashMap<>());
    }

    public String toQL(Map<String, Object> params) {
        Assert.notNull(params, "参数对象不能为空");
        StringBuilder sb = new StringBuilder("");
        if (cdList == null || cdList.size() == 0) {
            return "";
        }
        sb.append(" ( ");
        for (Condition condition : cdList) {
            sb.append(condition.toQL(params));
        }

        if (cds != null) {
            sb.append(this.andOr);
            sb.append(cds.toQL(params));
        }

        sb.append(") ");

        String ql = sb.toString();
        logger.debug(ql);
        return ql;
    }

    private static class Condition {
        private String andOr;
        //查询字段
        private String field;
        //表达式
        private String express;
        //值
        private Object value;

        private Condition(String andOr, String field) {
            this.andOr = andOr;
            this.field = field;
        }

        private String toQL(Map<String, Object> params) {
            int index = 0;
            String paramsKey = field + "_" + index;
            while (params.containsKey(paramsKey)) {
                index++;
                paramsKey = field + "_" + index;
            }
            params.put(paramsKey, value);

            if (isEmpty(andOr)) {
                return field + " " + (express != null ? express : "") + (value != null ? " :" + paramsKey : "") + " ";
            } else {
                return andOr + " " + field + " " + (express != null ? express : "") + (value != null ? " :" + paramsKey : "") + " ";
            }
        }

        private Condition express(String express) {
            this.express = express;
            return this;
        }

        private Condition value(Object value) {
            this.value = value;
            return this;
        }
    }

}
