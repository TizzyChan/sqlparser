package com.scistor.parser.oracle;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.scistor.parser.result.ScistorAlterResult;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.result.ScistorSQLType;

public class ScistorOracleAlterParser extends ScistorOracleParser{
	public ScistorOracleAlterParser(SQLStatement statement) {
		super(statement);
		this.result = new ScistorAlterResult();
		this.result.setSqlType(ScistorSQLType.ALTER);
	}

	@Override
	public ScistorResult getParseResult() {
		SQLAlterTableStatement alter = (SQLAlterTableStatement) this.statement;
		String tablename = alter.getTableSource().getExpr().toString();
		((ScistorAlterResult)this.result).setTableName(tablename);
		return this.result;
	}
}
