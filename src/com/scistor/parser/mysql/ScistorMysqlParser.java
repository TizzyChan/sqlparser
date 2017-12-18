package com.scistor.parser.mysql;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.scistor.parser.ScistorParser;
import com.scistor.parser.result.ScistorResult;

public abstract class ScistorMysqlParser implements ScistorParser{
	protected ScistorResult result;
	protected SQLStatement statement;
	
	public ScistorMysqlParser(SQLStatement statement){
		this.statement = statement;
	}
	
	
}
