package com.scistor.parser.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOrderingExpr;
//import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
//import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.column.ScistorSelectColumn;
import com.scistor.parser.column.ScistorTextColumn;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.result.ScistorColumnResult;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.table.ScistorTable;

/**
 * 解析列
 * @author GuoLiang
 *
 */
public abstract class ScistorMysqlConditionParser extends ScistorMysqlParser{
	protected int selfDefinedSubAliasID = 0;
	protected static final String sefDefindSubAlias = "Scistor";
	protected ScistorColumnResult result;
	public ScistorMysqlConditionParser(SQLStatement statement) {
		super(statement);
	}

	@Override
	public  abstract ScistorResult getParseResult() throws ScistorParserException;
	
	/*
	 * 获取select列 
	 */
	protected List<ScistorSelectColumn> getSelectColumns(MySqlSelectQueryBlock queryBlock) throws ScistorParserException{
		List<ScistorSelectColumn> inselectedColumns = new ArrayList<ScistorSelectColumn>();
		List<SQLSelectItem> items = queryBlock.getSelectList();
		parseSelectItems(items, inselectedColumns);
		return inselectedColumns;
	}
	
	/*
	 * 获取所有 出了join on 以外的 条件字段
	 */
	protected List<ScistorColumn> getAllConditionColumnExceptJoinOn(MySqlSelectQueryBlock queryBlock,
			List<SQLSelectQuery> subQuerys) throws ScistorParserException{
		List<ScistorColumn> inwhereColumns = new ArrayList<ScistorColumn>();
		SQLExpr where = queryBlock.getWhere();
		parseWhere(where,inwhereColumns, subQuerys);
		
		SQLSelectGroupByClause groupby = queryBlock.getGroupBy();
		parseGroupBy(groupby, inwhereColumns);
		
		SQLOrderBy orderby = queryBlock.getOrderBy();
		parseOrderBy(orderby, inwhereColumns);
		
		for(ScistorColumn column : inwhereColumns){
			column.setIsWhere(true);
		}
		
		return inwhereColumns;
	}
	
	/*
	 * group by 和  order by 中可以使用select 中的列 别名，用select真实的列名替换 别名
	 * 用select中的字段 替换 where中的别名 
	 */
	protected void replaceWhereCNameWithSelectCAliasName(List<ScistorColumn> whereColumns,
				List<ScistorSelectColumn> selectedColumns) throws ScistorParserException{
		for(ScistorColumn column : whereColumns){
			for(ScistorSelectColumn scolumn : selectedColumns){
				if(scolumn.getAlias()!=null){
					if(scolumn.getAlias().equals(column.getName())){
						column.setName(scolumn.getName());
					}
				}
			}
		}
	}
	
	/*
	 * 解析 select 列元素
	 */
	protected void parseSelectItems(List<SQLSelectItem> items,List<ScistorSelectColumn> selectedColumns) throws ScistorParserException{
		if(items==null||items.isEmpty()) return;
		for(int i = 0;i<items.size();i++){
			SQLSelectItem item = items.get(i);
			ArrayList<ScistorSelectColumn> subColumns = new ArrayList<ScistorSelectColumn>();
			String alias = item.getAlias();
			ScistorSelectColumn column = new ScistorSelectColumn();
			column.setAlias(alias);
			subColumns.add(column);
			SQLExpr expr = item.getExpr();
			if(expr instanceof SQLNumericLiteralExpr == false){
				//modify by cx 20171217:if条件改为相反的，去除异常，新增代码段
				//throw new ScistorParserException("SQL ERROR : "+expr.toString()+" is not supported syntax in select condition.");
				parseSelectExpr(expr, subColumns);
				for(int j=0;j<subColumns.size();j++){
					ScistorSelectColumn subColumn = subColumns.get(j);
					if(subColumn.getName() != null){
						selectedColumns.add(subColumn);
					}
				}
 			}
		}
	}
	
	/*
	 * 解析 select 列类型
	 */
	protected void parseSelectExpr(SQLExpr expr,List<ScistorSelectColumn> subColumns) throws ScistorParserException{
		int index = subColumns.size()-1;
		ScistorSelectColumn column = subColumns.get(index);
		if(expr instanceof SQLAllColumnExpr){
			column.setName("*");
		}else if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			column.setName(e.getName());
		}else if(expr instanceof SQLPropertyExpr){
			SQLPropertyExpr e = (SQLPropertyExpr)expr;
			column.setName(e.getName());
			column.setOwner(e.getOwner().toString());
		}else if(expr instanceof SQLAggregateExpr){
			//当为聚合函数的时候
			SQLAggregateExpr e = (SQLAggregateExpr)expr;
			column.setAggregator(true);
			
			SQLExpr aexpr = e.getArguments().get(0);
			if(aexpr instanceof SQLNumericLiteralExpr||aexpr instanceof SQLIntegerExpr) {
				//throw new ScistorParserException("SQL ERROR : "+aexpr.toString()+" is not supported syntax in "+e.getMethodName());
			    return;
			}
			parseSelectExpr(aexpr, subColumns);
			
			if(e.getOver() != null){
				if(e.getOver().getOrderBy()!=null){
					SQLOrderBy orderBy = (SQLOrderBy) e.getOver().getOrderBy();
					List<SQLSelectOrderByItem> items = orderBy.getItems();
					for(int i=0;i<items.size();i++){
						if(i>0){
							subColumns.add(new ScistorSelectColumn());
						}
						parseSelectExpr(items.get(i).getExpr(),subColumns);
					}

				}
				
				ArrayList<SQLExpr> partitionByList = (ArrayList<SQLExpr>) e.getOver().getPartitionBy();
				for(int i = 0;i<partitionByList.size();i++){
					if(i>0){
						subColumns.add(new ScistorSelectColumn());
					}
					parseSelectExpr(partitionByList.get(i),subColumns);
				}
			}
			
		}else if(expr instanceof SQLMethodInvokeExpr){
			SQLMethodInvokeExpr e = (SQLMethodInvokeExpr) expr;
			//TODO 方法判断,暂时不跑出异常。
			//不包含聚合函数和分析函数
			if(e.getMethodName().toUpperCase().equals("DECODE")||
					e.getMethodName().toUpperCase().equals("NVL")||
					e.getMethodName().toUpperCase().equals("NVL2")||
					e.getMethodName().toUpperCase().equals("TRIM")||
					e.getMethodName().toUpperCase().equals("SUBSTR")||
					e.getMethodName().toUpperCase().equals("INSTR")||
					e.getMethodName().toUpperCase().equals("REPLACE")||
					e.getMethodName().toUpperCase().equals("LENGTH")||
					e.getMethodName().toUpperCase().equals("LPAD")||
					e.getMethodName().toUpperCase().equals("RPAD")||
					e.getMethodName().toUpperCase().equals("TO_DATE")||
					e.getMethodName().toUpperCase().equals("TO_CHAR")){
				List<SQLExpr> exprList = (List<SQLExpr>)e.getParameters();
				for(int i=0;i<exprList.size();i++){
					SQLExpr subExpr = exprList.get(i);
					if(i>0){
						subColumns.add(new ScistorSelectColumn());
					}
					parseSelectExpr(subExpr,subColumns);
				} 
			}
			//throw new ScistorParserException("SQL ERROR : function '"+e.getMethodName()+"' is not supported in select condition");
		}else if(expr instanceof SQLIntegerExpr||expr instanceof SQLNumericLiteralExpr){
			return;
		}else if(expr instanceof SQLBinaryOpExpr){
			SQLBinaryOpExpr e = (SQLBinaryOpExpr) expr;
			parseSelectExpr(e.getRight(),subColumns);
			subColumns.add(new ScistorSelectColumn());
			parseSelectExpr(e.getLeft(),subColumns);
			//delete by cx 20171217
			//throw new ScistorParserException("SQL ERROR : operator '"+e.getOperator().getName()+"' is not supported in select condition");
		}
	}
	
	/*
	 * 解析where条件中的字段
	 */
	protected void parseWhere(SQLExpr where,List<ScistorColumn> whereColumns,List<SQLSelectQuery> subQuerys) throws ScistorParserException{
		if(where==null) return;
		if (where instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr) where;
			String op = expr.getOperator().getName();
			if(op.equals("AND")||op.equals("OR")||!isValueOperator(op)){
				parseWhere(expr.getLeft(), whereColumns,subQuerys);
				parseWhere(expr.getRight(), whereColumns,subQuerys);
			}else{
				parseWhereBinaryExpr(expr,whereColumns);
			}
		} else if(where instanceof SQLIdentifierExpr){
			SQLIdentifierExpr expr = (SQLIdentifierExpr)where;
			String name = expr.getName();
			ScistorColumn column = new ScistorColumn(name);
			whereColumns.add(column);
		} else if(where instanceof SQLPropertyExpr){
			SQLPropertyExpr expr = (SQLPropertyExpr) where;
			String owner = expr.getOwner().toString();
			String columnName = expr.getName();
			ScistorColumn column = new ScistorColumn(owner,columnName);
			whereColumns.add(column);
		} else if (where instanceof SQLInListExpr) {
			SQLInListExpr expr = (SQLInListExpr) where;
			parseWhereInListExpr(expr,whereColumns);
		} else if (where instanceof SQLExistsExpr) {
			SQLExistsExpr expr = (SQLExistsExpr) where;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
		} else if (where instanceof SQLNotExpr) {
			SQLNotExpr expr = (SQLNotExpr) where;
			parseWhere(expr.getExpr(),whereColumns,subQuerys);
		} else if (where instanceof SQLBetweenExpr) {
			SQLBetweenExpr expr = (SQLBetweenExpr) where;
			parseWhereBetweenExpr(expr,whereColumns);
		} else if (where instanceof SQLInSubQueryExpr){
			SQLInSubQueryExpr expr  = (SQLInSubQueryExpr) where;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
			parseWhere(expr.getExpr(),whereColumns, null);
		} else if (where instanceof SQLAggregateExpr) {
			SQLAggregateExpr expr = (SQLAggregateExpr) where;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' can not be used in where conditon");
		} else if (where instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr) where;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' is not supported in where conditon");
		} 
	}
	/*
	 * 当出现    cname='123' 类型的条件查询时
	 */
	protected void parseWhereBinaryExpr(SQLBinaryOpExpr expr,List<ScistorColumn> whereColumns) throws ScistorParserException{
		String op = expr.getOperator().getName();
		SQLExpr left = expr.getLeft();
		SQLExpr right = expr.getRight();
		if((left instanceof SQLIdentifierExpr || left instanceof SQLPropertyExpr)
				&&(right instanceof SQLTextLiteralExpr)){
			ScistorTextColumn column = new ScistorTextColumn();
			column.addExpr(right);
			if(op.equals("LIKE")||op.equals("NOT LIKE")){
				column.setLike(true);
			}else if(op.equals("REGEXP")||op.equals("NOT REGEXP")){
				column.setRegex(true);
			}
			parseWhereTextExpr(left, column);
			whereColumns.add(column);
		}else{
			parseWhere(left,whereColumns, null);
			parseWhere(right, whereColumns,null);
		}
	}
	
	/*
	 * 当出现  cname in (value1,value2); mysql中 value1和value2的数据类型可以不一样
	 */
	protected void parseWhereInListExpr(SQLInListExpr expr,List<ScistorColumn> whereColumns) throws ScistorParserException{
		List<SQLExpr> lists = expr.getTargetList();
		SQLExpr co = expr.getExpr();
		boolean hasTextValue = false;
		for(SQLExpr ee : lists){
			if(ee instanceof SQLTextLiteralExpr){
				hasTextValue = true;
				break;
			}
		}
		if(hasTextValue){
			ScistorTextColumn column = new ScistorTextColumn();
			parseWhereTextExpr(co, column);
			for(SQLExpr ee : lists){
				if(ee instanceof SQLTextLiteralExpr){
					column.addExpr(ee);
				}
			}
			whereColumns.add(column);
		}else{
			parseWhere(co,whereColumns, null);
		}
	}
	
	/*
	 * 解析between... and ...
	 */
	protected void parseWhereBetweenExpr(SQLBetweenExpr expr,List<ScistorColumn> whereColumns) throws ScistorParserException{
		SQLExpr columnexpr = expr.getTestExpr();
		SQLExpr begin = expr.getBeginExpr();
		SQLExpr end = expr.getEndExpr();
		if((begin instanceof SQLTextLiteralExpr)&&(end instanceof SQLNumericLiteralExpr)){
			throw new ScistorParserException("SQL ERROR: between syntax "+begin.toString()+" and "+end.toString()+" is not same data type");
		}
		if((end instanceof SQLTextLiteralExpr)&&(begin instanceof SQLNumericLiteralExpr)){
			throw new ScistorParserException("SQL ERROR: between syntax "+begin.toString()+" and "+end.toString()+" is not same data type");
		}
		if((begin instanceof SQLTextLiteralExpr) && (end instanceof SQLTextLiteralExpr)){
			ScistorTextColumn column = new ScistorTextColumn();
			column.addExpr(begin);
			column.addExpr(end);
			parseWhereTextExpr(columnexpr, column);
			whereColumns.add(column);
		}else{
			parseWhere(columnexpr,whereColumns, null);
		}
	}
	
	/*
	 * 解析where中含有 字符类型数据 的列
	 */
	protected void parseWhereTextExpr(SQLExpr expr,ScistorColumn column) throws ScistorParserException{
		if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			column.setName(e.getName());
		} else if(expr instanceof SQLPropertyExpr){
			SQLPropertyExpr e = (SQLPropertyExpr)expr;
			column.setName(e.getName());
			column.setOwner(e.getOwner().toString());
		} else if(expr instanceof SQLMethodInvokeExpr){
			SQLMethodInvokeExpr e = (SQLMethodInvokeExpr) expr;
			throw new ScistorParserException("SQL ERROR : function '"+e.getMethodName()+"' is not supported within char type column");
		} else if(expr instanceof SQLBinaryOpExpr){
			SQLBinaryOpExpr e = (SQLBinaryOpExpr) expr;
			throw new ScistorParserException("SQL ERROR : operator '"+e.getOperator().getName()+"' is not supported within char type column");
		} else if (expr instanceof SQLNotExpr) {
			SQLNotExpr e = (SQLNotExpr) expr;
			parseWhereTextExpr(e.getExpr(), column);
		} else if (expr instanceof SQLAggregateExpr) {
			SQLAggregateExpr e = (SQLAggregateExpr) expr;
			throw new ScistorParserException("SQL ERROR:'"+e.getMethodName()+"' is not supported within char type column");
		} 
	}
	
	protected boolean isValueOperator(String op){
		if(op.equals("=")
				||op.equals("!=")
				||op.equals("<>")
				||op.equals(">")
				||op.equals(">=")
				||op.equals("!>")
				||op.equals("<")
				||op.equals("<=")
				||op.equals("!<")
				||op.equals("<=>")
				||op.equals("<>")
				||op.equals("LIKE")
				||op.equals("NOT LIKE")
				||op.equals("REGEXP")
				||op.equals("NOT REGEXP")){
			return true;
		}
		return false;
	}
	
	/**
	 * 分析join的情况
	 * @param tableSource
	 * @param subQuerys
	 * @param tables
	 * @param onColumns 
	 * @throws ScistorParserException
	 */
	protected void parseJoinTableSource(SQLTableSource tableSource,Map<String,SQLSelectQuery> subQuerys,
			List<ScistorTable> tables, List<ScistorColumn> onColumns) throws ScistorParserException {
		if(tableSource instanceof SQLExprTableSource){
			SQLExprTableSource expr = (SQLExprTableSource) tableSource;
			SQLExpr tableExpr = expr.getExpr();
			if(!(tableExpr instanceof SQLIdentifierExpr)){
				throw new ScistorParserException("SQL ERROR : not supported table syntax "+tableExpr.toString());
			}
			String tablename = tableExpr.toString();
			String tablealias = expr.getAlias();
			ScistorTable table = new ScistorTable(tablename, tablealias);
			tables.add(table);
		}else if(tableSource instanceof SQLJoinTableSource){
			SQLJoinTableSource expr = (SQLJoinTableSource) tableSource;
			SQLTableSource left = expr.getLeft();
			SQLTableSource right = expr.getRight();
			parseJoinOnCondition(expr.getCondition(), onColumns);
			parseJoinUsingCondition(expr.getUsing(),onColumns);
			parseJoinTableSource(left,subQuerys,tables,onColumns);
			parseJoinTableSource(right,subQuerys,tables,onColumns);
		}else if(tableSource instanceof SQLSubqueryTableSource){
			SQLSubqueryTableSource expr = (SQLSubqueryTableSource) tableSource;
			subQuerys.put(expr.getAlias(), expr.getSelect().getQuery());//SQLSelectQuery
		}else if(tableSource instanceof SQLUnionQueryTableSource){
			SQLUnionQueryTableSource expr = (SQLUnionQueryTableSource) tableSource;
			subQuerys.put(expr.getAlias(), expr.getUnion());//SQLUnionQuery
		}else {
			throw new ScistorParserException("SQL ERROR : '"+tableSource.getClass().toString().substring(tableSource.getClass().toString().lastIndexOf("."))+"' is not supported");
		}
	}
	
	protected void parseJoinOnCondition(SQLExpr onCondition,List<ScistorColumn> onColumns) throws ScistorParserException{
		if(onCondition == null) return;
		if (onCondition instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr) onCondition;
			String op = expr.getOperator().getName();
			if(op.equals("AND")||op.equals("OR")||!isValueOperator(op)){
				parseJoinOnCondition(expr.getLeft(), onColumns);
				parseJoinOnCondition(expr.getRight(), onColumns);
			}else{
				parseWhereBinaryExpr(expr,onColumns);
			}
		} else if(onCondition instanceof SQLIdentifierExpr){
			SQLIdentifierExpr expr = (SQLIdentifierExpr)onCondition;
			String name = expr.getName();
			ScistorColumn column = new ScistorColumn(name);
			onColumns.add(column);
		} else if(onCondition instanceof SQLPropertyExpr){
			SQLPropertyExpr expr = (SQLPropertyExpr) onCondition;
			String owner = expr.getOwner().toString();
			String columnName = expr.getName();
			ScistorColumn column = new ScistorColumn(owner,columnName);
			onColumns.add(column);
		} else if (onCondition instanceof SQLInListExpr) {
			throw new ScistorParserException("SQL ERROR: 'in' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLExistsExpr) {
			throw new ScistorParserException("SQL ERROR: 'exists' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLNotExpr) {
			throw new ScistorParserException("SQL ERROR: 'not' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLBetweenExpr) {
			throw new ScistorParserException("SQL ERROR: 'between .. and ..' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLInSubQueryExpr){
			throw new ScistorParserException("SQL ERROR: 'in' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLAggregateExpr) {
			SQLAggregateExpr expr = (SQLAggregateExpr) onCondition;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' syntax is not supported within join on conditon");
		} else if (onCondition instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr) onCondition;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' syntax is not supported within join on conditon");
		} 
	}
	
	private void parseJoinUsingCondition(List<SQLExpr> using, List<ScistorColumn> onColumns) throws ScistorParserException {
		if(using==null || using.size()==0) return;
		SQLExpr expr = using.get(0);
		if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			String name = e.getName();
			ScistorColumn column = new ScistorColumn(name);
			onColumns.add(column);
		}else{
			throw new ScistorParserException("SQL ERROR : not supported syntax in using condition");
		}
	}
	
	protected void parseGroupBy(SQLSelectGroupByClause groupby , List<ScistorColumn> groupbyColumns) throws ScistorParserException{
		if(groupby == null) return;
		List<SQLExpr> exprs = groupby.getItems();
		for(SQLExpr e : exprs){
			//MySqlSelectGroupByExpr expr = (MySqlSelectGroupByExpr) e;
			MySqlOrderingExpr expr = (MySqlOrderingExpr) e;
			ScistorColumn column = new ScistorColumn();
			parseNameExpr(expr.getExpr(), column, "group by");
			groupbyColumns.add(column);
		}
		SQLExpr having = groupby.getHaving();
		parseHaving(having, groupbyColumns, null);
	}
	
	
	protected void parseHaving(SQLExpr having , List<ScistorColumn> havingColumns,List<SQLSelectQuery> subQuerys) throws ScistorParserException{
		if(having==null) return;
		if (having instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr) having;
			String op = expr.getOperator().getName();
			if(op.equals("AND")||op.equals("OR")||!isValueOperator(op)){
				parseHaving(expr.getLeft(), havingColumns,subQuerys);
				parseHaving(expr.getRight(), havingColumns,subQuerys);
			}else{
				parseWhereBinaryExpr(expr,havingColumns);
			}
		} else if(having instanceof SQLIdentifierExpr){
			SQLIdentifierExpr expr = (SQLIdentifierExpr)having;
			String name = expr.getName();
			ScistorColumn column = new ScistorColumn(name);
			havingColumns.add(column);
		} else if(having instanceof SQLPropertyExpr){
			SQLPropertyExpr expr = (SQLPropertyExpr) having;
			String owner = expr.getOwner().toString();
			String columnName = expr.getName();
			ScistorColumn column = new ScistorColumn(owner,columnName);
			havingColumns.add(column);
		} else if (having instanceof SQLInListExpr) {
			SQLInListExpr expr = (SQLInListExpr) having;
			parseWhereInListExpr(expr,havingColumns);
		} else if (having instanceof SQLExistsExpr) {
			SQLExistsExpr expr = (SQLExistsExpr) having;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
		} else if (having instanceof SQLNotExpr) {
			SQLNotExpr expr = (SQLNotExpr) having;
			parseHaving(expr.getExpr(),havingColumns,subQuerys);
		} else if (having instanceof SQLBetweenExpr) {
			SQLBetweenExpr expr = (SQLBetweenExpr) having;
			parseWhereBetweenExpr(expr,havingColumns);
		} else if (having instanceof SQLInSubQueryExpr){
			SQLInSubQueryExpr expr  = (SQLInSubQueryExpr) having;
			if(subQuerys!=null) subQuerys.add(expr.getSubQuery().getQuery());
			parseHaving(expr,havingColumns, null);
		} else if (having instanceof SQLAggregateExpr) {
			SQLAggregateExpr expr = (SQLAggregateExpr) having;
			SQLExpr col = expr.getArguments().get(0);
			ScistorColumn column = new ScistorColumn();
			parseNameExpr(col, column, "having "+expr.getMethodName());
			havingColumns.add(column);
		} else if (having instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr) having;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' is not supported in having conditon");
		} 
	}
	
	protected void parseOrderBy(SQLOrderBy orderby,List<ScistorColumn> orderbyColumns) throws ScistorParserException{
		if(orderby == null) return;
		List<SQLSelectOrderByItem> items = orderby.getItems();
		for(SQLSelectOrderByItem item : items){
			SQLExpr expr = item.getExpr();
			ScistorColumn column = new ScistorColumn();
			parseNameExpr(expr, column, "order by");
			orderbyColumns.add(column);
		}
	}
	
	protected void parseNameExpr(SQLExpr expr,ScistorColumn column,String param) throws ScistorParserException{
		if(expr instanceof SQLIdentifierExpr){
			SQLIdentifierExpr e = (SQLIdentifierExpr)expr;
			column.setName(e.getName());
		}else if(expr instanceof SQLPropertyExpr){
			SQLPropertyExpr e = (SQLPropertyExpr)expr;
			column.setName(e.getName());
			column.setOwner(e.getOwner().toString());
		}else{
			throw new ScistorParserException("SQL ERROR: "+expr.toString()+" is not supported syntax in "+param+" conditon");
		}
	}
	/**
	 * 当时join情况的时候找其可能对应的表
	 * @param column
	 * @param subSelectColumns
	 * @param tables
	 * @throws ScistorParserException 
	 */
	protected void findColumnTable(ScistorColumn column,Map<String,List<ScistorSelectColumn>> subSelectColumns,List<ScistorTable> tables) throws ScistorParserException{
		if(column.getOwner()==null){
			if(subSelectColumns==null){
				if(tables.size()==1){
					column.setOwner(tables.get(0).getTablename());
				}else{
					for(ScistorTable table : tables){
						column.addPossibleOwner(table.getTablename());
					}
				}
			}else{
				if(!inSubSelect(column,subSelectColumns)){
					if(tables.size()==1){
						column.setOwner(tables.get(0).getTablename());
					}else{
						for(ScistorTable table : tables){
							column.addPossibleOwner(table.getTablename());
						}
					}
				}
			}
		}else{
			for(ScistorTable table : tables){
				if(table.getTablealias()!=null){
					if(table.getTablealias().equals(column.getOwner())){
						column.setOwner(table.getTablename());
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 判断外层的列是否来自于嵌套查询
	 * @param column
	 * @param subSelectColumns
	 * @return
	 * @throws ScistorParserException 
	 */
	protected boolean inSubSelect(ScistorColumn column, Map<String, List<ScistorSelectColumn>> subSelectColumns) throws ScistorParserException {
		for(String alias : subSelectColumns.keySet()){
			List<ScistorSelectColumn> lists = subSelectColumns.get(alias);
			for(ScistorSelectColumn in : lists){
				if(in.isSelectAll()) throw new ScistorParserException("SQL ERROR : not supported '*' in subquery or union clause");
				if(in.getAlias()!=null){
					if(in.getAlias().equals(column.getName())){
						return true;
					}
				}else{
					if(in.getName().equals(column.getName())){
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * 获取列所属的嵌套查询的别名
	 */
	protected void getInSubSelectAliases(ScistorColumn column, Map<String, List<ScistorSelectColumn>> subSelectColumns,List<String> inaliasNames) throws ScistorParserException {
		for(String alias : subSelectColumns.keySet()){
			List<ScistorSelectColumn> lists = subSelectColumns.get(alias);
			for(ScistorSelectColumn in : lists){
				if(in.isSelectAll()) throw new ScistorParserException("SQL ERROR : not supported '*' in subquery or union clause");
				if(in.getAlias()!=null){
					if(in.getAlias().equals(column.getName())){
						inaliasNames.add(alias);
					}
				}else{
					if(in.getName().equals(column.getName())){
						inaliasNames.add(alias);
					}
				}
			}
		}
	}
	
	protected List<ScistorSelectColumn> getSubQuerySelectColumns(String subAlias,SQLSelectQuery subQuery) throws ScistorParserException {
		if(subQuery instanceof MySqlSelectQueryBlock){
			MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) subQuery;
			List<ScistorSelectColumn> inselectedColumns = new ArrayList<ScistorSelectColumn>();
			List<SQLSelectItem> items = queryBlock.getSelectList();
			parseSelectItems(items, inselectedColumns);
			for(ScistorSelectColumn column : inselectedColumns){
				column.setSubQueryAlias(subAlias);
			}
			return inselectedColumns;
		}//else if(subQuery instanceof MySqlUnionQuery){
		else if (subQuery instanceof SQLUnionQuery) {
			//MySqlUnionQuery union = (MySqlUnionQuery) subQuery;
			SQLUnionQuery union = (SQLUnionQuery)subQuery;
			/*
			 * 暂时没有处理
			 */
			SQLSelectQuery  left = union.getLeft();
			while(!(left instanceof MySqlSelectQueryBlock)){
				/*if(left instanceof MySqlUnionQuery){
					left = ((MySqlUnionQuery)left).getLeft();
				}*/
				if(left instanceof SQLUnionQuery){
					left = ((SQLUnionQuery)left).getLeft();
				}
			}
			return getSubQuerySelectColumns(subAlias,left);
		}
		return null;
	}
	
	protected void getQueryListFromUnion(SQLSelectQuery query,List<SQLSelectQuery> queryLists){
		if(query instanceof MySqlSelectQueryBlock){
			queryLists.add(query);
		}//else if(query instanceof MySqlUnionQuery){
		else if(query instanceof SQLUnionQuery){
			SQLUnionQuery union = (SQLUnionQuery) query;
			getQueryListFromUnion(union.getLeft(), queryLists);
			getQueryListFromUnion(union.getRight(), queryLists);
		}
	}
	
}
