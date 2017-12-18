package com.scistor.parser.result;

import com.scistor.parser.column.ScistorColumn;

/**
 * @author GuoLiang
 */
public class ScistorUpdateResult extends ScistorDeleteResult {

	public String toString(){
		StringBuilder sb = new StringBuilder();
		int size = this.tables.size();
		sb.append("UpdateTables:");
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
