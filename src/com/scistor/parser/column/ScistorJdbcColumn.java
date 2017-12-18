package com.scistor.parser.column;

/**
 * JDBC执行结果后返回的列，只有select * 号时使用。
 * @author GuoLiang
 */
public class ScistorJdbcColumn {
	private String owner;
	private String name;
	
	public ScistorJdbcColumn() {
		
	}
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String toString(){ 
		return "[tablename:"+this.owner+",column:"+this.name+"]";
	}
}
