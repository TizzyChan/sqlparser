package com.scistor.parser.result;

import java.util.ArrayList;
import java.util.List;

public class ScistorDropResult extends ScistorResult {

	private List<String> tablenames;
	
	public ScistorDropResult() {}
	
	
	public List<String> getTableName(){
		return this.tablenames;
	}
	
	
	public void addTableName(String tablename){
		if(this.tablenames == null){
			this.tablenames = new ArrayList<String>();
		}
		if(!this.tablenames.contains(tablename)){
			this.tablenames.add(tablename);
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		int size = this.tablenames.size();
		for(String tablename : this.tablenames){
			sb.append("[tablename:"+tablename+"]");
			if(--size!=0){
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
