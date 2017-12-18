package com.scistor.parser.result;

public class ScistorAlterResult extends ScistorResult {

	
	private String tableName;
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tablename) {
		this.tableName = tablename;
	}
	
	public String toString(){
		return "tablename:"+this.tableName;
	}
}
