package com.clearinghouse.configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EntityScan(basePackages = {"com.clearinghouse.entity"})
@EnableJpaRepositories(
        basePackages = {"com.clearinghouse.dao"},
        entityManagerFactoryRef = "mysqlEntityManagerFactory",
        transactionManagerRef = "mysqlTransactionManager"
)
public class MysqlJpaConfig {

    /**
     * Optional explicit Hibernate dialect for the primary (mysql) EntityManagerFactory.
     * Left blank by default, in which case Hibernate auto-detects the dialect from JDBC
     * metadata (the existing prod/qa behavior). A profile may set
     * {@code mysql.hibernate.dialect} to force a dialect — used by the local profile to
     * avoid metadata-based detection against a non-matching local server version.
     */
    @Value("${mysql.hibernate.dialect:}")
    private String mysqlHibernateDialect;

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        return mysqlDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "mysqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            @Qualifier("mysqlDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.clearinghouse.entity");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        // Only override the dialect when explicitly configured (local profile). When blank,
        // no property is set and Hibernate auto-detects as before — prod/qa unchanged.
        if (mysqlHibernateDialect != null && !mysqlHibernateDialect.isBlank()) {
            Properties props = new Properties();
            props.put("hibernate.dialect", mysqlHibernateDialect.trim());
            emf.setJpaProperties(props);
        }
        return emf;
    }

    @Primary
    @Bean(name = "mysqlTransactionManager")
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

