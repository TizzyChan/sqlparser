package com.scistor.parser.oracle;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleUpdateStatement;
import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.column.ScistorTextColumn;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.result.ScistorSQLType;
import com.scistor.parser.result.ScistorUpdateResult;
import com.scistor.parser.table.ScistorTable;

public class ScistorOracleUpdateParser  extends ScistorOracleDeleteParser{
	public ScistorOracleUpdateParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorUpdateResult();
		this.result.setSqlType(ScistorSQLType.UPDATE);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		OracleUpdateStatement update = (OracleUpdateStatement) this.statement;
		
		this.tables = new ArrayList<ScistorTable>();
		
		SQLTableSource tablesource = update.getTableSource();
		
		List<ScistorColumn> onColumns = new ArrayList<ScistorColumn>();
		parseTableSource(tablesource,onColumns);
		
		/*if(this.tables.size()>1&&(update.getOrderBy()!=null || update.getLimit()!=null)){
			throw new ScistorParserException("SQL ERROR : not support orderby or limit when update more than one table.");
		}*/
		
		((ScistorUpdateResult)this.result).setTables(this.tables);
		
		if(onColumns.size()!=0){
			for(ScistorColumn column : onColumns){
				addValueColumn(column);
			}
		}
		
		List<ScistorColumn> setColumns = new ArrayList<ScistorColumn>();
		List<SQLUpdateSetItem> setList = update.getItems();
		parseUpdateSetList(setList, setColumns);
		
		for(ScistorColumn column : setColumns){
			addValueColumn(column);
		}
		
		SQLExpr where = update.getWhere();
		if(where!=null){
			List<SQLSelectQuery> subQuerys = new ArrayList<SQLSelectQuery>();
			List<ScistorColumn> whereColumns = new ArrayList<ScistorColumn>();
			parseWhere(where,whereColumns,subQuerys);
			
			for(ScistorColumn column : whereColumns){
				column.setIsWhere(true);
			}
			
			for(ScistorColumn column : whereColumns){
				addValueColumn(column);
			}
			
			if(subQuerys.size()>0){
				for(SQLSelectQuery query : subQuerys){
					parseSubQueryCondition(query, sefDefindSubAlias+(++selfDefinedSubAliasID));
				}
			}
		}
		
		List<ScistorColumn> orderbyColumns = new ArrayList<ScistorColumn>();
		parseOrderBy(update.getOrderBy(), orderbyColumns);
		if(orderbyColumns.size()!=0){
			for(ScistorColumn column : orderbyColumns){
				addValueColumn(column);
			}
		}
		return this.result;
	}
	
	private void parseUpdateSetList(List<SQLUpdateSetItem> setList,List<ScistorColumn> setColumns) throws ScistorParserException{
		for(SQLUpdateSetItem item : setList){
			SQLExpr columnName = item.getColumn();
			SQLExpr value = item.getValue();
			if((columnName instanceof SQLIdentifierExpr || columnName instanceof SQLPropertyExpr)
					&&(value instanceof SQLTextLiteralExpr)){
				ScistorTextColumn column = new ScistorTextColumn();
				column.addExpr(value);
				parseWhereTextExpr(columnName, column);
				setColumns.add(column);
			}else{
				parseSetExpr(columnName,setColumns);
				parseSetExpr(value, setColumns);
			}
		}
	}
	private void parseSetExpr(SQLExpr set,List<ScistorColumn> setColumns) throws ScistorParserException{
		if(set==null) return;
		if (set instanceof SQLBinaryOpExpr) {
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr) set;
			String op = expr.getOperator().getName();
			if(op.equals("=")) throw new ScistorParserException("SQL ERROR : too more '=' operator");
			parseSetExpr(expr.getLeft(), setColumns);
			parseSetExpr(expr.getRight(), setColumns);
		} else if(set instanceof SQLIdentifierExpr){
			SQLIdentifierExpr expr = (SQLIdentifierExpr)set;
			String name = expr.getName();
			ScistorColumn column = new ScistorColumn(name);
			setColumns.add(column);
		} else if(set instanceof SQLPropertyExpr){
			SQLPropertyExpr expr = (SQLPropertyExpr) set;
			String owner = expr.getOwner().toString();
			String columnName = expr.getName();
			ScistorColumn column = new ScistorColumn(owner,columnName);
			setColumns.add(column);
		} else if (set instanceof SQLInListExpr) {
			throw new ScistorParserException("SQL ERROR: 'in' syntax is not supported within set conditon");
		} else if (set instanceof SQLExistsExpr) {
			throw new ScistorParserException("SQL ERROR: 'exists' syntax is not supported within set conditon");
		} else if (set instanceof SQLNotExpr) {
			throw new ScistorParserException("SQL ERROR: 'not' syntax is not supported within set conditon");
		} else if (set instanceof SQLBetweenExpr) {
			throw new ScistorParserException("SQL ERROR: 'between .. and ..' syntax is not supported within set conditon");
		} else if (set instanceof SQLInSubQueryExpr){
			throw new ScistorParserException("SQL ERROR: 'in' syntax is not supported within set conditon");
		} else if (set instanceof SQLAggregateExpr) {
			SQLAggregateExpr expr = (SQLAggregateExpr) set;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' can not be used in set conditon");
		} else if (set instanceof SQLMethodInvokeExpr) {
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr) set;
			throw new ScistorParserException("SQL ERROR:'"+expr.getMethodName()+"' is not supported within set conditon");
		} 
	}
}
