package jolyjdia.test.util.squall0;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//ОФК все отредачить
public class AbstractSquall<S_OUT> implements Squall<S_OUT> {

    private static final ExecutorService defaultExecutor = Executors.newCachedThreadPool();

    final AbstractSquall<?> sourceSquall;
    final PreparedStatement preparedStatement;

    List<StatelessFunc<?>> listSteps;
    Runnable closeAction;
    Connection connection;

    public AbstractSquall(Connection connection, final String sql, int key) throws SQLException {
        this.listSteps = new ArrayList<>();
        this.preparedStatement = (this.connection = connection).prepareStatement(sql, key);
        this.sourceSquall = this;
    }

    public AbstractSquall(Connection connection, final String sql) throws SQLException {
        this(connection, sql, Statement.NO_GENERATED_KEYS);
    }

    protected AbstractSquall(AbstractSquall<?> previousStage, PreparedStatement preparedStatement) {
        this.sourceSquall = previousStage.sourceSquall;
        this.preparedStatement = preparedStatement;
    }
    protected AbstractSquall(AbstractSquall<?> previousStage) {
        this(previousStage, previousStage.preparedStatement);
    }

    @Override
    public AbstractSquall<S_OUT> parameters(Object... obj) {
        Objects.requireNonNull(obj);
        assertOpen();
        int length = obj.length;
        try {
            for (int i = 0; i < length; ++i) {
                preparedStatement.setObject(i + 1, obj[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**======================================================================
    * --------------------------------{Test}--------------------------------
    ======================================================================*/

    public <T> AbstractSquall<T> of(String sql) {
        try {
            return new AbstractSquall<>(this, sourceSquall.connection.prepareStatement(sql));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public AbstractSquall<S_OUT> disableAutoCommit() {
        try {
            sourceSquall.connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> commit() {
        return new StatelessFunc<>(this) {
            @Override
            public Void opWrapSink(PreparedStatement ps) throws SQLException {
                sourceSquall.connection.commit();
                return null;
            }
        };
    }

    protected void checkRollback() {
        try {
            if(!sourceSquall.connection.getAutoCommit()) {
                sourceSquall.connection.rollback();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Произошла ошибка отката", e);
        }
    }
    /**======================================================================
     * ----------------------------------------------------------------------
     ======================================================================*/

    @Override
    public AbstractSquall<S_OUT> onClose(Runnable closeAction) {
        Objects.requireNonNull(closeAction);
        this.sourceSquall.closeAction = closeAction;
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> set(int index, Object x) {
        assertOpen();
        try {
            preparedStatement.setObject(index, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> fetchSize(int size) {
        assertOpen();
        try {
            preparedStatement.setFetchSize(size);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> addBatch() {
        assertOpen();
        try {
            preparedStatement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    //TODO: проверить на утечку памяти
    @Override
    public AbstractSquall<Boolean> execute() {
        return new StatelessFunc<>(this) {
            @Override
            public Boolean opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.execute();
            }
        };
    }

    @Override
    public AbstractSquall<int[]> executeBatch() {
        return new StatelessFunc<>(this) {
            @Override
            public int[] opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.executeBatch();
            }
        };
    }

    @Override
    public ResultSetSquall executeQuery() {
        return new ResultSetSquall(this) {
            @Override
            public ResultSet opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.executeQuery();
            }
        };
    }

    @Override
    public ResultSetSquall generatedKeys() {
        return new ResultSetSquall(this) {
            @Override
            public ResultSet opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.getGeneratedKeys();
            }
        };
    }


    public abstract static class StatelessFunc<T> extends AbstractSquall<T> {
        protected StatelessFunc(AbstractSquall<?> squall) {
            super(squall);
            sourceSquall.listSteps.add(this);
        }
        public abstract <R> R opWrapSink(PreparedStatement preparedStatement) throws SQLException;
    }

    @Override
    public S_OUT sync() {
        try {
            S_OUT result = null;
            for (StatelessFunc<?> step : sourceSquall.listSteps) {
                System.out.println(step);
                result = step.opWrapSink(step.preparedStatement);
            }
            return result;
        } catch (SQLException e) {
            checkRollback();
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    @Override
    public CompletableFuture<S_OUT> async() {
        return async(defaultExecutor);
    }

    @Override
    public CompletableFuture<S_OUT> async(Executor executor) {
        return CompletableFuture.supplyAsync(this::sync, executor);
    }

    protected void assertOpen() {
        try {
            if (preparedStatement.isClosed()) {
                throw new IllegalStateException("Squall уже закрылся");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if(sourceSquall.closeAction != null) {
            sourceSquall.closeAction.run();
        }
        try {
            sourceSquall.connection.close();
            for(AbstractSquall<?> abstractSquall : sourceSquall.listSteps) {
                abstractSquall.preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
