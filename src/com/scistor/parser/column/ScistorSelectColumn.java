package com.scistor.parser.column;

/**
 * select 列
 * @author GuoLiang
 */
public class ScistorSelectColumn extends ScistorColumn{
	 
	private String alias;
	private boolean isSelectAll;
	private boolean isAggregator;
	
	private boolean isFromJdbc;	//为  * 号时才使用
	private boolean isOrginTable;	//为 * 号时才使用
	
	public ScistorSelectColumn(){
		super();
	}
	public ScistorSelectColumn(String name){
		super(name);
		if(name!=null&&name.equals("*")){
			this.isSelectAll = true;
		}
	}
	
	public ScistorSelectColumn(String owner,String name){
		this(name);
		this.owner = owner;
	}
	
	public void setOwner(String owner) {
		if(!isFromJdbc){
			this.owner = owner;
		}else{
			if(this.owner==null){
				this.owner = owner;
			}
		}
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public void column(String name) {
		this.name = name;
		if(name.equals("*")){
			this.isSelectAll = true;
		}
	}
	
	public boolean isSelectAll() {
		return isSelectAll;
	}
	
	public void setSelectAll(boolean isSelectAll){
		this.isSelectAll = isSelectAll;
	}
	public int hashCode(){
		int code = 1;
		code = code + this.name.hashCode();
		return code;
	}
	public boolean equals(Object o){
		ScistorSelectColumn out = (ScistorSelectColumn) o;
		if(this.name.equals(out.getName())){
			if(this.owner==null&&out.getOwner()==null){
				if(this.possibleOwners!=null&&out.getPossibleOwners()!=null&&(this.possibleOwners.size()==out.getPossibleOwners().size())){
					for(int i=0;i<this.possibleOwners.size();i++){
						if(!this.possibleOwners.get(i).equals(out.getPossibleOwners().get(i))){
							return false;
						}
					}
					return true;
				}
			}else if(this.owner!=null&&out.getPossibleOwners()!=null&&out.getPossibleOwners().size()>1){
				if(out.getPossibleOwners().contains(this.owner)){
					return true;
				}
			}else if(this.possibleOwners!=null&&out.getOwner()!=null&&this.possibleOwners.size()>1){
				if(this.possibleOwners.contains(out.getOwner())){
					return true;
				}
			}
		}
		return false;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(this.owner!=null){
			sb.append("[name:"+this.name+",owner:"+this.owner+",isSelectAll:"+this.isSelectAll+",isAggregator:"+this.isAggregator+"]");
		}else{
			sb.append("[name:"+this.name+",possibleOwners:"+this.possibleOwners+",isSelectAll:"+this.isSelectAll+",isAggregator:"+this.isAggregator+"]");
		}
		
		return sb.toString();
	}
	public boolean isAggregator() {
		return isAggregator;
	}
	public void setAggregator(boolean isAggregator) {
		this.isAggregator = isAggregator;
	}
	
	/**
	 * 内部使用
	 * @return
	 */
	public boolean isFromJdbc() {
		return isFromJdbc;
	}
	/**
	 * 内部使用
	 * @return
	 */
	public void setFromJdbc(boolean isFromJdbc) {
		this.isFromJdbc = isFromJdbc;
	}
	/**
	 * 内部使用
	 * @return
	 */
	public boolean isOrginTable() {
		return isOrginTable;
	}
	/**
	 * 内部使用
	 * @return
	 */
	public void setOrginTable(boolean isOrginTable) {
		this.isOrginTable = isOrginTable;
	}
}
