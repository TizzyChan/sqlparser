package com.scistor.parser.column;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.scistor.parser.exception.ScistorParserException;

/**
 * 含有 字符类型 值的列
 * @author GuoLiang
 */
public class ScistorTextColumn extends ScistorColumn{

	private boolean isLike;
	private boolean isRegex;
	private List<SQLExpr> exprs;
	
	private int current = 0;
	public ScistorTextColumn(){
		super();
	}
	
	public ScistorTextColumn(String name) {
		super(name);
	}
	
	public ScistorTextColumn(String owner,String name){
		super(owner, name);
	}
	
	public boolean isLike() {
		return isLike;
	}

	public void setLike(boolean isLike) {
		this.isLike = isLike;
	}

	public boolean isRegex() {
		return isRegex;
	}

	public void setRegex(boolean isRegex) {
		this.isRegex = isRegex;
	}
	
	public void addExpr(SQLExpr expr){
		if(this.exprs == null){
			this.exprs = new ArrayList<SQLExpr>();
		}
		this.exprs.add(expr);
	}
	
	public boolean hasNext() {
		return current<this.exprs.size();
	}

	public String getNextValue() throws ScistorParserException {
		String value = null;
		if(this.exprs.get(current) instanceof SQLTextLiteralExpr){
			value = ((SQLTextLiteralExpr)this.exprs.get(current)).getText();
		}else{
			throw new ScistorParserException("column '"+this.name+"' is not char type.");
		}
		current++;
		return value;
	}

	public void setSecretValue(String secretValue) {
		int position = current-1;
		((SQLTextLiteralExpr)this.exprs.get(position)).setText(secretValue);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(this.owner!=null){
			sb.append("[name:"+this.name+",owner:"+this.owner+",isLike:"+this.isLike+",isRegexp:"+this.isRegex+"]");
		}else{
			sb.append("[name:"+this.name+",possibleOwners:"+this.possibleOwners+",isLike:"+this.isLike+",isRegexp:"+this.isRegex+"]");
		}
		sb.append("[values:");
		int size = this.exprs.size();
		for(SQLExpr expr : this.exprs){
			if(expr instanceof SQLTextLiteralExpr){
				sb.append(((SQLTextLiteralExpr)expr).getText());
			}else if(expr instanceof SQLNumericLiteralExpr){
				sb.append(((SQLNumericLiteralExpr)expr).getNumber().longValue());
			}
			if(--size!=0) sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
}
