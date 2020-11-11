package com.tdl.redis.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.annotation.Resource;


@Service
public class RedisConnectionService
{

	@Value("${redis.persister.transactions.support.enabled}")
	private boolean enableTransactionSupport;


	@Resource(name = "jedisConnectionFactory")
	private RedisConnectionFactory factory;


	public <T> T execute(final RedisCallback<T> action)
	{
		return execute(action, false);
	}


	public <T> T execute(final RedisCallback<T> action, final boolean pipeline)
	{
		Assert.notNull(action, "Callback object must not be null");

		RedisConnection conn = null;
		try
		{

			if (enableTransactionSupport)
			{
				// only bind resources in case of potential transaction synchronization
				conn = RedisConnectionUtils.bindConnection(factory, true);
			}
			else
			{
				conn = RedisConnectionUtils.getConnection(factory);
			}

			final boolean existingConnection = TransactionSynchronizationManager.hasResource(factory);
			final RedisConnection connToUse = preProcessConnection(conn, existingConnection);

			final boolean pipelineStatus = connToUse.isPipelined();
			if (pipeline && !pipelineStatus)
			{
				connToUse.openPipeline();
			}

			final T result = action.doInRedis(connToUse);

			// close pipeline
			if (pipeline && !pipelineStatus)
			{
				connToUse.closePipeline();
			}

			return postProcessResult(result, connToUse, existingConnection);
		}
		finally
		{

			if (!enableTransactionSupport)
			{
				RedisConnectionUtils.releaseConnection(conn, factory);
			}
		}
	}

	private <T> T postProcessResult(final T result, final RedisConnection conn, final boolean existingConnection)
	{
		return result;
	}

	@SuppressWarnings("unused")
	private RedisConnection preProcessConnection(final RedisConnection connection, final boolean existingConnection)
	{
		return connection;
	}


}
