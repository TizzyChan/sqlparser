package com.scistor.parser.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
//import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.scistor.parser.column.ScistorSelectColumn;
import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.result.ScistorDeleteResult;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.result.ScistorSQLType;
import com.scistor.parser.table.ScistorTable;

public class ScistorMysqlDeleteParser  extends ScistorMysqlConditionParser {

	protected List<ScistorTable> tables;
	public ScistorMysqlDeleteParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorDeleteResult();
		this.result.setSqlType(ScistorSQLType.DELETE);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		MySqlDeleteStatement delete = (MySqlDeleteStatement) this.statement;
		this.tables = new ArrayList<ScistorTable>();
		
		SQLTableSource tablesource = delete.getTableSource();
		List<ScistorColumn> onColumns = new ArrayList<ScistorColumn>();
		parseTableSource(tablesource,onColumns);
		
		if(this.tables.size()>1&&(delete.getOrderBy()!=null || delete.getLimit()!=null)){
			throw new ScistorParserException("SQL ERROR : not support orderby or limit when delete more than one table.");
		}
		
		int size = this.tables.size();
		if(size!=0){
			((ScistorDeleteResult)this.result).setTables(this.tables);
		}
		tablesource = delete.getFrom();
		parseTableSource(tablesource,onColumns);
		
		if(this.tables.size()!=0&&size==0){
			((ScistorDeleteResult)this.result).setTables(this.tables);
		}
		
		tablesource = delete.getUsing();
		parseTableSource(tablesource,onColumns);
		
		for(ScistorColumn column : onColumns){
			addValueColumn(column);
		}
			
		SQLExpr where = delete.getWhere();
		if(where!=null){
			List<SQLSelectQuery> subQuerys = new ArrayList<SQLSelectQuery>();
			List<ScistorColumn> whereColumns = new ArrayList<ScistorColumn>();
			
			parseWhere(where,whereColumns,subQuerys);
			
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
		parseOrderBy(delete.getOrderBy(), orderbyColumns);
		if(orderbyColumns.size()!=0){
			for(ScistorColumn column : orderbyColumns){
				addValueColumn(column);
			}
		}
		return this.result;
	}
	
	protected void addToTables(ScistorTable table){
		for(ScistorTable tab : this.tables){
			if(tab.getTablename().equals(table.getTablename())){
				return ;
			}
		}
		this.tables.add(table);
	}
	
	protected void parseTableSource(SQLTableSource tablesource,List<ScistorColumn> onColumns) throws ScistorParserException{
		if(tablesource == null) return;
		if(tablesource instanceof SQLExprTableSource){
			SQLExprTableSource expr = (SQLExprTableSource) tablesource;
			SQLExpr tableExpr = expr.getExpr();
			if(!(tableExpr instanceof SQLIdentifierExpr)){
				throw new ScistorParserException("SQL ERROR : not supported table syntax "+tableExpr.toString());
			}
			String tablename = tableExpr.toString();
			String tablealias = expr.getAlias();
			ScistorTable table = new ScistorTable(tablename, tablealias);
			addToTables(table);
		}else if(tablesource instanceof SQLJoinTableSource){
			SQLJoinTableSource expr = (SQLJoinTableSource) tablesource;
			parseTableSource(expr.getLeft(),onColumns);
			parseTableSource(expr.getRight(),onColumns);
			SQLExpr onCondition = expr.getCondition();
			parseJoinOnCondition(onCondition,onColumns);
		}
	}
	
	protected void addValueColumn(ScistorColumn column) throws ScistorParserException{
		if(this.tables.size()==1){
			column.setOwner(this.tables.get(0).getTablename());
		}else{
			if(column.getOwner()!=null){
				for(ScistorTable table: this.tables){
					if(table.getTablealias()!=null&&column.getOwner().equals(table.getTablealias())){
						column.setOwner(table.getTablename());
					}
				}
				
			}else{
				for(ScistorTable table: this.tables){
					column.addPossibleOwner(table.getTablename());
				}
			}
		}
		this.result.addConditionColumn(column);
	}
	
	/**
	 * @param subQuery
	 * @param thisQueryAlias
	 * @param type
	 * @throws ScistorParserException
	 */
	protected void parseSubQueryCondition(SQLSelectQuery subQuery,String thisQueryAlias) throws ScistorParserException{
		if(subQuery instanceof MySqlSelectQueryBlock){
			MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) subQuery;
			if(queryBlock.getInto()!=null) throw new ScistorParserException("SQL ERROR : select .. into.. syntax is not supported");
			
			List<ScistorSelectColumn> inselectedColumns = getSelectColumns(queryBlock);
			
			List<SQLSelectQuery> subWhereQuerys = new ArrayList<SQLSelectQuery>();
			List<ScistorColumn> inwhereColumns = getAllConditionColumnExceptJoinOn(queryBlock, subWhereQuerys);
			
			for(ScistorSelectColumn column : inselectedColumns){
				if(column.isSelectAll()) throw new ScistorParserException("SQL ERROR : not supported '*' in subquery or union clause");
				column.setSubQueryAlias(thisQueryAlias);
			}
			
			for(ScistorColumn column : inwhereColumns){
				column.setSubQueryAlias(thisQueryAlias);
			}
			replaceWhereCNameWithSelectCAliasName(inwhereColumns, inselectedColumns);
			
			SQLTableSource tableSource = queryBlock.getFrom();
			
			if(tableSource instanceof SQLExprTableSource){
				SQLExprTableSource table = (SQLExprTableSource) tableSource;
				SQLExpr tableExpr = table.getExpr();
				if(!(tableExpr instanceof SQLIdentifierExpr)){
					throw new ScistorParserException("SQL ERROR : not supported table syntax "+tableExpr.toString());
				}
				String tablename = ((SQLIdentifierExpr)tableExpr).getName();
				
				for(ScistorSelectColumn column : inselectedColumns){
					column.setOwner(tablename);
					this.result.replaceWherePart(column);
				}
				
				for(ScistorColumn column : inwhereColumns){
					column.setOwner(tablename);
					this.result.addConditionColumn(column);
				}
				
			}else if(tableSource instanceof SQLJoinTableSource){
				Map<String,SQLSelectQuery> subQuerys = new HashMap<String,SQLSelectQuery>();
				List<ScistorTable> tables = new ArrayList<ScistorTable>();
				List<ScistorColumn> onColumns = new ArrayList<ScistorColumn>();
				parseJoinTableSource(tableSource,subQuerys,tables,onColumns);
				
				for(ScistorColumn column : onColumns){
					column.setIsWhere(true);
				}
				
				replaceWhereCNameWithSelectCAliasName(onColumns, inselectedColumns);
				inwhereColumns.addAll(onColumns);
				
				if(subQuerys.size()==0){// there is no sub query
					for(ScistorSelectColumn column : inselectedColumns){
						findColumnTable(column, null,tables);
						this.result.replaceWherePart(column);
					}
					for(ScistorColumn column : inwhereColumns){
						findColumnTable(column, null, tables);
						this.result.addConditionColumn(column);
					}
				}else if(tables.size()==0){ // only sub querys
					for(ScistorSelectColumn column : inselectedColumns){
						this.result.replaceWherePart(column);
					}
					for(ScistorColumn column : inwhereColumns){
						this.result.addConditionColumn(column);
					}
					for(String subAlias : subQuerys.keySet()){
						parseSubQueryCondition(subQuerys.get(subAlias), subAlias);
					}
				}else{ // there is tablename and subquery
					Map<String,List<ScistorSelectColumn>> subSelectColumns = new HashMap<String,List<ScistorSelectColumn>>();
					for(String subAlias : subQuerys.keySet()){
						subSelectColumns.put(subAlias, getSubQuerySelectColumns(subAlias,subQuerys.get(subAlias)));
					}
					for(ScistorSelectColumn column : inselectedColumns){
						findColumnTable(column, subSelectColumns, tables);
						this.result.replaceWherePart(column);
					}
					for(ScistorColumn column : inwhereColumns){
						findColumnTable(column, subSelectColumns, tables);
						this.result.addConditionColumn(column);
					}
					for(String subAlias : subQuerys.keySet()){
						parseSubQueryCondition(subQuerys.get(subAlias), subAlias);
					}
				}
			}else if(tableSource instanceof SQLSubqueryTableSource){
				SQLSubqueryTableSource subsubQuery = (SQLSubqueryTableSource) tableSource;
				String subAlias = subsubQuery.getAlias();
				Map<String,List<ScistorSelectColumn>> subSelectColumns = new HashMap<String,List<ScistorSelectColumn>>();
				subSelectColumns.put(subAlias, getSubQuerySelectColumns(subAlias,subsubQuery.getSelect().getQuery()));
				for(ScistorSelectColumn column : inselectedColumns){
					if(!inSubSelect(column, subSelectColumns)){
						throw new ScistorParserException("SQL ERROR : Unknown column : "+ column.getName());
					}
					column.setOwner(subAlias);
					this.result.replaceWherePart(column);
				}
				for(ScistorColumn column : inwhereColumns){
					if(!inSubSelect(column, subSelectColumns)){
						throw new ScistorParserException("SQL ERROR : Unknown column : "+ column.getName());
					}
					column.setOwner(subAlias);
					this.result.addConditionColumn(column);
				}
				parseSubQueryCondition(subsubQuery.getSelect().getQuery(), subAlias);
			}else if(tableSource instanceof SQLUnionQueryTableSource){
				SQLUnionQueryTableSource subUnion = (SQLUnionQueryTableSource) tableSource;
				String subAlias = subUnion.getAlias();
				/*
				 * 
				 */
				List<SQLSelectQuery> queryLists = new ArrayList<SQLSelectQuery>();
				//MySqlUnionQuery union = (MySqlUnionQuery) subUnion.getUnion();
				SQLUnionQuery union = (SQLUnionQuery) subUnion.getUnion();
				getQueryListFromUnion(union.getLeft(), queryLists);
				getQueryListFromUnion(union.getRight(), queryLists);
				
				Map<String,List<ScistorSelectColumn>> subSelectColumns = new HashMap<String,List<ScistorSelectColumn>>();
				
				for(ScistorSelectColumn column : inselectedColumns){
					if(!inSubSelect(column, subSelectColumns)){
						throw new ScistorParserException("SQL ERROR : Unknown column : "+ column.getName());
					}
					column.setOwner(subAlias);
					this.result.replaceWherePart(column);
				}
				for(ScistorColumn column : inwhereColumns){
					if(!inSubSelect(column, subSelectColumns)){
						throw new ScistorParserException("SQL ERROR : Unknown column : "+ column.getName());
					}
					column.setOwner(subAlias);
					this.result.addConditionColumn(column);
				}
				for(int i=0;i<queryLists.size();i++){
					parseSubQueryCondition(queryLists.get(i),subAlias);
				}
			}else {
				throw new ScistorParserException("SQL ERROR : '"+tableSource.getClass().toString().substring(tableSource.getClass().toString().lastIndexOf("."))+"' is not supported");
			}
			if(subWhereQuerys.size()>0){
				for(SQLSelectQuery query : subWhereQuerys){
					parseSubQueryCondition(query,sefDefindSubAlias+(++selfDefinedSubAliasID));
				}
			}
		}else{
			throw new ScistorParserException("SQL ERROR : Parse Error");
		}
	}
	
}
