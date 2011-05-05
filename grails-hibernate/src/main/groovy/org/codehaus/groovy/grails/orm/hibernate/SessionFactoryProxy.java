/*
 * Copyright 2011 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.orm.hibernate;

import org.hibernate.*;
import org.hibernate.cache.QueryCache;
import org.hibernate.cache.Region;
import org.hibernate.cache.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;
import org.hibernate.classic.Session;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.context.CurrentSessionContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.*;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.engine.query.QueryPlanCache;
import org.hibernate.exception.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.StatisticsImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.SpringSessionContext;
import org.springframework.util.ReflectionUtils;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Proxies the SessionFactory allowing for the underlying SessionFactory instance to be replaced at runtime.
 * Used to enable rebuilding of the SessionFactory at development time
 *
 * @since 1.4
 * @author Graeme Rocher
 */
public class SessionFactoryProxy implements SessionFactory, SessionFactoryImplementor, ApplicationContextAware, InitializingBean{

    private String targetBean;
    private ApplicationContext applicationContext;
    private Class currentSessionContextClass = SpringSessionContext.class;

    /**
     * The target bean to proxy
     *
     * @param targetBean The name of the target bean
     */
    public void setTargetBean(String targetBean) {
        this.targetBean = targetBean;
    }

    /**
     * The class to use for the current session context
     *
     * @param currentSessionContextClass The current session context class
     */
    public void setCurrentSessionContextClass(Class currentSessionContextClass) {
        this.currentSessionContextClass = currentSessionContextClass;
    }

    /**
     * @return The current SessionFactory being proxied
     */
    public SessionFactory getCurrentSessionFactory() {
         return applicationContext.getBean(targetBean, SessionFactoryHolder.class).getSessionFactory();
    }

    /**
     * @return The current SessionFactoryImplementor being proxied
     */
    public SessionFactoryImplementor getCurrentSessionFactoryImplementor() {
         return (SessionFactoryImplementor) applicationContext.getBean(targetBean, SessionFactoryHolder.class).getSessionFactory();
    }

    public Session openSession() throws HibernateException {
        return getCurrentSessionFactory().openSession();
    }


    public Session openSession(Interceptor interceptor) throws HibernateException {
        return getCurrentSessionFactory().openSession(interceptor);
    }

    public Session openSession(Connection connection) {
        return getCurrentSessionFactory().openSession(connection);
    }

    public Session openSession(Connection connection, Interceptor interceptor) {
        return getCurrentSessionFactory().openSession(connection, interceptor);
    }

    public Session getCurrentSession() throws HibernateException {
        return getCurrentSessionFactory().getCurrentSession();
    }

    public StatelessSession openStatelessSession() {
        return getCurrentSessionFactory().openStatelessSession();
    }

    public StatelessSession openStatelessSession(Connection connection) {
        return getCurrentSessionFactory().openStatelessSession(connection);
    }

    public ClassMetadata getClassMetadata(Class entityClass) {
        return getCurrentSessionFactory().getClassMetadata(entityClass);
    }

    public ClassMetadata getClassMetadata(String entityName) {
        return getCurrentSessionFactory().getClassMetadata(entityName);
    }

    public CollectionMetadata getCollectionMetadata(String roleName) {
        return getCurrentSessionFactory().getCollectionMetadata(roleName);
    }

    public Map<String, ClassMetadata> getAllClassMetadata() {
        return getCurrentSessionFactory().getAllClassMetadata();
    }

    public Map getAllCollectionMetadata() {
        return getCurrentSessionFactory().getAllCollectionMetadata();
    }

    public Statistics getStatistics() {
        return getCurrentSessionFactory().getStatistics();
    }

    public void close() throws HibernateException {
        getCurrentSessionFactory().close();
    }

    public boolean isClosed() {
        return getCurrentSessionFactory().isClosed();
    }

    public Cache getCache() {
        return getCurrentSessionFactory().getCache();
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evict(Class persistentClass) throws HibernateException {
        getCurrentSessionFactory().evict(persistentClass);
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evict(Class persistentClass, Serializable id) throws HibernateException {
        getCurrentSessionFactory().evict(persistentClass, id);
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evictEntity(String entityName) throws HibernateException {
        getCurrentSessionFactory().evictEntity(entityName);
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evictEntity(String entityName, Serializable id) throws HibernateException {
        getCurrentSessionFactory().evictEntity(entityName, id);
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evictCollection(String roleName) throws HibernateException {
        getCurrentSessionFactory().evictCollection(roleName);
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evictCollection(String roleName, Serializable id) throws HibernateException {
        getCurrentSessionFactory().evictCollection(roleName, id);
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evictQueries(String cacheRegion) throws HibernateException {
        getCurrentSessionFactory().evictQueries(cacheRegion);
    }

    @SuppressWarnings({"deprecation"})
    @Deprecated
    public void evictQueries() throws HibernateException {
        getCurrentSessionFactory().evictQueries();
    }

    public Set getDefinedFilterNames() {
        return getCurrentSessionFactory().getDefinedFilterNames();
    }

    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return getCurrentSessionFactory().getFilterDefinition(filterName);
    }

    public boolean containsFetchProfileDefinition(String name) {
        return getCurrentSessionFactory().containsFetchProfileDefinition(name);
    }

    public TypeHelper getTypeHelper() {
        return getCurrentSessionFactory().getTypeHelper();
    }

    public Reference getReference() throws NamingException {
        return getCurrentSessionFactory().getReference();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return getCurrentSessionFactoryImplementor().getTypeResolver();
    }

    @Override
    public Properties getProperties() {
        return getCurrentSessionFactoryImplementor().getProperties();
    }

    @Override
    public EntityPersister getEntityPersister(String entityName) throws MappingException {
        return getCurrentSessionFactoryImplementor().getEntityPersister(entityName);
    }

    @Override
    public CollectionPersister getCollectionPersister(String role) throws MappingException {
        return getCurrentSessionFactoryImplementor().getCollectionPersister(role);
    }

    @Override
    public Dialect getDialect() {
        return getCurrentSessionFactoryImplementor().getDialect();
    }

    @Override
    public Interceptor getInterceptor() {
        return getCurrentSessionFactoryImplementor().getInterceptor();
    }

    @Override
    public QueryPlanCache getQueryPlanCache() {
        return getCurrentSessionFactoryImplementor().getQueryPlanCache();
    }

    @Override
    public Type[] getReturnTypes(String queryString) throws HibernateException {
        return getCurrentSessionFactoryImplementor().getReturnTypes(queryString);
    }

    @Override
    public String[] getReturnAliases(String queryString) throws HibernateException {
        return getCurrentSessionFactoryImplementor().getReturnAliases(queryString);
    }

    @Override
    public ConnectionProvider getConnectionProvider() {
        return getCurrentSessionFactoryImplementor().getConnectionProvider();
    }

    @Override
    public String[] getImplementors(String className) throws MappingException {
        return getCurrentSessionFactoryImplementor().getImplementors(className);
    }

    @Override
    public String getImportedClassName(String name) {
        return getCurrentSessionFactoryImplementor().getImportedClassName(name);
    }

    @Override
    public TransactionManager getTransactionManager() {
        return getCurrentSessionFactoryImplementor().getTransactionManager();
    }

    @Override
    public QueryCache getQueryCache() {
        return getCurrentSessionFactoryImplementor().getQueryCache();
    }

    @Override
    public QueryCache getQueryCache(String regionName) throws HibernateException {
        return getCurrentSessionFactoryImplementor().getQueryCache(regionName);
    }

    @Override
    public UpdateTimestampsCache getUpdateTimestampsCache() {
        return getCurrentSessionFactoryImplementor().getUpdateTimestampsCache();
    }

    @Override
    public StatisticsImplementor getStatisticsImplementor() {
        return getCurrentSessionFactoryImplementor().getStatisticsImplementor();
    }

    @Override
    public NamedQueryDefinition getNamedQuery(String queryName) {
        return getCurrentSessionFactoryImplementor().getNamedQuery(queryName);
    }

    @Override
    public NamedSQLQueryDefinition getNamedSQLQuery(String queryName) {
        return getCurrentSessionFactoryImplementor().getNamedSQLQuery(queryName);
    }

    @Override
    public ResultSetMappingDefinition getResultSetMapping(String name) {
        return getCurrentSessionFactoryImplementor().getResultSetMapping(name);
    }

    @Override
    public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
        return getCurrentSessionFactoryImplementor().getIdentifierGenerator(rootEntityName);
    }

    @Override
    public Region getSecondLevelCacheRegion(String regionName) {
        return getCurrentSessionFactoryImplementor().getSecondLevelCacheRegion(regionName);
    }

    @Override
    public Map getAllSecondLevelCacheRegions() {
        return getCurrentSessionFactoryImplementor().getAllSecondLevelCacheRegions();
    }

    @Override
    public SQLExceptionConverter getSQLExceptionConverter() {
        return getCurrentSessionFactoryImplementor().getSQLExceptionConverter();
    }

    @Override
    public Settings getSettings() {
        return getCurrentSessionFactoryImplementor().getSettings();
    }

    @Override
    public Session openTemporarySession() throws HibernateException {
        return getCurrentSessionFactoryImplementor().openTemporarySession();
    }

    @Override
    public Session openSession(Connection connection, boolean flushBeforeCompletionEnabled, boolean autoCloseSessionEnabled, ConnectionReleaseMode connectionReleaseMode) throws HibernateException {
        return getCurrentSessionFactoryImplementor().openSession(connection,flushBeforeCompletionEnabled,autoCloseSessionEnabled, connectionReleaseMode);
    }

    @Override
    public Set<String> getCollectionRolesByEntityParticipant(String entityName) {
        return getCurrentSessionFactoryImplementor().getCollectionRolesByEntityParticipant(entityName);
    }

    @Override
    public EntityNotFoundDelegate getEntityNotFoundDelegate() {
        return getCurrentSessionFactoryImplementor().getEntityNotFoundDelegate();
    }

    @Override
    public SQLFunctionRegistry getSqlFunctionRegistry() {
        return getCurrentSessionFactoryImplementor().getSqlFunctionRegistry();
    }

    @Override
    public FetchProfile getFetchProfile(String name) {
        return getCurrentSessionFactoryImplementor().getFetchProfile(name);
    }

    @Override
    public SessionFactoryObserver getFactoryObserver() {
        return getCurrentSessionFactoryImplementor().getFactoryObserver();
    }

    @SuppressWarnings({"deprecation"})
    @Override
    @Deprecated
    public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
        return getCurrentSessionFactoryImplementor().getIdentifierGeneratorFactory();
    }

    @Override
    public Type getIdentifierType(String className) throws MappingException {
        return getCurrentSessionFactoryImplementor().getIdentifierType(className);
    }

    @Override
    public String getIdentifierPropertyName(String className) throws MappingException {
        return getCurrentSessionFactoryImplementor().getIdentifierPropertyName(className);
    }

    @Override
    public Type getReferencedPropertyType(String className, String propertyName) throws MappingException {
        return getCurrentSessionFactoryImplementor().getReferencedPropertyType(className,propertyName);
    }

    @Override
    public void afterPropertiesSet() {
        SessionFactoryImplementor sessionFactory = getCurrentSessionFactoryImplementor();

        // patch the currentSessionContext variable of SessionFactoryImpl to use this proxy as the key
        CurrentSessionContext ssc = createCurrentSessionContext();

        try {
            Class<? extends SessionFactoryImplementor> sessionFactoryClass = sessionFactory.getClass();
            Field currentSessionContextField = sessionFactoryClass.getDeclaredField("currentSessionContext");
            if(currentSessionContextField != null) {

                ReflectionUtils.makeAccessible(currentSessionContextField);
                currentSessionContextField.set(sessionFactory, ssc);
            }
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (SecurityException e) {
            // ignore
        } catch (IllegalArgumentException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        }
    }

    protected CurrentSessionContext createCurrentSessionContext() {
        Class sessionContextClass = currentSessionContextClass;
        if(sessionContextClass == null) {
            sessionContextClass = SpringSessionContext.class;
        }
        try {
            Constructor<CurrentSessionContext> constructor = sessionContextClass.getConstructor(SessionFactoryImplementor.class);
            return BeanUtils.instantiateClass(constructor, this);
        } catch (NoSuchMethodException e) {
            return new SpringSessionContext(this);
        }
    }
}