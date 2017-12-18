package com.scistor.test;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.scistor.parser.ScistorSQLParser;
import com.scistor.parser.ScistorParser;
import com.scistor.parser.column.ScistorColumn;
import com.scistor.parser.column.ScistorJdbcColumn;
import com.scistor.parser.column.ScistorSelectColumn;
import com.scistor.parser.column.ScistorTextColumn;
import com.scistor.parser.exception.ScistorParserException;
import com.scistor.parser.mysql.ScistorMysqlSelectParser;
import com.scistor.parser.result.ScistorInsertResult;
import com.scistor.parser.result.ScistorResult;
import com.scistor.parser.result.ScistorSelectResult;

public class TT {
	
	
	public static void main(String[] args) throws ScistorParserException, ClassNotFoundException, SQLException {
		long start = System.currentTimeMillis();
		String sql = "select xx.*,b bb,sum(c) from x xx where sasd=1 and b='abc' and e='fsdf' and f like 'sdfsd' or g regexp '.*[a].*'";
//		sql = "select x.*,y.b,c from x xx join y yy on x.id=y.id join z zz on z.id=y.id where zz.d = 'aaa'";
//		sql = "select * from a";
//		sql = "select xx.a,yy.a from ( select aa a,bb b from x xxx) xx join (select cc a,dd d from y yyy) yy on yy.a=xx.a";
//		sql = "select xx.a,c from ( select aa a,bb b from x xxx) xx join yy on yy.a=xx.a";
//		sql = "select xx.a,c,d from ( select aa a,bb b from x xxx) xx join (select cc a,dd d from y yyy) yy on yy.a=xx.a join z on z.a=yy.a";
//		sql = "select aaa from (select aa aaa,bb bbb from (select a aa,b bb,c cc from x where c='sdfsd') xx)xxx where bbb='sad'";
//		sql = "select aaa,ccc,dd from (select aa aaa,bb,cc ccc from (select a,b bb,id from x ) xxx join (select id,c cc from y) yy on xxx.id=yy.id) xx "
//				+ " left join zz on zz.id=xx.aaa where bb='dfsad' join ";
//		sql = "select aaa,ccc,dd from (select aa,bb,cc ccc from xxx join yy on xxx.id=yy.id) xx "
//				+ " left join zz on zz.id=xx.aaa where bb='dfsad' join ";
//		sql = "select a,b,c from x where xx in (1,'sdf',2,'dfsdfs')";
//		sql = "select y.* from (select * from xx) x join (select c,d from yy) y on y.c=x.a";
//		sql = "select uuuu,f.* ` (select username uuuu,edu eeeee from user u where username='zby') u join friends f on f.username=u.uuuu";
//		sql = "select uuuu,f.* from (select username uuuu,edu eeeee from user u where username='zby') u join friends f on f.username=u.uuuu";
//		sql = "select username,f.* from (select username uuuu,edu eeeee from user u where username='zby') u join friends f on f.username=u.uuuu";
//		sql = "select u.username,f.* from (select username,edu eeeee from user u where username='zby') u join friends f on f.username=u.username";
//		sql = "select * from user u right join friends f on u.username=f.username right join file ff on ff.username=u.username";
//		sql = "select count(*) from bill where type='shopping' and name in (1,'Obama','Me')";
//		sql = "select * from lt left join rt on lt.id=rt.id";
		//sql = "select a,b from x union select c,d from y union select e,f from z";
/*		sql = "select count(aa) aaa,bb bbb from ( "
				+ "select a aa,b bb from "
				+ "("
				+ "select aaaa a,bbbbb b,abbbb from x "
					+ "join (select id,bbbbb from z) zz on zz.id=x.id where abbbb='sfs' "
					+ "union select aaa,c,d from y where ycccddd='sfs' group by ooo"
				+ ") xx  "
				+ "where abbbb='sfs' union select c,d from y where cddd='sfs' ) xy where xy.bb='sdfsd'";*/
//		sql = "select aa from (select a aa from x) xx where b='sdfs'";
		//sql = "select aaaa a,bbbbb b,abbbb from x join (select id,bbbbb from z) zz on zz.id=x.id where abbbb='sfs'";
//		sql = "select * from lt left join rt on lt.id=rt.id";
//		sql = "select a,b from x where c in (select e,f from (select e,f,g from y where h in (select dd from xy where dxy='3235')) yy where g='dfsdf') and cc='12344' and exists (select zzz from z where za='dfsdfa')";
//		sql = "insert into x(a,b,c) values(1,2,3)";
//		sql = "insert into x(a,b,c) values('1',2,'3'),('aa',2,'cc'),('AA',2,'CC')";
//		sql = "create table xx(id int)";
//		sql = "create table xx(id int) as select id from y";
//		sql = "drop table xx,yy,zz";
//		sql = "DELETE t1, t2 FROM t1 INNER JOIN t2 INNER JOIN t3 WHERE t1.id=t2.id AND t2.id=t3.id and xx like 'adfs'";
//		sql = "DELETE FROM t1, t2 USING t1 INNER JOIN t2 INNER JOIN t3 WHERE t1.id=t2.id AND t2.id=t3.id order by t3.bb";
//		sql = "delete from t tt where d in (select * from tt group by bb,c having a=1 and cc='dfsd' and aa+bb=1)";
//		sql = "update x,y set a=1 where b=2 order by c limit 2";
//		sql = "update x xx,y yy set xx.a=1 where yy.b=2";
//		sql = "alter table x add a int";
//		sql = "select a from (select * from x)xx";
//		sql = "select a from x union select b from y";
//		sql = "select * from lt,rt";
//		sql = "select ll.*,r.*,addr from (select id idd,age agee from lt) ll join (select id,address addr from rt) r on r.id=ll.idd where agee>0";
//		sql = "select a,b,c from (select a,b,c,d from x,y) xx where d='dsfs'";
//		sql = "select a from x union select b from y where yc='sdfs'";
//		sql = "select a from (select * from x) xx";
//		sql = "select sum(a) aa,bb b from (select a,id from x) xx join (select b bb,id from y) yy on yy.id=xx.id where bb='adfsd' group by aa order by a";
//		sql = "select user()";
//		sql = "select age aa,ll.id from (select id,age from lt) ll join rt rr on ll.id=rr.id and age=1 or aa=2";
//		sql = "select age aa from (select age,a from x)xx where xx.a=1";
//		sql = "select a,b,c from (select a from x join y on x.id=y.id) xy join z on z.id=xy.a";
//		sql = "select a from x into y";
//		sql = "select a from x where id in (1,2,3) and name in (select name from (select nn name from y) yy) and exists (select id from z)";
//		sql = "select a from x union select b from y";
		//sql = "select * from (select username,edu,name from user) u left join (select username,name,work,place from friends) f on f.username=u.username";
//		sql = "select u.* from (select username,edu,name from user) u";
//		sql = "select * from user u left join friends f on f.username=u.username";
//		sql = "select *,a from (select username a from user) u";
		//sql = "select l.*,r.* from (select id,age,name from lt ) l left join (select lt.id,address,telephone from rt join lt on lt.id=rt.id) r on r.id=l.id where address='China'";
		//sql = "select aaa,bbb,ccc from (select * from (select a aa,b from x) xx)xxx join (select ccc from (select a,b,c ccc from y) yy)yyy on yyy.ccc=xxx.bbb where xxx.aaa='sdfsf'";
		//sql = "select aa,bb from (select * from (select aa from z) x ) xx left join (select * from y) yy on yy.id=xx.id";
		//sql = "select a from x union select * from y";
		//sql = "select a from (select a,id from y) xx join (select b,id from x) yy using(id)  where yy.b = 'sdfsdf'";
		//sql = "select * from lt";
		//sql = "select a from (select count(bb) a from x) xx";
		//sql = "select count(*) from x,y";
		sql = "select sum(bb.ppp) keep(DENSE_RANK LAST order by aa.oooo desc),bb.test,1,count(bb.test),decode(aa.xx,nvl(bb.ff,0),1),aa.yy||bb.zz||aa.zz||lpad(bb.pp,'') from a aa left join b bb where aa.name=2";
		ScistorSQLParser parser = new ScistorSQLParser(sql, null);
		ScistorResult result = parser.getResult();
		
		System.out.println("---------------------");
		System.out.println(result);
		System.out.println("---------------------");
		
		if(result instanceof ScistorSelectResult){
			ScistorSelectResult re = (ScistorSelectResult) result;
			List<ScistorColumn> columns = re.getConditionColumns();
			if(columns!=null)
			for(ScistorColumn column : columns){
				System.out.println("condition column:"+column);
			}
			if(columns!=null){
				for(ScistorColumn co : columns){
					if(co instanceof ScistorTextColumn){
						ScistorTextColumn cc = (ScistorTextColumn)co;
						while(cc.hasNext()){
							String value = cc.getNextValue();
							String columnName = co.getName();
							String tablename = co.getOwner();
							if(tablename!=null){
								/*
								 * 调用加密接口
								 */
								
								cc.setSecretValue("FS!"+value);
							}else{
								List<String> possibleTables = ((ScistorColumn)co).getPossibleOwners();
								if(possibleTables!=null)
								for(String ptablename : possibleTables){
									/*
									 * 调用加密接口
									 */
									
									cc.setSecretValue("FS!"+value);
								}
							}
						}
					}else if(co instanceof ScistorColumn){
						
					}
				}
			}
			if(re.isSelectAll()){
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://192.168.1.64:3306/scidata?useUnicode=true";
				String username = "root";
				String password = "123456";
//				String url = "jdbc:mysql://localhost:3306/zby";
//				String username = "root";
//				String password = "gl901225";
				Connection conn = DriverManager.getConnection(url, username, password);
				Statement st = conn.createStatement();
				
				ResultSet rs = st.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int cols = rsmd.getColumnCount();
				ArrayList<ScistorJdbcColumn> columns1 = new ArrayList<ScistorJdbcColumn>();
				for(int i = 1;i<=cols;i++){
					ScistorJdbcColumn column = new ScistorJdbcColumn();
					column.setName(rsmd.getColumnName(i));
					column.setOwner(rsmd.getTableName(i));
					columns1.add(column);
				}
				ScistorParser selectParser = parser.getParser();
				ScistorMysqlSelectParser select = (ScistorMysqlSelectParser)selectParser;
				select.setStarColumns(columns1);
			}
			List<ScistorSelectColumn> selectedColumns = re.getSelectColumns();//只获取SELECT部分信息
			List<ScistorColumn> whereColumns = re.getConditionColumns();
			for(ScistorSelectColumn column : selectedColumns){
				System.out.println("selectTable:"+column.getOwner());
				System.out.println("selectedColumn:"+column.getName());
			}
			for(ScistorColumn column : whereColumns){
				System.out.println("whereTable:"+column.getOwner());
				System.out.println("whereColumn:"+column.getName());
			}
		}else if(result instanceof ScistorInsertResult){
			ScistorInsertResult re = (ScistorInsertResult) result;
			if(re.isNoColumn()){
				String tablename = re.getTablename();
				System.out.println("tablename:"+tablename);
			}else{
				List<ScistorColumn> columns = re.getConditionColumns();
				for(ScistorColumn column : columns){
					System.out.println("columns:"+column);
				}
				
			}
			
			
		}
		System.out.println("================");
		System.out.println(parser.getChangedSql());
		long end = System.currentTimeMillis();
		System.out.println("解析用时:"+(end-start)+"ms");
	}
	
}
