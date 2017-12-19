package com.scistor.parser.oracle;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.result.ScistorOtherResult;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.result.ScistorSQLType;

public class ScistorOtherParser  extends ScistorOracleParser{
	public ScistorOtherParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorOtherResult();
		this.result.setSqlType(ScistorSQLType.OTHER);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		return null;
	}
}
