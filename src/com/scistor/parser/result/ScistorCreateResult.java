package com.scistor.parser.result;

public class ScistorCreateResult extends ScistorResult {

	private String tablename;
	public ScistorCreateResult() {}
	public void setTableName(String tablename){
		this.tablename = tablename;
	}
	public String getTableName(){
		return this.tablename;
	}
	public String toString(){
		return "[tablename:"+this.tablename+"]";
	}
}
