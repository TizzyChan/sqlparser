package com.scistor.parser.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.column.ScistorSelectColumn;
import com.scistor.parser.column.ScistorTextColumn;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.mysql.ScistorMysqlSelectParser;

/**
 * @author GuoLiang
 */
public class ScistorSelectResult extends ScistorColumnResult{
	
	private boolean isSelectAll;
	private List<ScistorSelectColumn> selectColumns;
	private List<ScistorColumn> whereColumns;
	
	public ScistorSelectResult(){
		
	}
	public boolean isSelectAll() {
		return isSelectAll;
	}

	public void setSelectAll(boolean isSelectAll) {
		this.isSelectAll = isSelectAll;
	}

	public List<ScistorSelectColumn> getSelectColumns(){
		return this.selectColumns;
	}
	
	
	public List<ScistorColumn> getConditionColumns(){
		/*
		 * 去重一下，避免重复验证。
		 */
		List<ScistorColumn> columns = null; 
		if(this.whereColumns!=null){
			columns = new ArrayList<ScistorColumn>();
			for(ScistorColumn column : this.whereColumns){
				if(!(column instanceof ScistorTextColumn)){
					boolean isDuplicate = false;
					for(ScistorColumn co : columns){
						if(co.getName().equals(column.getName())){
							if(co.getOwner()!=null&&column.getOwner()!=null&&co.getOwner().equals(column.getOwner())){
								isDuplicate = true;
								break;
							}else if(co.getPossibleOwners()!=null&&column.getPossibleOwners()!=null){
								int size1 = co.getPossibleOwners().size();
								int size2 = column.getPossibleOwners().size();
								if(size1!=size2) continue;
								for(String tb : co.getPossibleOwners()){
									if(!column.getPossibleOwners().contains(tb)){
										break;
									}
									--size1;
								}
								if(size1==0){
									isDuplicate = true;
									break;
								}
							}
						}
					}
					if(!isDuplicate){
						columns.add(column);
					}
				}else{
					columns.add(column);
				}
			}
		}
		return columns;
	}
	
	public void addSelectColumn(ScistorSelectColumn column){
		if(column==null) return;
		if(this.selectColumns==null){
			this.selectColumns = new ArrayList<ScistorSelectColumn>();
		}
		if(column.isFromJdbc()&&column.getOwner()==null){
			if(column.getPossibleOwners()!=null&&column.getPossibleOwners().size()>1){
				if(this.duplicateColumns==null){
					this.duplicateColumns = new TreeMap<String,Integer>();
				}
				if(this.selectColumns.contains(column)&&column.getPossibleOwners()!=null&&column.getPossibleOwners().size()>1){
					int lastIndex = this.selectColumns.lastIndexOf(column);
					if(!this.duplicateColumns.containsKey(column.getName())){
						this.duplicateColumns.put(column.getName(), 0);
					}
					ScistorSelectColumn duplicate = this.selectColumns.get(lastIndex);
					if(duplicate.getPossibleOwners()!=null){
						int id = this.duplicateColumns.get(column.getName()).intValue();
						duplicate.setOwner(column.getPossibleOwners().get(id));
						this.duplicateColumns.put(column.getName(), new Integer(++id) );
						duplicate.setPossibleOwners(null);
					}
					int id = this.duplicateColumns.get(column.getName()).intValue();
					column.setOwner(column.getPossibleOwners().get(id));
					this.duplicateColumns.put(column.getName(), new Integer(++id) );
					column.setPossibleOwners(null);
				}
			}
		}
		this.selectColumns.add(column);
	}
	@Override
	public void addConditionColumn(ScistorColumn column) throws ScistorParserException {
		if(this.whereColumns == null){
			this.whereColumns = new ArrayList<ScistorColumn>();
		}
		
		if(!(column instanceof ScistorTextColumn)){
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
	
	
	public void replace(ScistorSelectColumn column,int part) throws ScistorParserException{
		if(part == ScistorMysqlSelectParser.ONLY_SELECT){
			replaceSelectPart(column);
		}else if(part == ScistorMysqlSelectParser.ONLY_WHERE){
			replaceWherePart(column);
		}else if(part == ScistorMysqlSelectParser.NO_STAR){
			
			replaceSelectPart(column);
			if(this.whereColumns!=null){
				for(ScistorColumn co: this.whereColumns){
					replaceColumn(co, column);
				}
			}
		}else{
			throw new ScistorParserException("Parse Error: Unknowed type " + part);
		}
	}
	
	private void replaceSelectPart(ScistorSelectColumn column) throws ScistorParserException{
		if(this.selectColumns==null){
			throw new ScistorParserException("Parse Error");
		}
		if(column.getOwner()==null&&column.getPossibleOwners()==null&&column.getSubQueryAlias()==null){
			throw new ScistorParserException("SQL ERROR : "+column.getName()+" can not be finded");
		}
		
		for(ScistorSelectColumn co : this.selectColumns){
			replaceSelectColumn(co, column);
		}
	}
	
	public void replaceWherePart(ScistorSelectColumn column) throws ScistorParserException{
		if(this.whereColumns!=null){
			if(column.getOwner()==null&&column.getPossibleOwners()==null&&column.getSubQueryAlias()==null){
				throw new ScistorParserException("SQL ERROR : "+column.getName()+" can not be finded");
			}
			for(ScistorColumn co: this.whereColumns){
				replaceColumn(co, column);
			}
		}
	}
	
	private void replaceSelectColumn(ScistorSelectColumn co,ScistorSelectColumn column){
		if(co.getOwner()!=null){
			if(co.isFromJdbc()&&co.isOrginTable()){
				replaceColumnIf(co, column);
			}else{
				if(column.getSubQueryAlias().equals(co.getOwner())){
					replaceColumnIf(co, column);
				}
			}
		}else{
			if(co.getPossibleOwners()==null){
				replaceColumnIf(co, column);
			}
		}
	}
	private void replaceColumn(ScistorColumn co,ScistorSelectColumn column){
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
	private void replaceColumnIf(ScistorColumn co,ScistorSelectColumn column){
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
	private void replaceColumnContent(ScistorColumn co,ScistorSelectColumn column){
		co.setName(column.getName());
		co.setOwner(column.getOwner());
		co.setPossibleOwners(column.getPossibleOwners());
		co.setSubQueryAlias(column.getSubQueryAlias());
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(this.selectColumns!=null){
			sb.append("SelectedColumns:\n");
			for(ScistorSelectColumn column : this.selectColumns){
				sb.append(column.toString()+"\n");
			}
		}
		if(this.whereColumns!=null){
			sb.append("WhereColumns:\n");
			for(ScistorColumn column : this.whereColumns){
				sb.append(column.toString()+"\n");
			}
		}
		return sb.toString();
	}
	
	private Map<String,Integer> duplicateColumns;
}
