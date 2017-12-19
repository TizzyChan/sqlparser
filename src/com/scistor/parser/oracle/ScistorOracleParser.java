package com.scistor.parser.oracle;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.scistor.parser.ScistorParser;
import com.scistor.parser.result.ScistorResult;

public abstract class ScistorOracleParser implements ScistorParser{
	protected ScistorResult result;
	protected SQLStatement statement;
	
	public ScistorOracleParser(SQLStatement statement){
		this.statement = statement;
	}
	
}
