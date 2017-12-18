package com.scistor.parser.column;

import java.util.ArrayList;
import java.util.List;

/**
 * ×Ö¶ÎÃûµÄÁÐ£¬
 * @author GuoLiang
 */
public class ScistorColumn{
	
	protected String owner;
	protected String name;
	protected List<String> possibleOwners;
	protected String subQueryAlias;
	protected boolean isWhere;
	public boolean isWhere() {
		return isWhere;
	}
	public void setIsWhere(boolean isWhere) {
		this.isWhere = isWhere;
	}
	public ScistorColumn(){
		
	}
	public ScistorColumn(String name){
		this.name = name;
	}
	public ScistorColumn(String owner,String name){
		this(name);
		this.owner = owner;
	}
	public String getOwner() {
		return owner;
	}
	public String getName() {
		return name;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getPossibleOwners() {
		return possibleOwners;
	}
	public void setPossibleOwners(List<String> possibleOwners) {
		this.possibleOwners = possibleOwners;
	}
	public void addPossibleOwner(String tablename){
		if(this.owner==null){
			if(this.possibleOwners==null){
				this.possibleOwners = new ArrayList<String>();
			}
			if(!this.possibleOwners.contains(tablename)){
				this.possibleOwners.add(tablename);
			}
		}
	}
	public String getSubQueryAlias() {
		return subQueryAlias;
	}
	public void setSubQueryAlias(String subQueryAlias) {
		this.subQueryAlias = subQueryAlias;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(owner!=null){
			sb.append("[name:"+this.name+",owner:"+this.owner+"]");
		}else{
			sb.append("[name:"+this.name+",possibleOwners:"+this.possibleOwners+"]");
		}
		return sb.toString();
	}
}
