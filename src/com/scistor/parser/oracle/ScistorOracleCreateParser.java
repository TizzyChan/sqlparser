package com.scistor.parser.oracle;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleCreateTableStatement;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.result.ScistorCreateResult;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.result.ScistorSQLType;

public class ScistorOracleCreateParser extends ScistorOracleParser{
	public ScistorOracleCreateParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorCreateResult();
		this.result.setSqlType(ScistorSQLType.CREATE);
	}

	@Override
	public ScistorResult getParseResult() throws ScistorParserException {
		OracleCreateTableStatement create = (OracleCreateTableStatement) this.statement;
		String tablename = create.getName().getSimpleName();
		((ScistorCreateResult)this.result).setTableName(tablename);
		if(create.getSelect()!=null){
			throw new ScistorParserException("SQL ERROR : create table with select not support.");
		}
		return this.result;
	}
}
