package jolyjdia.test.util.squall0;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Последовательность операций, поддерживающих асинхронный агрегат
 * для составления sql-запросов;
 * Следующий пример иллюстрирует агрегатную операцию с использованием
 * <pre>{@code
 *     DB.ofPrepare("SELECT * FROM `identifier_players`")
 *                 .executeQuery()
 *                 .doOnNext(e -> System.out.println(e.getString(2)))
 *                 .async();
 * }</pre>
 * так же этот шквал поддерживает транзакцию
 * для этого нужно выставить флаг {@link Squall#disableAutoCommit()}
 * и создать посделовательность из {@link Squall#of} и {@link Squall#execute()}
 * в конце всего шклава вызвать {@link Squall#commit()}
 * <pre>{@code
 *     DB.ofPrepare("INSERT INTO `identifier_players` (`name`) VALUES (?)")
 *                 .disableAutoCommit()
 *                 .parameters("JolyJDIA")
 *                 .execute()
 *
 *                 .of("INSERT INTO `identifier_players` (`name`) VALUES (?)")
 *                 .parameters("LemonTea")
 *                 .execute()
 *
 *                 .of("INSERT INTO `identifier_players` (`name`) VALUES (?)")
 *                 .parameters("FirboRugor")
 *                 .execute()
 *
 *                 .of("INSERT INTO `identifier_players` (`name`) VALUES (?)")
 *                 .parameters("KILLA_RAIN")
 *                 .execute()
 *
 *                 .commit()
 *                 .async();
 * }</pre>
 * Шквал делиться на собирательные промежуточные и терминальные операции
 * собирательные задают параметры {@link java.sql.Statement}
 * терминальные операции {@link Squall#sync()} или {@link Squall#async()}
 *
 * шквал имеют метод {@link Squall#close()} и реализуют {@link AutoCloseable}
 * для предотвращения утечки памяти закрывает {@link java.sql.Statement} и {@link java.sql.Connection}
 *
 * @param <T> тип результата
 * @see <a href="package-summary.html">jolyjdia.util.squall0</a>
 */
public interface Squall<U> extends AutoCloseable {
    /**
     * задает значение заданных параметров с помощью данных объектов
     * @param obj Объекты
     * @return тот же Squall
     */
    Squall<U> parameters(Object... obj);

    /**
     * Возвращает эквивалентный Squall с дополнительным обработчиком закрытия.
     * обработчик запускается при использовании метода {@link #close ()}
     * @param closeAction Задача для выполнения при закрытии потока
     * @return тот же Squall
     */
    Squall<U> onClose(Runnable closeAction);

    /**
     * задает значение по индексу заданного параметра с помощью данного объекта
     * @param obj объекты
     * @return тот же Squall
     */
    Squall<U> set(int index, Object x);

    Squall<U> disableAutoCommit();

    Squall<U> fetchSize(int size);

    Squall<U> addBatch();

    /**
     *
     * @return новый Squall
     */
    Squall<int[]> executeBatch();

    /**
     *
     * @return новый Squall
     */
    ResultSetSquall executeQuery();

    /**
     *
     * @return новый Squall
     */
    ResultSetSquall generatedKeys();

    /**
     *
     * @return новый Squall
     */
    Squall<Boolean> execute();

    /**
     *
     * @return новый Squall
     */
    Squall<U> commit();

    CompletableFuture<U> async();

    CompletableFuture<U> async(Executor executor);

    U sync();
}