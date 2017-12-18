package com.scistor.parser.result;

import java.util.ArrayList;
import java.util.List;

import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.column.ScistorSelectColumn;
import com.scistor.parser.exception.ScistorParserException;

public class ScistorInsertResult  extends ScistorColumnResult{

	private boolean noColumn;
	private String tablename;
	private List<ScistorColumn> columns;
	
	public boolean isNoColumn() {
		return noColumn;
	}
	public void setNoColumn(boolean noColumn) {
		this.noColumn = noColumn;
	}
	public String getTablename() {
		return tablename;
	}
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}
	@Override
	public List<ScistorColumn> getConditionColumns() {
		return this.columns;
	}
	@Override
	public void addConditionColumn(ScistorColumn column) throws ScistorParserException {
		if(this.columns==null){
			this.columns = new ArrayList<ScistorColumn>();
		}
		this.columns.add(column);
	}
	
	public String toString(){
		if(this.noColumn){
			return "[tablename:"+this.tablename+",noColumn:"+this.noColumn+"]";
		}else{
			StringBuilder sb = new StringBuilder();
			for(ScistorColumn column : this.columns){
				sb.append(column.toString());
			}
			return sb.toString();
		}
	}
	
	@Override
	public void replaceWherePart(ScistorSelectColumn column) throws ScistorParserException {
		// TODO Auto-generated method stub
		
	}
	
}
