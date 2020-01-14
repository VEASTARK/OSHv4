package osh.comdriver.logger;

import org.eclipse.persistence.exceptions.DatabaseException;
import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.StateExchange;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * This class helps with logging of all measurements
 * / actions taken by the controller box
 *
 * @author Kaibin Bao, Ingo Mauser
 */
public abstract class ValueDatabaseLogger extends ValueLogger {

    /**
     * ref. to singleton writer
     */
    protected final WriterThread dbWriter;
    protected EntityManagerFactory emf;

    /**
     * CONSTRUCTOR
     *
     * @param persistenceUnit
     * @param logger
     */
    public ValueDatabaseLogger(
            String persistenceUnit,
            IGlobalLogger logger) {
        this.dbWriter = new WriterThread(persistenceUnit, logger);
        this.dbWriter.start();
        this.dbWriter.setName("DB Writer Thread for PU " + persistenceUnit);
    }

    protected void put(Object entity) {
        this.dbWriter.put(entity);
    }

    @Override
    public void log(long timestamp, Object entity) throws OSHException {

        if (entity == null) {
            throw new OSHException("Persisting NULL does not make sense...");
        }

        this.put(entity);
    }

    /**
     * Retrieves the device state from the database
     */
    public StateExchange retrieve(UUID device, Long timestamp, Class<? extends StateExchange> exchangeClass) {
        StateExchange stateExchange;

        EntityManager em = this.emf.createEntityManager();

        em.getTransaction().begin();

        Query q = em.createQuery("SELECT p FROM " + exchangeClass.getSimpleName() + " p WHERE p.mid.deviceId = :uuid AND p.mid.timestamp <= :timestamp ORDER BY p.mid.timestamp DESC").setMaxResults(1);
        q.setParameter("uuid", device.toString());
        q.setParameter("timestamp", timestamp);
        if (q.getResultList().size() == 1) {
            stateExchange = (StateExchange) q.getSingleResult();
        } else {
            stateExchange = null;
        }

        em.getTransaction().commit();

        em.close();

        return stateExchange;
    }

    @Override
    protected void finalize() throws Throwable {
        this.emf.close();
        super.finalize();
    }

    public static class WriterThread extends Thread {
        private final EntityManagerFactory emf;
        private final BlockingQueue<Object> entities;
        private final IGlobalLogger logger;

        private final Set<Class<?>> unLoggableTypes = new HashSet<>();

        public WriterThread(String persistenceUnit, IGlobalLogger logger) {
            this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
            this.entities = new LinkedBlockingDeque<>();
            this.logger = logger;
        }

        public void put(Object e) {
            try {
                this.entities.put(e);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {

                EntityManager em = this.emf.createEntityManager();
                em.getTransaction().begin();

                while (true) {
                    // get top entity
                    Object entity = this.entities.peek();
                    // if there is currently no entity -> flush
                    if (entity == null) {
                        if (em.getTransaction().isActive()) {
                            try {
                                em.getTransaction().commit();
                            } catch (DatabaseException e) {
                                // should not happen...
                                this.logger.logError(e.getMessage());
                            } catch (RollbackException rbe) {
                                // everything has been rolled back ???
//								this.logger.logDebug(rbe.getMessage(), rbe);
                                this.logger.logDebug(rbe.getMessage());
                            } finally {
                                em.clear();
                                em.getTransaction().begin();
                            }

                        } else {
                            em.getTransaction().begin();
                        }
                    }

                    try {
                        // wait for entity, get top entity and remove it from queue
                        entity = this.entities.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }

                    // only log loggable types
                    if (!this.unLoggableTypes.contains(entity.getClass())) {
                        try {
//							em.persist(entity);
                            em.merge(entity);
                        } catch (DatabaseException e) {
//							this.logger.logDebug(e.getMessage(), e);
                            this.logger.logDebug(e.getMessage());
                            @SuppressWarnings("unused")
                            int debug = 0;
                            // should not happen...
                        } catch (RollbackException e) {
//							this.logger.logDebug(e.getMessage(), e);
                            this.logger.logDebug(e.getMessage());
                            // rollback is bad...
                            @SuppressWarnings("unused")
                            int debug = 0;
                            //...it should not happen
                        } catch (IllegalArgumentException e) {
//							this.logger.logWarning("Could not log entity, ignore it from now on", e));
                            this.logger.logWarning(e.getMessage());
                            this.unLoggableTypes.add(entity.getClass());
                        } catch (Exception e) {
//							this.logger.logDebug(e.getMessage(), e);
                            this.logger.logDebug(e.getMessage());
                        }

                        // do NOT commit all the time...
//						finally {
//							try {
//								em.getTransaction().commit();
//								em.clear();
//							} catch (Exception e) {
//								this.logger.logDebug(e.getMessage());
//								//e.printStackTrace();
//							}
//						}
                    } /* if (!unLoggableTypes.contains(entity.getClass())) */

                    try {
                        Thread.sleep(1); // NEW by IMA
                    } catch (Exception e) {
                        this.logger.logDebug(e);
                    }
                } /* while(true) */

            } catch (Exception e) {
                this.logger.logDebug("ExchangeDatabaseWriter died: ", e);
                this.logger.logError("Writer thread died! because of ..." + Arrays.toString(e.getStackTrace()), e);
            }
            //em.close();
        }
    }
}
