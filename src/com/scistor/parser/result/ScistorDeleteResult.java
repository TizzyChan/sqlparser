package com.scistor.parser.result;

import java.util.ArrayList;
import java.util.List;

import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.column.ScistorSelectColumn;
import com.scistor.parser.column.ScistorTextColumn;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.table.ScistorTable;

public class ScistorDeleteResult extends ScistorColumnResult{

	protected List<String> tables;
	protected List<ScistorColumn> whereColumns;
	
	public List<String> getTables(){
		return this.tables;
	}
	@Override
	public List<ScistorColumn> getConditionColumns() {
		return this.whereColumns;
	}
 
	public void addConditionColumn(ScistorColumn column) throws ScistorParserException{
		if(this.whereColumns==null){
			this.whereColumns = new ArrayList<>();
		}
		if(!(column instanceof ScistorTextColumn)&&!(column instanceof ScistorSelectColumn)){
			for(ScistorColumn col : this.whereColumns){
				if(col.getName().equals(column.getName())){
					if(col.getOwner()==null&&column.getOwner()==null){
						return;
					}
					if(col.getOwner()!=null&&column.getOwner()!=null&&col.getOwner().equals(column.getOwner())){
						return;
					}
				}
			}
		}
		this.whereColumns.add(column);
	}
	
	public void replaceWherePart(ScistorSelectColumn column) throws ScistorParserException{
		if(this.whereColumns!=null){
			if(column.getOwner()==null&&column.getPossibleOwners()==null&&column.getSubQueryAlias()==null){
				System.out.println(column.getName()+" can not be finded");
				throw new ScistorParserException("SQL ERROR");
			}
			for(ScistorColumn co: this.whereColumns){
				replaceColumn((ScistorColumn)co, column);
			}
		}
	}
	
	protected void replaceColumn(ScistorColumn co,ScistorSelectColumn column){
		if(co.getOwner()!=null){
			if(column.getSubQueryAlias().equals(co.getOwner())){
				replaceColumnIf(co, column);
			}
		}else{
			if(co.getPossibleOwners()==null){
				replaceColumnIf(co, column);
			}
		}
	}
	protected void replaceColumnIf(ScistorColumn co,ScistorSelectColumn column){
		if(column.getAlias()!=null){
			if(column.getAlias().equals(co.getName())){
				replaceColumnContent(co, column);
			}
		}else{
			if(column.getName().equals(co.getName())){
				replaceColumnContent(co, column);
			}
		}
	}
	protected void replaceColumnContent(ScistorColumn co,ScistorSelectColumn column){
		co.setName(column.getName());
		co.setOwner(column.getOwner());
		co.setPossibleOwners(column.getPossibleOwners());
		co.setSubQueryAlias(column.getSubQueryAlias());
	}

	public void setTables(List<ScistorTable> deletetables) {
		if(this.tables == null) {
			this.tables = new ArrayList<String>();
		}
		for(ScistorTable table : deletetables){
			this.tables.add(table.getTablename());
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		int size = this.tables.size();
		sb.append("DeleteContentTables:");
		for(String tablename : this.tables){
			sb.append(tablename);
			if(--size!=0) sb.append(",");
		}
		if(this.whereColumns!=null){
			int se = this.whereColumns.size();
			sb.append("\nConditionColumns:\n");
			for(ScistorColumn column : this.whereColumns){
				sb.append(column.toString());
				if(--se!=0) sb.append("\n");
			}
		}
		return sb.toString();
	}
}
