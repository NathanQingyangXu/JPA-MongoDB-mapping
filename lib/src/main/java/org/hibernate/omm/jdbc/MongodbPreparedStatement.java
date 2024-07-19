package org.hibernate.omm.jdbc;

import com.mongodb.client.MongoDatabase;

import org.hibernate.engine.jdbc.mutation.JdbcValueBindings;
import org.hibernate.engine.jdbc.mutation.TableInclusionChecker;
import org.hibernate.omm.jdbc.adapter.PreparedStatementAdapter;
import org.hibernate.omm.jdbc.exception.NotSupportedSQLException;
import org.hibernate.omm.jdbc.exception.SimulatedSQLException;
import org.hibernate.omm.util.StringUtil;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MongodbPreparedStatement extends MongodbStatement
		implements PreparedStatementAdapter {

	private String commandString;
	private Map<Integer, String> parameters;

	public MongodbPreparedStatement(MongoDatabase mongoDatabase, Connection connection, String sql) {
		super( mongoDatabase, connection );
		this.commandString = sql;
		this.parameters = new HashMap<>();
	}

	@Override
	public ResultSet executeQuery() throws SimulatedSQLException {
		return executeQuery( getFinalCommandString() );
	}

	@Override
	public int executeUpdate() throws SimulatedSQLException {
		return executeUpdate( getFinalCommandString() );
	}

	@Override
	public boolean execute() throws SimulatedSQLException {
		return execute( getFinalCommandString() );
	}

	/**
	 * @see org.hibernate.engine.jdbc.batch.internal.BatchImpl#addToBatch(JdbcValueBindings,
	 * TableInclusionChecker)
	 */
	@Override
	public void addBatch() {
		// no-op
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SimulatedSQLException {
		parameters.put( parameterIndex, "null" );
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SimulatedSQLException {
		parameters.put( parameterIndex, Boolean.toString( x ) );
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SimulatedSQLException {
		parameters.put( parameterIndex, Byte.toString( x ) );
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SimulatedSQLException {
		parameters.put( parameterIndex, Short.toString( x ) );
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SimulatedSQLException {
		parameters.put( parameterIndex, "{ \"$numberInt\": \"" + x + "\" }" );
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SimulatedSQLException {
		parameters.put( parameterIndex, "{ \"$numberLong\": \"" + x + "\" }" );
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SimulatedSQLException {
		parameters.put( parameterIndex, Float.toString( x ) );
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SimulatedSQLException {
		parameters.put( parameterIndex, "{ \"$numberDouble\": \"" + x + "\" }" );
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SimulatedSQLException {
		parameters.put( parameterIndex, "{ \"$numberDecimal\": \"" + x + "\" }" );
	}

	@Override
	public void setString(int parameterIndex, String x) throws SimulatedSQLException {
		parameters.put( parameterIndex, StringUtil.writeStringHelper( x ) );
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SimulatedSQLException {
		String base64Text = Base64.getEncoder().encodeToString( x );
		parameters.put( parameterIndex, "\"$binary\": {\"base64\": \"" + base64Text + "\", \"subType\": \"0\"}" );
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SimulatedSQLException {
		parameters.put( parameterIndex, "{\"$date\": {\"$numberLong\": \"" + x.toInstant().getEpochSecond() + "\"}}" );
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SimulatedSQLException {
		parameters.put( parameterIndex, "{\"$date\": {\"$numberLong\": \"" + x.toInstant().getEpochSecond() + "\"}}" );
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SimulatedSQLException {
		parameters.put(
				parameterIndex,
				"{\"$timestamp\": {\"" + x.toInstant().getEpochSecond() + "\": <t>, \"i\": 1}}"
		);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setNString(int parameterIndex, String value) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length)
			throws SimulatedSQLException {
		throw new NotSupportedSQLException();
	}

	private String getFinalCommandString() {
		int parameterIndex = 1;
		int lastIndex = -1;
		int index;
		StringBuilder sb = new StringBuilder();

		while ( ( index = commandString.indexOf( '?', lastIndex + 1 ) ) != -1 ) {
			sb.append( commandString, lastIndex + 1, index );
			String parameterValue = parameters.get( parameterIndex++ );
			sb.append( parameterValue );
			lastIndex = index;
		}
		sb.append( commandString.substring( lastIndex + 1 ) );
		return sb.toString();
	}
}
