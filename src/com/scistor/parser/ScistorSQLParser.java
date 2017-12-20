package com.scistor.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.scistor.parser.authentication.ScistorAuthenticate;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.factory.ScistorMysqlParserFactory;
import com.scistor.parser.factory.ScistorOracleParserFactory;
import com.scistor.parser.factory.ScistorParserFactory;
import com.scistor.parser.result.ScistorResult;

/**
 * 解析入口
 * @author GuoLiang
 */
public class ScistorSQLParser {
	
	private ScistorResult result;	
	private ScistorAuthenticate auth;	///对where的加密或者其他形式的加密实现这个接口，根据结果集进行加密
	private SQLStatement statement;
	private ScistorParser sparser;
	
	public ScistorSQLParser(String sql,ScistorAuthenticate authentication) throws ScistorParserException{
		this.auth = authentication;
		parse(sql);
	}
	public void parse(String sql) throws ScistorParserException{
		//MySqlStatementParser parser = new MySqlStatementParser(sql);
		OracleStatementParser parser = new OracleStatementParser(sql);
		statement = parser.parseStatement();
		//ScistorParserFactory factory = new ScistorMysqlParserFactory();
		ScistorParserFactory factory = new ScistorOracleParserFactory();
		sparser = null;
		if(statement instanceof SQLSelectStatement){
			sparser = factory.createSelectParser(statement);
			
		}
		/*else if(statement instanceof MySqlInsertStatement){
			sparser = factory.createInsertParser(statement);
		
		}else if(statement instanceof MySqlUpdateStatement){
			sparser = factory.createUpdateParser(statement);
		
		}else if(statement instanceof MySqlDeleteStatement){
			sparser = factory.createDeleteParser(statement);
		
		}else if(statement instanceof SQLAlterTableStatement){
			sparser = factory.createAlterParser(statement);
		
		}else if(statement instanceof MySqlCreateTableStatement){
			sparser = factory.createCreateParser(statement);
		
		}else if(statement instanceof SQLDropTableStatement){
			sparser = factory.createDropParser(statement);
			
		}else {
			sparser = factory.createOtherParser(statement);
		}*/
		
		this.result = sparser.getParseResult();
	}
	
	public ScistorParser getParser(){
		return this.sparser;
	}
	
	public ScistorAuthenticate getAuthenticate(){
		return this.auth;
	}
	public int getAuthResult(){
		return this.auth.auth(this.result);
	}
	
	public ScistorResult getResult(){
		return this.result;
	}
	
	public String getChangedSql() {
		return statement.toString();
	}
	
}
