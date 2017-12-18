package com.scistor.parser.table;

/**
 * @author GuoLiang
 */
public class ScistorTable {
	private String tablename;
	private String tablealias;
	public ScistorTable(String tname){
		this(tname, null);
	}
	
	public ScistorTable(String tname,String talias){
		this.tablename = tname;
		this.tablealias = talias;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getTablealias() {
		return tablealias;
	}

	public void setTablealias(String tablealias) {
		this.tablealias = tablealias;
	}
	public String toString(){
		return "[tablename:"+this.tablename+"]";
	}
}
