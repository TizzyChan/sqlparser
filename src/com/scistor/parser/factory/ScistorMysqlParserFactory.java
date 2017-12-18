package com.scistor.parser.factory;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.scistor.parser.mysql.ScistorMysqlAlterParser;
import com.scistor.parser.mysql.ScistorMysqlCreateParser;
import com.scistor.parser.mysql.ScistorMysqlDeleteParser;
import com.scistor.parser.mysql.ScistorMysqlDropParser;
import com.scistor.parser.mysql.ScistorMysqlInsertParser;
import com.scistor.parser.mysql.ScistorMysqlParser;
import com.scistor.parser.mysql.ScistorMysqlSelectParser;
import com.scistor.parser.mysql.ScistorMysqlUpdateParser;
import com.scistor.parser.mysql.ScistorOtherParser;

/**
 * Mysql½âÎö¹¤³§
 * @author GuoLiang
 */
public class ScistorMysqlParserFactory implements ScistorParserFactory{

	@Override
	public ScistorMysqlParser createSelectParser(SQLStatement statement) {
		return new ScistorMysqlSelectParser(statement);
	}

	@Override
	public ScistorMysqlParser createInsertParser(SQLStatement statement) {
		return new ScistorMysqlInsertParser(statement);
	}

	@Override
	public ScistorMysqlParser createUpdateParser(SQLStatement statement) {
		return new ScistorMysqlUpdateParser(statement);
	}

	@Override
	public ScistorMysqlParser createDeleteParser(SQLStatement statement) {
		return new ScistorMysqlDeleteParser(statement);
	}

	@Override
	public ScistorMysqlParser createAlterParser(SQLStatement statement) {
		return new ScistorMysqlAlterParser(statement);
	}

	@Override
	public ScistorMysqlParser createCreateParser(SQLStatement statement) {
		
		return new ScistorMysqlCreateParser(statement);
	}

	@Override
	public ScistorMysqlParser createDropParser(SQLStatement statement) {
		return new ScistorMysqlDropParser(statement);
	}

	@Override
	public ScistorMysqlParser createOtherParser(SQLStatement statement) {
		return new ScistorOtherParser(statement);
	}

}
